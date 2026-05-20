package org.vivecraft.mod_compat_vr.voxy.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.VoxyRenderSystem")
public abstract class VoxyRenderSystemVRMixin {
    @ModifyExpressionValue(method = "computeProjectionMat", at = @At(value = "CONSTANT", args = "floatValue=0.05"))
    private static float vivecraft$nearPlane(float original) {
        return RenderPassType.isVanilla() ? original : 0.02F;
    }
}
