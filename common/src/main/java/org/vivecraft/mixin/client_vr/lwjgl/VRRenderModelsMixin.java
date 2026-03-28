package org.vivecraft.mixin.client_vr.lwjgl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.openvr.VRRenderModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.utils.JNIUtils;

@Mixin(VRRenderModels.class)
public class VRRenderModelsMixin {

    @WrapOperation(method = "nVRRenderModels_GetComponentStateForDevicePath", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPJPPZ(JJJJJJ)Z"))
    private static boolean vivecraft$nVRRenderModels_GetComponentStateForDevicePath(
        long pchRenderModelName, long pchComponentName, long devicePath, long pState, long pComponentState,
        long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PPJPP_Z", __functionAddress, pchRenderModelName, pchComponentName, devicePath, pState,
            pComponentState);
    }

    @WrapOperation(method = "nVRRenderModels_GetComponentState", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPPPPZ(JJJJJJ)Z"))
    private static boolean vivecraft$nVRRenderModels_GetComponentState(
        long pchRenderModelName, long pchComponentName, long pControllerState, long pState, long pComponentState,
        long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PPPPP_Z", __functionAddress, pchRenderModelName, pchComponentName, pControllerState,
            pState, pComponentState);
    }

    @WrapOperation(method = "nVRRenderModels_RenderModelHasComponent", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPPZ(JJJ)Z"))
    private static boolean vivecraft$nVRRenderModels_RenderModelHasComponent(
        long pchRenderModelName, long pchComponentName, long __functionAddress, Operation<Boolean> original)
    {
        return JNIUtils.callZ("PP_Z", __functionAddress, pchRenderModelName, pchComponentName);
    }
}
