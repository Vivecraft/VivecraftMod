package org.vivecraft.mixin.client_vr.lwjgl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.openvr.VRInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.utils.JNIUtils;

@Mixin(VRInput.class)
public class VRInputMixin {

    @WrapOperation(method = "nVRInput_GetDigitalActionData", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPJI(JJIJJ)I"))
    private static int vivecraft$nVRInput_GetDigitalActionData(
        long action, long pActionData, int unActionDataSize, long ulRestrictToDevice, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("JPUJ_I", __functionAddress, action, pActionData, unActionDataSize, ulRestrictToDevice);
    }

    @WrapOperation(method = "nVRInput_GetAnalogActionData", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPJI(JJIJJ)I"))
    private static int vivecraft$nVRInput_GetAnalogActionData(
        long action, long pActionData, int unActionDataSize, long ulRestrictToDevice, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("JPUJ_I", __functionAddress, action, pActionData, unActionDataSize, ulRestrictToDevice);
    }

    @WrapOperation(method = "nVRInput_GetPoseActionDataRelativeToNow", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPJI(JIFJIJJ)I"))
    private static int vivecraft$nVRInput_GetPoseActionDataRelativeToNow(
        long action, int eOrigin, float fPredictedSecondsFromNow, long pActionData, int unActionDataSize,
        long ulRestrictToDevice, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JIFPUJ_I", __functionAddress, action, eOrigin, fPredictedSecondsFromNow, pActionData,
            unActionDataSize, ulRestrictToDevice);
    }

    @WrapOperation(method = "nVRInput_GetPoseActionDataForNextFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPJI(JIJIJJ)I"))
    private static int vivecraft$nVRInput_GetPoseActionDataForNextFrame(
        long action, int eOrigin, long pActionData, int unActionDataSize, long ulRestrictToDevice,
        long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JIPUJ_I", __functionAddress, action, eOrigin, pActionData, unActionDataSize,
            ulRestrictToDevice);
    }

    @WrapOperation(method = "nVRInput_GetSkeletalActionData", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JJIJ)I"))
    private static int vivecraft$nVRInput_GetSkeletalActionData(
        long action, long pActionData, int unActionDataSize, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JPU_I", __functionAddress, action, pActionData, unActionDataSize);
    }

    @WrapOperation(method = "nVRInput_GetBoneCount", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JJJ)I"))
    private static int vivecraft$nVRInput_GetBoneCount(
        long action, long pBoneCount, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JP_I", __functionAddress, action, pBoneCount);
    }

    @WrapOperation(method = "nVRInput_GetBoneHierarchy", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JJIJ)I"))
    private static int vivecraft$nVRInput_GetBoneHierarchy(
        long action, long pParentIndices, int unIndexArrayCount, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JPU_I", __functionAddress, action, pParentIndices, unIndexArrayCount);
    }

    @WrapOperation(method = "nVRInput_GetBoneName", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JIJIJ)I"))
    private static int vivecraft$nVRInput_GetBoneName(
        long action, int nBoneIndex, long pchBoneName, int unNameBufferSize, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("JIPU_I", __functionAddress, action, nBoneIndex, pchBoneName, unNameBufferSize);
    }

    @WrapOperation(method = "nVRInput_GetSkeletalReferenceTransforms", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JIIJIJ)I"))
    private static int vivecraft$nVRInput_GetSkeletalReferenceTransforms(
        long action, int eTransformSpace, int eReferencePose, long pTransformArray, int unTransformArrayCount,
        long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JIIPU_I", __functionAddress, action, eTransformSpace, eReferencePose, pTransformArray,
            unTransformArrayCount);
    }

    @WrapOperation(method = "nVRInput_GetSkeletalTrackingLevel", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JJJ)I"))
    private static int vivecraft$nVRInput_GetSkeletalTrackingLevel(
        long action, long pSkeletalTrackingLevel, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JP_I", __functionAddress, action, pSkeletalTrackingLevel);
    }

    @WrapOperation(method = "nVRInput_GetSkeletalBoneData", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JIIJIJ)I"))
    private static int vivecraft$nVRInput_GetSkeletalBoneData(
        long action, int eTransformSpace, int eMotionRange, long pTransformArray, int unTransformArrayCount,
        long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JIIPU_I", __functionAddress, action, eTransformSpace, eMotionRange, pTransformArray,
            unTransformArrayCount);
    }

    @WrapOperation(method = "nVRInput_GetSkeletalSummaryData", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JIJJ)I"))
    private static int vivecraft$nVRInput_GetSkeletalSummaryData(
        long action, int eSummaryType, long pSkeletalSummaryData, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JIP_I", __functionAddress, action, eSummaryType, pSkeletalSummaryData);
    }

    @WrapOperation(method = "nVRInput_GetSkeletalBoneDataCompressed", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPPI(JIJIJJ)I"))
    private static int vivecraft$nVRInput_GetSkeletalBoneDataCompressed(
        long action, int eMotionRange, long pvCompressedData, int unCompressedSize, long punRequiredCompressedSize,
        long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JIPUP_I", __functionAddress, action, eMotionRange, pvCompressedData, unCompressedSize,
            punRequiredCompressedSize);
    }

    @WrapOperation(method = "VRInput_TriggerHapticVibrationAction", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJJI(JFFFFJJ)I"))
    private static int vivecraft$VRInput_TriggerHapticVibrationAction(
        long action, float fStartSecondsFromNow, float fDurationSeconds, float fFrequency, float fAmplitude,
        long ulRestrictToDevice, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JFFFFJ_I", __functionAddress, action, fStartSecondsFromNow, fDurationSeconds, fFrequency,
            fAmplitude, ulRestrictToDevice);
    }

    @WrapOperation(method = "nVRInput_GetActionOrigins", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJJPI(JJJIJ)I"))
    private static int vivecraft$nVRInput_GetActionOrigins(
        long actionSetHandle, long digitalActionHandle, long originsOut, int originOutCount, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("JJPU_I", __functionAddress, actionSetHandle, digitalActionHandle, originsOut,
            originOutCount);
    }

    @WrapOperation(method = "nVRInput_GetOriginLocalizedName", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JJIIJ)I"))
    private static int vivecraft$nVRInput_GetOriginLocalizedName(
        long origin, long pchNameArray, int unNameArraySize, int unStringSectionsToInclude, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("JPUI_I", __functionAddress, origin, pchNameArray, unNameArraySize,
            unStringSectionsToInclude);
    }

    @WrapOperation(method = "nVRInput_GetOriginTrackedDeviceInfo", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JJIJ)I"))
    private static int vivecraft$nVRInput_GetOriginTrackedDeviceInfo(
        long origin, long pOriginInfo, int unOriginInfoSize, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JPU_I", __functionAddress, origin, pOriginInfo, unOriginInfoSize);
    }

    @WrapOperation(method = "nVRInput_GetActionBindingInfo", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPPI(JJIIJJ)I"))
    private static int vivecraft$nVRInput_GetActionBindingInfo(
        long action, long pOriginInfo, int unBindingInfoSize, int unBindingInfoCount, long punReturnedBindingInfoCount,
        long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JPUUP_I", __functionAddress, action, pOriginInfo, unBindingInfoSize, unBindingInfoCount,
            punReturnedBindingInfoCount);
    }

    @WrapOperation(method = "VRInput_ShowActionOrigins", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJJI(JJJ)I"))
    private static int vivecraft$VRInput_ShowActionOrigins(
        long actionSetHandle, long ulActionHandle, long __functionAddress, Operation<Integer> original)
    {
        return JNIUtils.callI("JJ_I", __functionAddress, actionSetHandle, ulActionHandle);
    }

    @WrapOperation(method = "nVRInput_ShowBindingsForActionSet", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPJI(JIIJJ)I"))
    private static int vivecraft$nVRInput_ShowBindingsForActionSet(
        long pSets, int unSizeOfVRSelectedActionSet_t, int unSetCount, long originToHighlight, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("PUUJ_I", __functionAddress, pSets, unSizeOfVRSelectedActionSet_t, unSetCount,
            originToHighlight);
    }

    @WrapOperation(method = "nVRInput_OpenBindingUI", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callPJJI(JJJZJ)I"))
    private static int vivecraft$nVRInput_OpenBindingUI(
        long pchAppKey, long ulActionSetHandle, long ulDeviceHandle, boolean bShowOnDesktop, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("PJJZ_I", __functionAddress, pchAppKey, ulActionSetHandle, ulDeviceHandle,
            bShowOnDesktop);
    }

    @WrapOperation(method = "nVRInput_GetBindingVariant", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/JNI;callJPI(JJIJ)I"))
    private static int vivecraft$nVRInput_GetBindingVariant(
        long ulDevicePath, long pchVariantArray, int unVariantArraySize, long __functionAddress,
        Operation<Integer> original)
    {
        return JNIUtils.callI("JPU_I", __functionAddress, ulDevicePath, pchVariantArray, unVariantArraySize);
    }
}
