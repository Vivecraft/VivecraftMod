package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MultiPassList;
import org.vivecraft.client_vr.MultiPassRenderTarget;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Mixin(PostChain.class)
public abstract class PostChainVRMixin {

    @Shadow
    @Final
    private RenderTarget screenTarget;

    @Shadow
    @Final
    @Mutable
    private List<PostPass> passes;

    @Accessor
    public abstract List<PostPass> getPasses();

    @Unique
    private final EnumMap<RenderPass, PostChain> vivecraft$VRPostChains = new EnumMap<>(RenderPass.class);

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void vivecraft$createVRChains(
        TextureManager textureManager, ResourceProvider resourceProvider, RenderTarget screenTarget,
        ResourceLocation name, CallbackInfo ci) throws IOException
    {
        if (VRState.VR_INITIALIZED && this.screenTarget == RenderPassManager.INSTANCE.vanillaRenderTarget) {
            for (RenderPass pass : RenderPass.values()) {
                // gui has no world renderpass
                if (pass == RenderPass.GUI) {
                    this.vivecraft$VRPostChains.put(pass,
                        new PostChain(textureManager, resourceProvider, GuiHandler.GUI_FRAMEBUFFER, name));
                    continue;
                }

                // create one post chain for each active render pass
                if (WorldRenderPass.getByRenderPass(pass) == null) continue;
                this.vivecraft$VRPostChains.put(pass,
                    new PostChain(textureManager, resourceProvider, WorldRenderPass.getByRenderPass(pass).target,
                        name));
            }

            EnumMap<RenderPass, List<PostPass>> vrPasses = new EnumMap<>(RenderPass.class);
            for (Map.Entry<RenderPass, PostChain> entry : this.vivecraft$VRPostChains.entrySet()) {
                vrPasses.put(entry.getKey(), ((PostChainVRMixin) (Object) entry.getValue()).getPasses());
            }

            this.passes = new MultiPassList<>(this.passes, vrPasses);
        }
    }

    @Inject(method = "getTempTarget", at = @At("RETURN"), cancellable = true)
    private void vivecraft$getVRTempTarget(String attributeName, CallbackInfoReturnable<RenderTarget> cir) {
        if (cir.getReturnValue() != null && VRState.VR_INITIALIZED && !this.vivecraft$VRPostChains.isEmpty()) {
            cir.setReturnValue(new MultiPassRenderTarget(cir.getReturnValue(), pass -> {
                return this.vivecraft$VRPostChains.containsKey(pass) ?
                    this.vivecraft$VRPostChains.get(pass).getTempTarget(attributeName) : null;
            }));
        }
    }

    @ModifyVariable(method = "addTempTarget", at = @At(value = "STORE"), ordinal = 0)
    private RenderTarget vivecraft$vrTargetStencil(RenderTarget renderTarget) {
        if (((RenderTargetExtension) this.screenTarget).vivecraft$hasStencil()) {
            ((RenderTargetExtension) renderTarget).vivecraft$setStencil(true);
            renderTarget.resize(renderTarget.width, renderTarget.height, Minecraft.ON_OSX);
        }
        return renderTarget;
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void vivecraft$closeVRChains(CallbackInfo ci) {
        for (PostChain postChain : this.vivecraft$VRPostChains.values()) {
            postChain.close();
        }
        this.vivecraft$VRPostChains.clear();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void vivecraft$resizeVRChains(CallbackInfo ci) {
        for (Map.Entry<RenderPass, PostChain> entry : this.vivecraft$VRPostChains.entrySet()) {
            RenderTarget target = null;
            if (entry.getKey() == RenderPass.GUI) {
                target = GuiHandler.GUI_FRAMEBUFFER;
            } else {
                WorldRenderPass pass = WorldRenderPass.getByRenderPass(entry.getKey());
                if (pass != null) {
                    target = pass.target;
                }
            }
            if (target != null) {
                entry.getValue().resize(target.width, target.height);
            }
        }
    }

    @Inject(method = "process", at = @At(value = "HEAD"), cancellable = true)
    private void vivecraft$renderVRChain(float partialTick, CallbackInfo ci) {
        if (!RenderPassType.isVanilla() &&
            this.vivecraft$VRPostChains.containsKey(ClientDataHolderVR.getInstance().currentPass))
        {
            this.vivecraft$VRPostChains.get(ClientDataHolderVR.getInstance().currentPass).process(partialTick);
            ci.cancel();
        }
    }
}
