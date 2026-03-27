package org.vivecraft.mod_compat_vr.voxy.mixin;

import me.cortex.voxy.client.core.IrisVoxyRenderPipeline;
import me.cortex.voxy.client.core.rendering.util.DepthFramebuffer;
import me.cortex.voxy.client.iris.IGetIrisVoxyPipelineData;
import me.cortex.voxy.client.iris.IrisVoxyRenderPipelineData;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;

import java.util.EnumMap;
import java.util.Optional;

import static org.lwjgl.opengl.GL45C.glNamedFramebufferDrawBuffers;
import static org.lwjgl.opengl.GL45C.glNamedFramebufferTexture;

@Pseudo
@Mixin(IrisVoxyRenderPipeline.class)
public class IrisVoxyRenderPipelineVRMixin extends AbstractRenderPipelineVRMixin {

    @Final
    @Mutable
    @Shadow
    public DepthFramebuffer fbTranslucent;

    @Final
    @Mutable
    @Shadow
    private IrisVoxyRenderPipelineData data;

    @Unique
    private final EnumMap<RenderPass, DepthFramebuffer> vivecraft$framebuffersOpaque = new EnumMap<>(RenderPass.class);

    @Unique
    private final EnumMap<RenderPass, DepthFramebuffer> vivecraft$framebuffersTranslucent = new EnumMap<>(
        RenderPass.class);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$storeFramebuffers(CallbackInfo ci) {
        this.vivecraft$framebuffersOpaque.put(ClientDataHolderVR.getInstance().currentPass, this.fb);
        this.vivecraft$framebuffersTranslucent.put(ClientDataHolderVR.getInstance().currentPass, this.fbTranslucent);
    }

    @Inject(method = "preSetup", at = @At("HEAD"))
    private void vivecraft$switchPipeline(CallbackInfo ci) {
        // make sure iris is actually loaded
        if (IrisHelper.isLoaded()) {
            Optional<?> irisPipe = IrisHelper.getPipeline();
            if (irisPipe.isPresent() && irisPipe.get() instanceof IGetIrisVoxyPipelineData getVoxyPipeData) {
                IrisVoxyRenderPipelineData pipeData = getVoxyPipeData.voxy$getPipelineData();
                if (this.data != pipeData) {
                    this.data = pipeData;
                    this.data.thePipeline = (IrisVoxyRenderPipeline) (Object) this;

                    if (!this.vivecraft$framebuffersOpaque.containsKey(ClientDataHolderVR.getInstance().currentPass)) {
                        this.fb = new DepthFramebuffer(this.fb.getFormat());
                        //Bind the drawbuffers
                        int[] oDT = this.data.opaqueDrawTargets;
                        int[] binding = new int[oDT.length];
                        for (int i = 0; i < oDT.length; i++) {
                            binding[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
                            glNamedFramebufferTexture(this.fb.framebuffer.id, GL30.GL_COLOR_ATTACHMENT0 + i, oDT[i], 0);
                        }
                        glNamedFramebufferDrawBuffers(this.fb.framebuffer.id, binding);
                        this.vivecraft$framebuffersOpaque.put(ClientDataHolderVR.getInstance().currentPass, this.fb);
                        this.fb.framebuffer.verify();
                    } else {
                        this.fb = this.vivecraft$framebuffersOpaque.get(ClientDataHolderVR.getInstance().currentPass);
                    }


                    if (!this.vivecraft$framebuffersTranslucent.containsKey(
                        ClientDataHolderVR.getInstance().currentPass))
                    {
                        this.fbTranslucent = new DepthFramebuffer(this.fbTranslucent.getFormat());
                        //Bind the drawbuffers
                        int[] tDT = this.data.translucentDrawTargets;
                        int[] binding = new int[tDT.length];
                        for (int i = 0; i < tDT.length; i++) {
                            binding[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
                            glNamedFramebufferTexture(this.fbTranslucent.framebuffer.id, GL30.GL_COLOR_ATTACHMENT0 + i,
                                tDT[i], 0);
                        }
                        glNamedFramebufferDrawBuffers(this.fbTranslucent.framebuffer.id, binding);
                        this.vivecraft$framebuffersTranslucent.put(ClientDataHolderVR.getInstance().currentPass,
                            this.fbTranslucent);
                        this.fbTranslucent.framebuffer.verify();
                    } else {
                        this.fbTranslucent = this.vivecraft$framebuffersTranslucent.get(
                            ClientDataHolderVR.getInstance().currentPass);
                    }
                }
            }
        }
    }

    @Inject(method = "free", at = @At("HEAD"))
    private void vivecraft$free(CallbackInfo ci) {
        for (DepthFramebuffer buffer : this.vivecraft$framebuffersOpaque.values()) {
            if (buffer != null && buffer != this.fb) {
                buffer.free();
            }
        }
        for (DepthFramebuffer buffer : this.vivecraft$framebuffersTranslucent.values()) {
            if (buffer != null && buffer != this.fbTranslucent) {
                buffer.free();
            }
        }
        this.vivecraft$framebuffersOpaque.clear();
        this.vivecraft$framebuffersTranslucent.clear();
    }
}
