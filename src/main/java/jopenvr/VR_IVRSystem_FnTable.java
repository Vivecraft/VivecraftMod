package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVRSystem_FnTable extends Structure
{
    public VR_IVRSystem_FnTable.GetRecommendedRenderTargetSize_callback GetRecommendedRenderTargetSize;
    public VR_IVRSystem_FnTable.GetProjectionMatrix_callback GetProjectionMatrix;
    public VR_IVRSystem_FnTable.GetProjectionRaw_callback GetProjectionRaw;
    public VR_IVRSystem_FnTable.ComputeDistortion_callback ComputeDistortion;
    public VR_IVRSystem_FnTable.GetEyeToHeadTransform_callback GetEyeToHeadTransform;
    public VR_IVRSystem_FnTable.GetTimeSinceLastVsync_callback GetTimeSinceLastVsync;
    public VR_IVRSystem_FnTable.GetD3D9AdapterIndex_callback GetD3D9AdapterIndex;
    public VR_IVRExtendedDisplay_FnTable.GetDXGIOutputInfo_callback GetDXGIOutputInfo;
    public VR_IVRSystem_FnTable.GetOutputDevice_callback GetOutputDevice;
    public VR_IVRSystem_FnTable.IsDisplayOnDesktop_callback IsDisplayOnDesktop;
    public VR_IVRSystem_FnTable.SetDisplayVisibility_callback SetDisplayVisibility;
    public VR_IVRSystem_FnTable.GetDeviceToAbsoluteTrackingPose_callback GetDeviceToAbsoluteTrackingPose;
    public VR_IVRSystem_FnTable.ResetSeatedZeroPose_callback ResetSeatedZeroPose;
    public VR_IVRSystem_FnTable.GetSeatedZeroPoseToStandingAbsoluteTrackingPose_callback GetSeatedZeroPoseToStandingAbsoluteTrackingPose;
    public VR_IVRSystem_FnTable.GetRawZeroPoseToStandingAbsoluteTrackingPose_callback GetRawZeroPoseToStandingAbsoluteTrackingPose;
    public VR_IVRSystem_FnTable.GetSortedTrackedDeviceIndicesOfClass_callback GetSortedTrackedDeviceIndicesOfClass;
    public VR_IVRSystem_FnTable.GetTrackedDeviceActivityLevel_callback GetTrackedDeviceActivityLevel;
    public VR_IVRSystem_FnTable.ApplyTransform_callback ApplyTransform;
    public VR_IVRSystem_FnTable.GetTrackedDeviceIndexForControllerRole_callback GetTrackedDeviceIndexForControllerRole;
    public VR_IVRSystem_FnTable.GetControllerRoleForTrackedDeviceIndex_callback GetControllerRoleForTrackedDeviceIndex;
    public VR_IVRSystem_FnTable.GetTrackedDeviceClass_callback GetTrackedDeviceClass;
    public VR_IVRSystem_FnTable.IsTrackedDeviceConnected_callback IsTrackedDeviceConnected;
    public VR_IVRSystem_FnTable.GetBoolTrackedDeviceProperty_callback GetBoolTrackedDeviceProperty;
    public VR_IVRSystem_FnTable.GetFloatTrackedDeviceProperty_callback GetFloatTrackedDeviceProperty;
    public VR_IVRSystem_FnTable.GetInt32TrackedDeviceProperty_callback GetInt32TrackedDeviceProperty;
    public VR_IVRSystem_FnTable.GetUint64TrackedDeviceProperty_callback GetUint64TrackedDeviceProperty;
    public VR_IVRSystem_FnTable.GetMatrix34TrackedDeviceProperty_callback GetMatrix34TrackedDeviceProperty;
    public VR_IVRSystem_FnTable.GetArrayTrackedDeviceProperty_callback GetArrayTrackedDeviceProperty;
    public VR_IVRSystem_FnTable.GetStringTrackedDeviceProperty_callback GetStringTrackedDeviceProperty;
    public VR_IVRSystem_FnTable.GetPropErrorNameFromEnum_callback GetPropErrorNameFromEnum;
    public VR_IVRSystem_FnTable.PollNextEvent_callback PollNextEvent;
    public VR_IVRSystem_FnTable.PollNextEventWithPose_callback PollNextEventWithPose;
    public VR_IVRSystem_FnTable.GetEventTypeNameFromEnum_callback GetEventTypeNameFromEnum;
    public VR_IVRSystem_FnTable.GetHiddenAreaMesh_callback GetHiddenAreaMesh;
    public VR_IVRSystem_FnTable.GetControllerState_callback GetControllerState;
    public VR_IVRSystem_FnTable.GetControllerStateWithPose_callback GetControllerStateWithPose;
    public VR_IVRSystem_FnTable.TriggerHapticPulse_callback TriggerHapticPulse;
    public VR_IVRSystem_FnTable.GetButtonIdNameFromEnum_callback GetButtonIdNameFromEnum;
    public VR_IVRSystem_FnTable.GetControllerAxisTypeNameFromEnum_callback GetControllerAxisTypeNameFromEnum;
    public VR_IVRSystem_FnTable.IsInputAvailable_callback IsInputAvailable;
    public VR_IVRSystem_FnTable.IsSteamVRDrawingControllers_callback IsSteamVRDrawingControllers;
    public VR_IVRSystem_FnTable.ShouldApplicationPause_callback ShouldApplicationPause;
    public VR_IVRSystem_FnTable.ShouldApplicationReduceRenderingWork_callback ShouldApplicationReduceRenderingWork;
    public VR_IVRSystem_FnTable.DriverDebugRequest_callback DriverDebugRequest;
    public VR_IVRSystem_FnTable.PerformFirmwareUpdate_callback PerformFirmwareUpdate;
    public VR_IVRSystem_FnTable.AcknowledgeQuit_Exiting_callback AcknowledgeQuit_Exiting;
    public VR_IVRSystem_FnTable.AcknowledgeQuit_UserPrompt_callback AcknowledgeQuit_UserPrompt;

    public VR_IVRSystem_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetRecommendedRenderTargetSize", "GetProjectionMatrix", "GetProjectionRaw", "ComputeDistortion", "GetEyeToHeadTransform", "GetTimeSinceLastVsync", "GetD3D9AdapterIndex", "GetDXGIOutputInfo", "GetOutputDevice", "IsDisplayOnDesktop", "SetDisplayVisibility", "GetDeviceToAbsoluteTrackingPose", "ResetSeatedZeroPose", "GetSeatedZeroPoseToStandingAbsoluteTrackingPose", "GetRawZeroPoseToStandingAbsoluteTrackingPose", "GetSortedTrackedDeviceIndicesOfClass", "GetTrackedDeviceActivityLevel", "ApplyTransform", "GetTrackedDeviceIndexForControllerRole", "GetControllerRoleForTrackedDeviceIndex", "GetTrackedDeviceClass", "IsTrackedDeviceConnected", "GetBoolTrackedDeviceProperty", "GetFloatTrackedDeviceProperty", "GetInt32TrackedDeviceProperty", "GetUint64TrackedDeviceProperty", "GetMatrix34TrackedDeviceProperty", "GetArrayTrackedDeviceProperty", "GetStringTrackedDeviceProperty", "GetPropErrorNameFromEnum", "PollNextEvent", "PollNextEventWithPose", "GetEventTypeNameFromEnum", "GetHiddenAreaMesh", "GetControllerState", "GetControllerStateWithPose", "TriggerHapticPulse", "GetButtonIdNameFromEnum", "GetControllerAxisTypeNameFromEnum", "IsInputAvailable", "IsSteamVRDrawingControllers", "ShouldApplicationPause", "ShouldApplicationReduceRenderingWork", "DriverDebugRequest", "PerformFirmwareUpdate", "AcknowledgeQuit_Exiting", "AcknowledgeQuit_UserPrompt");
    }

    public VR_IVRSystem_FnTable(Pointer peer)
    {
        super(peer);
    }

    public interface AcknowledgeQuit_Exiting_callback extends Callback
    {
        void apply();
    }

    public interface AcknowledgeQuit_UserPrompt_callback extends Callback
    {
        void apply();
    }

    public interface ApplyTransform_callback extends Callback
    {
        void apply(TrackedDevicePose_t var1, TrackedDevicePose_t var2, HmdMatrix34_t var3);
    }

    public static class ByReference extends VR_IVRSystem_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRSystem_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface ComputeDistortion_callback extends Callback
    {
        byte apply(int var1, float var2, float var3, DistortionCoordinates_t var4);
    }

    public interface DriverDebugRequest_callback extends Callback
    {
        int apply(int var1, Pointer var2, Pointer var3, int var4);
    }

    public interface GetArrayTrackedDeviceProperty_callback extends Callback
    {
        int apply(int var1, int var2, int var3, Pointer var4, int var5, IntByReference var6);
    }

    public interface GetBoolTrackedDeviceProperty_callback extends Callback
    {
        byte apply(int var1, int var2, IntByReference var3);
    }

    public interface GetButtonIdNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetControllerAxisTypeNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetControllerRoleForTrackedDeviceIndex_callback extends Callback
    {
        int apply(int var1);
    }

    public interface GetControllerStateWithPose_callback extends Callback
    {
        byte apply(int var1, int var2, VRControllerState_t var3, int var4, TrackedDevicePose_t var5);
    }

    public interface GetControllerState_callback extends Callback
    {
        byte apply(int var1, VRControllerState_t var2, int var3);
    }

    public interface GetD3D9AdapterIndex_callback extends Callback
    {
        int apply();
    }

    public interface GetDXGIOutputInfo_callback extends Callback
    {
        void apply(IntByReference var1);
    }

    public interface GetDeviceToAbsoluteTrackingPose_callback extends Callback
    {
        void apply(int var1, float var2, TrackedDevicePose_t var3, int var4);
    }

    public interface GetEventTypeNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetEyeToHeadTransform_callback extends Callback
    {
        HmdMatrix34_t.ByValue apply(int var1);
    }

    public interface GetFloatTrackedDeviceProperty_callback extends Callback
    {
        float apply(int var1, int var2, IntByReference var3);
    }

    public interface GetHiddenAreaMesh_callback extends Callback
    {
        HiddenAreaMesh_t.ByValue apply(int var1, int var2);
    }

    public interface GetInt32TrackedDeviceProperty_callback extends Callback
    {
        int apply(int var1, int var2, IntByReference var3);
    }

    public interface GetMatrix34TrackedDeviceProperty_callback extends Callback
    {
        HmdMatrix34_t.ByValue apply(int var1, int var2, IntByReference var3);
    }

    public interface GetOutputDevice_callback extends Callback
    {
        void apply(LongByReference var1, int var2, JOpenVRLibrary.VkInstance_T var3);
    }

    public interface GetProjectionMatrix_callback extends Callback
    {
        HmdMatrix44_t.ByValue apply(int var1, float var2, float var3);
    }

    public interface GetProjectionRaw_callback extends Callback
    {
        void apply(int var1, FloatByReference var2, FloatByReference var3, FloatByReference var4, FloatByReference var5);
    }

    public interface GetPropErrorNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetRawZeroPoseToStandingAbsoluteTrackingPose_callback extends Callback
    {
        HmdMatrix34_t.ByValue apply();
    }

    public interface GetRecommendedRenderTargetSize_callback extends Callback
    {
        void apply(IntByReference var1, IntByReference var2);
    }

    public interface GetSeatedZeroPoseToStandingAbsoluteTrackingPose_callback extends Callback
    {
        HmdMatrix34_t.ByValue apply();
    }

    public interface GetSortedTrackedDeviceIndicesOfClass_callback extends Callback
    {
        int apply(int var1, IntByReference var2, int var3, int var4);
    }

    public interface GetStringTrackedDeviceProperty_callback extends Callback
    {
        int apply(int var1, int var2, Pointer var3, int var4, IntByReference var5);
    }

    public interface GetTimeSinceLastVsync_callback extends Callback
    {
        byte apply(FloatByReference var1, LongByReference var2);
    }

    public interface GetTrackedDeviceActivityLevel_callback extends Callback
    {
        int apply(int var1);
    }

    public interface GetTrackedDeviceClass_callback extends Callback
    {
        int apply(int var1);
    }

    public interface GetTrackedDeviceIndexForControllerRole_callback extends Callback
    {
        int apply(int var1);
    }

    public interface GetUint64TrackedDeviceProperty_callback extends Callback
    {
        long apply(int var1, int var2, IntByReference var3);
    }

    public interface IsDisplayOnDesktop_callback extends Callback
    {
        byte apply();
    }

    public interface IsInputAvailable_callback extends Callback
    {
        byte apply();
    }

    public interface IsSteamVRDrawingControllers_callback extends Callback
    {
        byte apply();
    }

    public interface IsTrackedDeviceConnected_callback extends Callback
    {
        byte apply(int var1);
    }

    public interface PerformFirmwareUpdate_callback extends Callback
    {
        int apply(int var1);
    }

    public interface PollNextEventWithPose_callback extends Callback
    {
        byte apply(int var1, VREvent_t var2, int var3, TrackedDevicePose_t var4);
    }

    public interface PollNextEvent_callback extends Callback
    {
        byte apply(VREvent_t var1, int var2);
    }

    public interface ResetSeatedZeroPose_callback extends Callback
    {
        void apply();
    }

    public interface SetDisplayVisibility_callback extends Callback
    {
        byte apply(byte var1);
    }

    public interface ShouldApplicationPause_callback extends Callback
    {
        byte apply();
    }

    public interface ShouldApplicationReduceRenderingWork_callback extends Callback
    {
        byte apply();
    }

    public interface TriggerHapticPulse_callback extends Callback
    {
        void apply(int var1, int var2, short var3);
    }
}
