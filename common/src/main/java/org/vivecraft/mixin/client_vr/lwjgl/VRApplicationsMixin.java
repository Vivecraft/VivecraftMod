package org.vivecraft.mixin.client_vr.lwjgl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.openvr.VRApplications;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.utils.JNIUtils;

@Mixin(VRApplications.class)
public class VRApplicationsMixin {

    @WrapOperation(method = "nVRApplications_AddApplicationManifest", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPI(JZJ)I"))
    private static int vivecraft$nVRApplications_AddApplicationManifest(
        long pchApplicationManifestFullPath, boolean bTemporary, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callI("PZ_I", __functionAddress, pchApplicationManifestFullPath, bTemporary);
    }

    @WrapOperation(method = "nVRApplications_GetApplicationPropertyString", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPI(JIJIJJ)I"))
    private static int vivecraft$nVRApplications_GetApplicationPropertyString(
        long pchAppKey, int eProperty, long pchPropertyValueBuffer, int unPropertyValueBufferLen, long peError,
        long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callI("PIPUP_I", __functionAddress, pchAppKey, eProperty, pchPropertyValueBuffer,
            unPropertyValueBufferLen, peError);
    }

    @WrapOperation(method = "nVRApplications_GetApplicationPropertyBool", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(JIJJ)Z"))
    private static boolean vivecraft$nVRApplications_GetApplicationPropertyBool(
        long pchAppKey, int eProperty, long peError, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PIP_Z", __functionAddress, pchAppKey, eProperty, peError);
    }

    @WrapOperation(method = "nVRApplications_GetApplicationPropertyUint64", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPJ(JIJJ)J"))
    private static long vivecraft$nVRApplications_GetApplicationPropertyUint64(
        long pchAppKey, int eProperty, long peError, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callJ("PIP_J", __functionAddress, pchAppKey, eProperty, peError);
    }

    @WrapOperation(method = "nVRApplications_SetApplicationAutoLaunch", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPI(JZJ)I"))
    private static int vivecraft$nVRApplications_SetApplicationAutoLaunch(
        long pchAppKey, boolean bAutoLaunch, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callI("PZ_I", __functionAddress, pchAppKey, bAutoLaunch);
    }

    @WrapOperation(method = "nVRApplications_GetDefaultApplicationForMimeType", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(JJIJ)Z"))
    private static boolean vivecraft$nVRApplications_GetDefaultApplicationForMimeType(
        long pchMimeType, long pchAppKeyBuffer, int unAppKeyBufferLen, long __functionAddress,
        Operation<Boolean> original)
    {
        return JNIUtils.callZ("PPU_Z", __functionAddress, pchMimeType, pchAppKeyBuffer, unAppKeyBufferLen);
    }

    @WrapOperation(method = "nVRApplications_GetApplicationSupportedMimeTypes", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(JJIJ)Z"))
    private static boolean vivecraft$nVRApplications_GetApplicationSupportedMimeTypes(
        long pchAppKey, long pchMimeTypesBuffer, int unMimeTypesBuffer, long __functionAddress,
        Operation<Boolean> original)
    {
        return JNIUtils.callZ("PPU_Z", __functionAddress, pchAppKey, pchMimeTypesBuffer, unMimeTypesBuffer);
    }
}
