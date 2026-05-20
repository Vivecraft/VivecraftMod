package org.vivecraft.mixin.client_vr.lwjgl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.openvr.VRChaperone;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.utils.JNIUtils;

@Mixin(VRChaperone.class)
public class VRChaperoneMixin {

    @WrapOperation(method = "nVRChaperone_GetPlayAreaSize", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(JJJ)Z"))
    private static boolean vivecraft$nVRChaperone_GetPlayAreaSize(
        long pSizeX, long pSizeZ, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PP_Z", __functionAddress, pSizeX, pSizeZ);
    }

    @WrapOperation(method = "nVRChaperone_GetBoundsColor", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPV(JIFJJ)V"))
    private static void vivecraft$nVRChaperone_GetBoundsColor(
        long pOutputColorArray, int nNumOutputColors, float flCollisionBoundsFadeDistance, long pOutputCameraColor,
        long __functionAddress, Operation<Void> original)
    {
        JNIUtils.callV("PUFP_V", __functionAddress, pOutputColorArray, nNumOutputColors, flCollisionBoundsFadeDistance,
            pOutputCameraColor);
    }

    @WrapOperation(method = "VRChaperone_AreBoundsVisible", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callZ(J)Z"))
    private static boolean vivecraft$VRChaperone_AreBoundsVisible(
        long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("_Z", __functionAddress);
    }
}
