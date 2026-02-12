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

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Optional;

import static org.lwjgl.opengl.GL45C.glNamedFramebufferDrawBuffers;
import static org.lwjgl.opengl.GL45C.glNamedFramebufferTexture;

@Pseudo
@Mixin(IrisVoxyRenderPipeline.class)
public class IrisVoxyRenderPipelineVRMixin {

    @Final
    @Mutable
    @Shadow(remap = false)
    public DepthFramebuffer fbTranslucent;

    @Final
    @Mutable
    @Shadow(remap = false)
    private IrisVoxyRenderPipelineData data;

    @Unique
    private final EnumMap<RenderPass, DepthFramebuffer> vivecraft$framebuffersOpaque = new EnumMap<>(RenderPass.class);

    @Unique
    private final EnumMap<RenderPass, DepthFramebuffer> vivecraft$framebuffersTranslucent = new EnumMap<>(
        RenderPass.class);

    // need to use reflection because the field is in different spots in 0.2.5 and 0.2.6+
    @Unique
    private Field vivecraft$fbAccess;
    @Unique
    private Field vivecraft$fbType;

    @Unique
    private DepthFramebuffer vivecraft$getFb() {
        try {
            return (DepthFramebuffer) this.vivecraft$fbAccess.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Unique
    private void vivecraft$setFb(DepthFramebuffer newFb) {
        try {
            this.vivecraft$fbAccess.set(this, newFb);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Unique
    private int vivecraft$getFormat(DepthFramebuffer buffer) {
        try {
            DepthFramebuffer.class.getMethod("getFormat");
            return buffer.getFormat();
        } catch (NoSuchMethodException e) {
            if (this.vivecraft$fbType == null) {
                try {
                    this.vivecraft$fbType = DepthFramebuffer.class.getDeclaredField("depthType");
                    this.vivecraft$fbType.setAccessible(true);
                } catch (NoSuchFieldException ex) {
                    throw new RuntimeException(ex);
                }
            }
            try {
                return (int) this.vivecraft$fbType.get(buffer);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void vivecraft$storeFramebuffers(CallbackInfo ci) {
        try {
            this.vivecraft$fbAccess = this.getClass().getField("fb");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        this.vivecraft$framebuffersOpaque.put(ClientDataHolderVR.getInstance().currentPass, this.vivecraft$getFb());
        this.vivecraft$framebuffersTranslucent.put(ClientDataHolderVR.getInstance().currentPass, this.fbTranslucent);
    }

    @Inject(method = "preSetup", at = @At("HEAD"), remap = false)
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
                        this.vivecraft$setFb(new DepthFramebuffer(vivecraft$getFormat(this.vivecraft$getFb())));
                        //Bind the drawbuffers
                        int[] oDT = this.data.opaqueDrawTargets;
                        int[] binding = new int[oDT.length];
                        for (int i = 0; i < oDT.length; i++) {
                            binding[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
                            glNamedFramebufferTexture(this.vivecraft$getFb().framebuffer.id,
                                GL30.GL_COLOR_ATTACHMENT0 + i, oDT[i], 0);
                        }
                        glNamedFramebufferDrawBuffers(this.vivecraft$getFb().framebuffer.id, binding);
                        this.vivecraft$framebuffersOpaque.put(ClientDataHolderVR.getInstance().currentPass,
                            this.vivecraft$getFb());
                        this.vivecraft$getFb().framebuffer.verify();
                    } else {
                        this.vivecraft$setFb(
                            this.vivecraft$framebuffersOpaque.get(ClientDataHolderVR.getInstance().currentPass));
                    }


                    if (!this.vivecraft$framebuffersTranslucent.containsKey(
                        ClientDataHolderVR.getInstance().currentPass))
                    {
                        this.fbTranslucent = new DepthFramebuffer(vivecraft$getFormat(this.fbTranslucent));
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

    @Inject(method = "free", at = @At("HEAD"), remap = false)
    private void vivecraft$free(CallbackInfo ci) {
        for (DepthFramebuffer buffer : this.vivecraft$framebuffersOpaque.values()) {
            if (buffer != null && buffer != this.vivecraft$getFb()) {
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
