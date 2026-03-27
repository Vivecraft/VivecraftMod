package org.vivecraft.mod_compat_vr.iris.mixin;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.uniforms.CameraUniforms$CameraPositionTracker",
    "net.irisshaders.iris.uniforms.CameraUniforms$CameraPositionTracker"})
public class IrisCameraPositionTrackerMixin {

    @Shadow
    private Vector3d currentCameraPosition = new Vector3d();

    @Inject(method = "update", at = @At("TAIL"))
    private void vivecraft$capturePosition(CallbackInfo ci) {
        if (!VRState.VR_RUNNING || ClientDataHolderVR.getInstance().isFirstPass || ShadersHelper.isSlowMode()) {
            ShadersHelper.setShadowCameraPosition(false, (float) this.currentCameraPosition.x,
                (float) this.currentCameraPosition.y, (float) this.currentCameraPosition.z);
        }
    }
}
