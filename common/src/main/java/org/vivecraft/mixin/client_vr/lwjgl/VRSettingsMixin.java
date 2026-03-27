package org.vivecraft.mixin.client_vr.lwjgl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.openvr.VRSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.utils.JNIUtils;

@Mixin(VRSettings.class)
public class VRSettingsMixin {

    @WrapOperation(method = "nVRSettings_SetBool", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPV(JJZJJ)V"))
    private static void vivecraft$nVRSettings_SetBool(
        long pchSection, long pchSettingsKey, boolean bValue, long peError, long __functionAddress,
        Operation<Void> original)
    {
        JNIUtils.callV("PPZP_V", __functionAddress, pchSection, pchSettingsKey, bValue, peError);
    }

    @WrapOperation(method = "nVRSettings_SetFloat", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPV(JJFJJ)V"))
    private static void vivecraft$nVRSettings_SetFloat(
        long pchSection, long pchSettingsKey, float flValue, long peError, long __functionAddress,
        Operation<Void> original)
    {
        JNIUtils.callV("PPFP_V", __functionAddress, pchSection, pchSettingsKey, flValue, peError);
    }

    @WrapOperation(method = "nVRSettings_GetBool", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPZ(JJJJ)Z"))
    private static boolean vivecraft$nVRSettings_GetBool(
        long pchSection, long pchSettingsKey, long peError, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PPP_Z", __functionAddress, pchSection, pchSettingsKey, peError);
    }

    @WrapOperation(method = "nVRSettings_GetFloat", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPF(JJJJ)F"))
    private static float vivecraft$nVRSettings_GetFloat(
        long pchSection, long pchSettingsKey, long peError, long __functionAddress, Operation<Float> original)
    {
        return JNIUtils.callF("PPP_F", __functionAddress, pchSection, pchSettingsKey, peError);
    }

    @WrapOperation(method = "nVRSettings_GetString", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPPV(JJJIJJ)V"))
    private static void vivecraft$nVRSettings_GetString(
        long pchSection, long pchSettingsKey, long pchValue, int unValueLen, long peError, long __functionAddress,
        Operation<Void> original)
    {
        JNIUtils.callV("PPPUP_V", __functionAddress, pchSection, pchSettingsKey, pchValue, unValueLen, peError);
    }
}
