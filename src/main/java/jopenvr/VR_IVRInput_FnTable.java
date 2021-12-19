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
    public VR_IVRInput_FnTable.SetActionManifestPath_callback SetActionManifestPath;
    public VR_IVRInput_FnTable.GetActionSetHandle_callback GetActionSetHandle;
    public VR_IVRInput_FnTable.GetActionHandle_callback GetActionHandle;
    public VR_IVRInput_FnTable.GetInputSourceHandle_callback GetInputSourceHandle;
    public VR_IVRInput_FnTable.UpdateActionState_callback UpdateActionState;
    public VR_IVRInput_FnTable.GetDigitalActionData_callback GetDigitalActionData;
    public VR_IVRInput_FnTable.GetAnalogActionData_callback GetAnalogActionData;
    public VR_IVRInput_FnTable.GetPoseActionDataRelativeToNow_callback GetPoseActionDataRelativeToNow;
    public VR_IVRInput_FnTable.GetPoseActionDataForNextFrame_callback GetPoseActionDataForNextFrame;
    public VR_IVRInput_FnTable.GetSkeletalActionData_callback GetSkeletalActionData;
    public VR_IVRInput_FnTable.GetBoneCount_callback GetBoneCount;
    public VR_IVRInput_FnTable.GetBoneHierarchy_callback GetBoneHierarchy;
    public VR_IVRInput_FnTable.GetBoneName_callback GetBoneName;
    public VR_IVRInput_FnTable.GetSkeletalReferenceTransforms_callback GetSkeletalReferenceTransforms;
    public VR_IVRInput_FnTable.GetSkeletalTrackingLevel_callback GetSkeletalTrackingLevel;
    public VR_IVRInput_FnTable.GetSkeletalBoneData_callback GetSkeletalBoneData;
    public VR_IVRInput_FnTable.GetSkeletalSummaryData_callback GetSkeletalSummaryData;
    public VR_IVRInput_FnTable.GetSkeletalBoneDataCompressed_callback GetSkeletalBoneDataCompressed;
    public VR_IVRInput_FnTable.DecompressSkeletalBoneData_callback DecompressSkeletalBoneData;
    public VR_IVRInput_FnTable.TriggerHapticVibrationAction_callback TriggerHapticVibrationAction;
    public VR_IVRInput_FnTable.GetActionOrigins_callback GetActionOrigins;
    public VR_IVRInput_FnTable.GetOriginLocalizedName_callback GetOriginLocalizedName;
    public VR_IVRInput_FnTable.GetOriginTrackedDeviceInfo_callback GetOriginTrackedDeviceInfo;
    public VR_IVRInput_FnTable.ShowActionOrigins_callback ShowActionOrigins;
    public VR_IVRInput_FnTable.ShowBindingsForActionSet_callback ShowBindingsForActionSet;
    public VR_IVRInput_FnTable.IsUsingLegacyInput_callback IsUsingLegacyInput;

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

    public static class ByReference extends VR_IVRInput_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRInput_FnTable implements com.sun.jna.Structure.ByValue
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
