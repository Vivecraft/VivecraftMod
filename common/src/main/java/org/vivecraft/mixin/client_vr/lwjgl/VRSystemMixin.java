package org.vivecraft.mixin.client_vr.lwjgl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.openvr.VRSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.utils.JNIUtils;

@Mixin(VRSystem.class)
public class VRSystemMixin {

    @WrapOperation(method = "nVRSystem_GetProjectionRaw", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPPV(IJJJJJ)V"))
    private static void vivecraft$nVRSystem_GetProjectionRaw(
        int eEye, long pfLeft, long pfRight, long pfTop, long pfBottom, long __functionAddress,
        Operation<Void> original)
    {
        JNIUtils.callV("UPPPP_V", __functionAddress, eEye, pfLeft, pfRight, pfTop, pfBottom);
    }

    @WrapOperation(method = "nVRSystem_GetTimeSinceLastVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(JJJ)Z"))
    private static boolean vivecraft$nVRSystem_GetTimeSinceLastVsync(
        long pfSecondsSinceLastVsync, long pulFrameCounter, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PP_Z", __functionAddress, pfSecondsSinceLastVsync, pulFrameCounter);
    }

    @WrapOperation(method = "VRSystem_IsDisplayOnDesktop", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callZ(J)Z"))
    private static boolean vivecraft$VRSystem_IsDisplayOnDesktop(long __functionAddress, Operation<Boolean> original) {
        return JNIUtils.callZ("_Z", __functionAddress);
    }

    @WrapOperation(method = "VRSystem_SetDisplayVisibility", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callZ(ZJ)Z"))
    private static boolean vivecraft$VRSystem_SetDisplayVisibility(
        boolean bIsVisibleOnDesktop, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("Z_Z", __functionAddress, bIsVisibleOnDesktop);
    }

    @WrapOperation(method = "nVRSystem_GetBoolTrackedDeviceProperty", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPZ(IIJJ)Z"))
    private static boolean vivecraft$nVRSystem_GetBoolTrackedDeviceProperty(
        int unDeviceIndex, int prop, long pError, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("UIP_Z", __functionAddress, unDeviceIndex, prop, pError);
    }

    @WrapOperation(method = "nVRSystem_GetStringTrackedDeviceProperty", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPI(IIJIJJ)I"))
    private static int vivecraft$nVRSystem_GetStringTrackedDeviceProperty(
        int unDeviceIndex, int prop, long pchValue, int unBufferSize, long pError, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("UIPUP_U", __functionAddress, unDeviceIndex, prop, pchValue, unBufferSize, pError);
    }

    @WrapOperation(method = "nVRSystem_PollNextEvent", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPZ(JIJ)Z"))
    private static boolean vivecraft$nVRSystem_PollNextEvent(
        long pEvent, int uncbVREvent, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PU_Z", __functionAddress, pEvent, uncbVREvent);
    }

    @WrapOperation(method = "nVRSystem_PollNextEventWithPose", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(IJIJJ)Z"))
    private static boolean vivecraft$nVRSystem_PollNextEventWithPose(
        int eOrigin, long pEvent, int uncbVREvent, long pTrackedDevicePose, long __functionAddress,
        Operation<Boolean> original)
    {
        return JNIUtils.callZ("IPUP_Z", __functionAddress, eOrigin, pEvent, uncbVREvent, pTrackedDevicePose);
    }

    @WrapOperation(method = "nVRSystem_GetControllerState", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPZ(IJIJ)Z"))
    private static boolean vivecraft$nVRSystem_GetControllerState(
        int unControllerDeviceIndex, long pControllerState, int unControllerStateSize, long __functionAddress,
        Operation<Boolean> original)
    {
        return JNIUtils.callZ("UPU_Z", __functionAddress, unControllerDeviceIndex, pControllerState,
            unControllerStateSize);
    }

    @WrapOperation(method = "nVRSystem_GetControllerStateWithPose", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(IIJIJJ)Z"))
    private static boolean vivecraft$nVRSystem_GetControllerStateWithPose(
        int eOrigin, int unControllerDeviceIndex, long pControllerState, int unControllerStateSize,
        long pTrackedDevicePose, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("IUUUP_Z", __functionAddress, eOrigin, unControllerDeviceIndex, pControllerState,
            unControllerStateSize, pTrackedDevicePose);
    }

    @WrapOperation(method = "VRSystem_TriggerHapticPulse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callCV(IISJ)V"))
    private static void vivecraft$VRSystem_TriggerHapticPulse(
        int unControllerDeviceIndex, int unAxisId, short usDurationMicroSec, long __functionAddress,
        Operation<Void> original)
    {
        JNIUtils.callV("UUS_Z", __functionAddress, unControllerDeviceIndex, unAxisId, usDurationMicroSec);
    }

    @WrapOperation(method = "VRSystem_IsInputAvailable", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callZ(J)Z"))
    private static boolean vivecraft$VRSystem_IsInputAvailable(long __functionAddress, Operation<Boolean> original) {
        return JNIUtils.callZ("_Z", __functionAddress);
    }

    @WrapOperation(method = "VRSystem_IsSteamVRDrawingControllers", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callZ(J)Z"))
    private static boolean vivecraft$VRSystem_IsSteamVRDrawingControllers(
        long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("_Z", __functionAddress);
    }

    @WrapOperation(method = "VRSystem_ShouldApplicationPause", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callZ(J)Z"))
    private static boolean vivecraft$VRSystem_ShouldApplicationPause(
        long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("_Z", __functionAddress);
    }

    @WrapOperation(method = "VRSystem_ShouldApplicationReduceRenderingWork", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callZ(J)Z"))
    private static boolean vivecraft$VRSystem_ShouldApplicationReduceRenderingWork(
        long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("_Z", __functionAddress);
    }
}
