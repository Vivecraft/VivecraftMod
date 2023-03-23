package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRInput_FnTable extends Structure
{
    public SetActionManifestPath_callback SetActionManifestPath;
    public GetActionSetHandle_callback GetActionSetHandle;
    public GetActionHandle_callback GetActionHandle;
    public GetInputSourceHandle_callback GetInputSourceHandle;
    public UpdateActionState_callback UpdateActionState;
    public GetDigitalActionData_callback GetDigitalActionData;
    public GetAnalogActionData_callback GetAnalogActionData;
    public GetPoseActionDataRelativeToNow_callback GetPoseActionDataRelativeToNow;
    public GetPoseActionDataForNextFrame_callback GetPoseActionDataForNextFrame;
    public GetSkeletalActionData_callback GetSkeletalActionData;
    public GetBoneCount_callback GetBoneCount;
    public GetBoneHierarchy_callback GetBoneHierarchy;
    public GetBoneName_callback GetBoneName;
    public GetSkeletalReferenceTransforms_callback GetSkeletalReferenceTransforms;
    public GetSkeletalTrackingLevel_callback GetSkeletalTrackingLevel;
    public GetSkeletalBoneData_callback GetSkeletalBoneData;
    public GetSkeletalSummaryData_callback GetSkeletalSummaryData;
    public GetSkeletalBoneDataCompressed_callback GetSkeletalBoneDataCompressed;
    public DecompressSkeletalBoneData_callback DecompressSkeletalBoneData;
    public TriggerHapticVibrationAction_callback TriggerHapticVibrationAction;
    public GetActionOrigins_callback GetActionOrigins;
    public GetOriginLocalizedName_callback GetOriginLocalizedName;
    public GetOriginTrackedDeviceInfo_callback GetOriginTrackedDeviceInfo;
    public ShowActionOrigins_callback ShowActionOrigins;
    public ShowBindingsForActionSet_callback ShowBindingsForActionSet;
    public IsUsingLegacyInput_callback IsUsingLegacyInput;

    public VR_IVRInput_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("SetActionManifestPath", "GetActionSetHandle", "GetActionHandle", "GetInputSourceHandle", "UpdateActionState", "GetDigitalActionData", "GetAnalogActionData", "GetPoseActionDataRelativeToNow", "GetPoseActionDataForNextFrame", "GetSkeletalActionData", "GetBoneCount", "GetBoneHierarchy", "GetBoneName", "GetSkeletalReferenceTransforms", "GetSkeletalTrackingLevel", "GetSkeletalBoneData", "GetSkeletalSummaryData", "GetSkeletalBoneDataCompressed", "DecompressSkeletalBoneData", "TriggerHapticVibrationAction", "GetActionOrigins", "GetOriginLocalizedName", "GetOriginTrackedDeviceInfo", "ShowActionOrigins", "ShowBindingsForActionSet", "IsUsingLegacyInput");
    }

    public VR_IVRInput_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRInput_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRInput_FnTable implements Structure.ByValue
    {
    }

    public interface DecompressSkeletalBoneData_callback extends Callback
    {
        int apply(Pointer var1, int var2, int var3, VRBoneTransform_t var4, int var5);
    }

    public interface GetActionHandle_callback extends Callback
    {
        int apply(Pointer var1, LongByReference var2);
    }

    public interface GetActionOrigins_callback extends Callback
    {
        int apply(long var1, long var3, LongByReference var5, int var6);
    }

    public interface GetActionSetHandle_callback extends Callback
    {
        int apply(Pointer var1, LongByReference var2);
    }

    public interface GetAnalogActionData_callback extends Callback
    {
        int apply(long var1, InputAnalogActionData_t var3, int var4, long var5);
    }

    public interface GetBoneCount_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface GetBoneHierarchy_callback extends Callback
    {
        int apply(long var1, IntByReference var3, int var4);
    }

    public interface GetBoneName_callback extends Callback
    {
        int apply(long var1, int var3, Pointer var4, int var5);
    }

    public interface GetDigitalActionData_callback extends Callback
    {
        int apply(long var1, InputDigitalActionData_t var3, int var4, long var5);
    }

    public interface GetInputSourceHandle_callback extends Callback
    {
        int apply(Pointer var1, LongByReference var2);
    }

    public interface GetOriginLocalizedName_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4, int var5);
    }

    public interface GetOriginTrackedDeviceInfo_callback extends Callback
    {
        int apply(long var1, InputOriginInfo_t var3, int var4);
    }

    public interface GetPoseActionDataForNextFrame_callback extends Callback
    {
        int apply(long var1, int var3, InputPoseActionData_t var4, int var5, long var6);
    }

    public interface GetPoseActionDataRelativeToNow_callback extends Callback
    {
        int apply(long var1, int var3, float var4, InputPoseActionData_t var5, int var6, long var7);
    }

    public interface GetSkeletalActionData_callback extends Callback
    {
        int apply(long var1, InputSkeletalActionData_t var3, int var4);
    }

    public interface GetSkeletalBoneDataCompressed_callback extends Callback
    {
        int apply(long var1, int var3, Pointer var4, int var5, IntByReference var6);
    }

    public interface GetSkeletalBoneData_callback extends Callback
    {
        int apply(long var1, int var3, int var4, VRBoneTransform_t var5, int var6);
    }

    public interface GetSkeletalReferenceTransforms_callback extends Callback
    {
        int apply(long var1, int var3, int var4, VRBoneTransform_t var5, int var6);
    }

    public interface GetSkeletalSummaryData_callback extends Callback
    {
        int apply(long var1, int var3, VRSkeletalSummaryData_t var4);
    }

    public interface GetSkeletalTrackingLevel_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface IsUsingLegacyInput_callback extends Callback
    {
        byte apply();
    }

    public interface SetActionManifestPath_callback extends Callback
    {
        int apply(Pointer var1);
    }

    public interface ShowActionOrigins_callback extends Callback
    {
        int apply(long var1, long var3);
    }

    public interface ShowBindingsForActionSet_callback extends Callback
    {
        int apply(VRActiveActionSet_t var1, int var2, int var3, long var4);
    }

    public interface TriggerHapticVibrationAction_callback extends Callback
    {
        int apply(long var1, float var3, float var4, float var5, float var6, long var7);
    }

    public interface UpdateActionState_callback extends Callback
    {
        int apply(VRActiveActionSet_t var1, int var2, int var3);
    }
}
