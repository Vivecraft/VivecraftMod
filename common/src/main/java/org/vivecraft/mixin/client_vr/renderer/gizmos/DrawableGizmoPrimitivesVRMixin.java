package org.vivecraft.mixin.client_vr.renderer.gizmos;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.VRState;

@Mixin(targets = "net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives$Group")
public class DrawableGizmoPrimitivesVRMixin {
    @ModifyExpressionValue(method = "renderTexts", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraRenderState;initialized:Z"))
    private boolean vivecraft$renderTextsInMenu(boolean initialized) {
        return initialized || VRState.VR_RUNNING;
    }
}
