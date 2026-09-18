package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.backend.opengl.Uniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.graphics.OpenGLHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.optifine.extensions.GlRenderPipelineExtension;

@ClassDependentMixin("net.optifine.Config")
@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlCommandEncoder")
public class GlCommandEncoderVRMixin {
    @Inject(method = "setupDraw", at = @At("TAIL"))
    private void vivecraft$addWhiteTexture(CallbackInfo ci, @Local(argsOnly = true) GlRenderPass renderpass) {

        if (VRState.VR_RUNNING && !RenderPassType.isGuiOnly() && OptifineHelper.isShaderActive() &&
            renderpass.pipeline != null)
        {
            if (((GlRenderPipelineExtension) (Object) renderpass.pipeline).vivecraft$getName()
                .startsWith("vivecraft:"))
            {
                boolean hasSampler = false;
                for (int i = 0; i < renderpass.pipeline.program().uniformCount(); i++) {
                    if (renderpass.pipeline.program().getUniform(i) instanceof Uniform.Sampler) {
                        hasSampler = true;
                    }
                }
                if (!hasSampler) {
                    // bind a white texture as fallback
                    OpenGLHelper.bindTexture(0, RenderHelper.getGpuTexture(RenderHelper.WHITE_TEXTURE));
                }
            }
        }
    }
}
