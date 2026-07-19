package org.vivecraft.mixin.client_vr.renderer.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.feature.GizmoFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.VRState;

@Mixin(GizmoFeatureRenderer.class)
public class GizmoFeatureRendererVRMixin {
    @ModifyExpressionValue(method = "buildTexts", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraRenderState;initialized:Z"))
    private boolean vivecraft$renderTextsInMenu(boolean initialized) {
        return initialized || VRState.VR_RUNNING;
    }
}
