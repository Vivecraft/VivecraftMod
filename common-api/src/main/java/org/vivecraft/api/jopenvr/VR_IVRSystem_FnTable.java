package org.vivecraft.api.jopenvr;

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
    public GetRecommendedRenderTargetSize_callback GetRecommendedRenderTargetSize;
    public GetProjectionMatrix_callback GetProjectionMatrix;
    public GetProjectionRaw_callback GetProjectionRaw;
    public ComputeDistortion_callback ComputeDistortion;
    public GetEyeToHeadTransform_callback GetEyeToHeadTransform;
    public GetTimeSinceLastVsync_callback GetTimeSinceLastVsync;
    public GetD3D9AdapterIndex_callback GetD3D9AdapterIndex;
    public VR_IVRExtendedDisplay_FnTable.GetDXGIOutputInfo_callback GetDXGIOutputInfo;
    public GetOutputDevice_callback GetOutputDevice;
    public IsDisplayOnDesktop_callback IsDisplayOnDesktop;
    public SetDisplayVisibility_callback SetDisplayVisibility;
    public GetDeviceToAbsoluteTrackingPose_callback GetDeviceToAbsoluteTrackingPose;
    public ResetSeatedZeroPose_callback ResetSeatedZeroPose;
    public GetSeatedZeroPoseToStandingAbsoluteTrackingPose_callback GetSeatedZeroPoseToStandingAbsoluteTrackingPose;
    public GetRawZeroPoseToStandingAbsoluteTrackingPose_callback GetRawZeroPoseToStandingAbsoluteTrackingPose;
    public GetSortedTrackedDeviceIndicesOfClass_callback GetSortedTrackedDeviceIndicesOfClass;
    public GetTrackedDeviceActivityLevel_callback GetTrackedDeviceActivityLevel;
    public ApplyTransform_callback ApplyTransform;
    public GetTrackedDeviceIndexForControllerRole_callback GetTrackedDeviceIndexForControllerRole;
    public GetControllerRoleForTrackedDeviceIndex_callback GetControllerRoleForTrackedDeviceIndex;
    public GetTrackedDeviceClass_callback GetTrackedDeviceClass;
    public IsTrackedDeviceConnected_callback IsTrackedDeviceConnected;
    public GetBoolTrackedDeviceProperty_callback GetBoolTrackedDeviceProperty;
    public GetFloatTrackedDeviceProperty_callback GetFloatTrackedDeviceProperty;
    public GetInt32TrackedDeviceProperty_callback GetInt32TrackedDeviceProperty;
    public GetUint64TrackedDeviceProperty_callback GetUint64TrackedDeviceProperty;
    public GetMatrix34TrackedDeviceProperty_callback GetMatrix34TrackedDeviceProperty;
    public GetArrayTrackedDeviceProperty_callback GetArrayTrackedDeviceProperty;
    public GetStringTrackedDeviceProperty_callback GetStringTrackedDeviceProperty;
    public GetPropErrorNameFromEnum_callback GetPropErrorNameFromEnum;
    public PollNextEvent_callback PollNextEvent;
    public PollNextEventWithPose_callback PollNextEventWithPose;
    public GetEventTypeNameFromEnum_callback GetEventTypeNameFromEnum;
    public GetHiddenAreaMesh_callback GetHiddenAreaMesh;
    public GetControllerState_callback GetControllerState;
    public GetControllerStateWithPose_callback GetControllerStateWithPose;
    public TriggerHapticPulse_callback TriggerHapticPulse;
    public GetButtonIdNameFromEnum_callback GetButtonIdNameFromEnum;
    public GetControllerAxisTypeNameFromEnum_callback GetControllerAxisTypeNameFromEnum;
    public IsInputAvailable_callback IsInputAvailable;
    public IsSteamVRDrawingControllers_callback IsSteamVRDrawingControllers;
    public ShouldApplicationPause_callback ShouldApplicationPause;
    public ShouldApplicationReduceRenderingWork_callback ShouldApplicationReduceRenderingWork;
    public DriverDebugRequest_callback DriverDebugRequest;
    public PerformFirmwareUpdate_callback PerformFirmwareUpdate;
    public AcknowledgeQuit_Exiting_callback AcknowledgeQuit_Exiting;
    public AcknowledgeQuit_UserPrompt_callback AcknowledgeQuit_UserPrompt;

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

    public static class ByReference extends VR_IVRSystem_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRSystem_FnTable implements Structure.ByValue
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
