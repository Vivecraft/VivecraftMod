package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.graphics.OpenGLHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

@ClassDependentMixin("net.optifine.Config")
@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class GlCommandEncoderVRMixin {
    @Inject(method = "trySetup", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
    private void vivecraft$addWhiteTexture(
        CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) GlRenderPass renderpass)
    {
        if (VRState.VR_RUNNING && !RenderPassType.isGuiOnly() && OptifineHelper.isShaderActive() &&
            renderpass.samplers.isEmpty() && renderpass.pipeline != null &&
            renderpass.pipeline.info().getLocation().getNamespace().equals("vivecraft"))
        {
            // bind a white texture as fallback
            OpenGLHelper.bindTexture(0, RenderHelper.getGpuTexture(RenderHelper.WHITE_TEXTURE));
        }
    }
}
