package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client.extensions.RenderTargetExtension;

@Mixin(PostChain.class)
public class PostChainVRMixin {

    @Shadow
    @Final
    private RenderTarget screenTarget;

    @ModifyVariable(method = "addTempTarget", at = @At(value = "STORE"), ordinal = 0)
    private RenderTarget vivecraft$vrTargetStencil(RenderTarget renderTarget) {
        if (((RenderTargetExtension) this.screenTarget).vivecraft$hasStencil()) {
            ((RenderTargetExtension) renderTarget).vivecraft$setStencil(true);
            renderTarget.resize(renderTarget.width, renderTarget.height, Minecraft.ON_OSX);
        }
        return renderTarget;
    }
}
