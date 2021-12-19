package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVROverlay_FnTable extends Structure
{
    public VR_IVROverlay_FnTable.FindOverlay_callback FindOverlay;
    public VR_IVROverlay_FnTable.CreateOverlay_callback CreateOverlay;
    public VR_IVROverlay_FnTable.DestroyOverlay_callback DestroyOverlay;
    public VR_IVROverlay_FnTable.SetHighQualityOverlay_callback SetHighQualityOverlay;
    public VR_IVROverlay_FnTable.GetHighQualityOverlay_callback GetHighQualityOverlay;
    public VR_IVROverlay_FnTable.GetOverlayKey_callback GetOverlayKey;
    public VR_IVROverlay_FnTable.GetOverlayName_callback GetOverlayName;
    public VR_IVROverlay_FnTable.SetOverlayName_callback SetOverlayName;
    public VR_IVROverlay_FnTable.GetOverlayImageData_callback GetOverlayImageData;
    public VR_IVROverlay_FnTable.GetOverlayErrorNameFromEnum_callback GetOverlayErrorNameFromEnum;
    public VR_IVROverlay_FnTable.SetOverlayRenderingPid_callback SetOverlayRenderingPid;
    public VR_IVROverlay_FnTable.GetOverlayRenderingPid_callback GetOverlayRenderingPid;
    public VR_IVROverlay_FnTable.SetOverlayFlag_callback SetOverlayFlag;
    public VR_IVROverlay_FnTable.GetOverlayFlag_callback GetOverlayFlag;
    public VR_IVROverlay_FnTable.SetOverlayColor_callback SetOverlayColor;
    public VR_IVROverlay_FnTable.GetOverlayColor_callback GetOverlayColor;
    public VR_IVROverlay_FnTable.SetOverlayAlpha_callback SetOverlayAlpha;
    public VR_IVROverlay_FnTable.GetOverlayAlpha_callback GetOverlayAlpha;
    public VR_IVROverlay_FnTable.SetOverlayTexelAspect_callback SetOverlayTexelAspect;
    public VR_IVROverlay_FnTable.GetOverlayTexelAspect_callback GetOverlayTexelAspect;
    public VR_IVROverlay_FnTable.SetOverlaySortOrder_callback SetOverlaySortOrder;
    public VR_IVROverlay_FnTable.GetOverlaySortOrder_callback GetOverlaySortOrder;
    public VR_IVROverlay_FnTable.SetOverlayWidthInMeters_callback SetOverlayWidthInMeters;
    public VR_IVROverlay_FnTable.GetOverlayWidthInMeters_callback GetOverlayWidthInMeters;
    public VR_IVROverlay_FnTable.SetOverlayAutoCurveDistanceRangeInMeters_callback SetOverlayAutoCurveDistanceRangeInMeters;
    public VR_IVROverlay_FnTable.GetOverlayAutoCurveDistanceRangeInMeters_callback GetOverlayAutoCurveDistanceRangeInMeters;
    public VR_IVROverlay_FnTable.SetOverlayTextureColorSpace_callback SetOverlayTextureColorSpace;
    public VR_IVROverlay_FnTable.GetOverlayTextureColorSpace_callback GetOverlayTextureColorSpace;
    public VR_IVROverlay_FnTable.SetOverlayTextureBounds_callback SetOverlayTextureBounds;
    public VR_IVROverlay_FnTable.GetOverlayTextureBounds_callback GetOverlayTextureBounds;
    public VR_IVROverlay_FnTable.GetOverlayRenderModel_callback GetOverlayRenderModel;
    public VR_IVROverlay_FnTable.SetOverlayRenderModel_callback SetOverlayRenderModel;
    public VR_IVROverlay_FnTable.GetOverlayTransformType_callback GetOverlayTransformType;
    public VR_IVROverlay_FnTable.SetOverlayTransformAbsolute_callback SetOverlayTransformAbsolute;
    public VR_IVROverlay_FnTable.GetOverlayTransformAbsolute_callback GetOverlayTransformAbsolute;
    public VR_IVROverlay_FnTable.SetOverlayTransformTrackedDeviceRelative_callback SetOverlayTransformTrackedDeviceRelative;
    public VR_IVROverlay_FnTable.GetOverlayTransformTrackedDeviceRelative_callback GetOverlayTransformTrackedDeviceRelative;
    public VR_IVROverlay_FnTable.SetOverlayTransformTrackedDeviceComponent_callback SetOverlayTransformTrackedDeviceComponent;
    public VR_IVROverlay_FnTable.GetOverlayTransformTrackedDeviceComponent_callback GetOverlayTransformTrackedDeviceComponent;
    public VR_IVROverlay_FnTable.GetOverlayTransformOverlayRelative_callback GetOverlayTransformOverlayRelative;
    public VR_IVROverlay_FnTable.SetOverlayTransformOverlayRelative_callback SetOverlayTransformOverlayRelative;
    public VR_IVROverlay_FnTable.ShowOverlay_callback ShowOverlay;
    public VR_IVROverlay_FnTable.HideOverlay_callback HideOverlay;
    public VR_IVROverlay_FnTable.IsOverlayVisible_callback IsOverlayVisible;
    public VR_IVROverlay_FnTable.GetTransformForOverlayCoordinates_callback GetTransformForOverlayCoordinates;
    public VR_IVROverlay_FnTable.PollNextOverlayEvent_callback PollNextOverlayEvent;
    public VR_IVROverlay_FnTable.GetOverlayInputMethod_callback GetOverlayInputMethod;
    public VR_IVROverlay_FnTable.SetOverlayInputMethod_callback SetOverlayInputMethod;
    public VR_IVROverlay_FnTable.GetOverlayMouseScale_callback GetOverlayMouseScale;
    public VR_IVROverlay_FnTable.SetOverlayMouseScale_callback SetOverlayMouseScale;
    public VR_IVROverlay_FnTable.ComputeOverlayIntersection_callback ComputeOverlayIntersection;
    public VR_IVROverlay_FnTable.IsHoverTargetOverlay_callback IsHoverTargetOverlay;
    public VR_IVROverlay_FnTable.GetGamepadFocusOverlay_callback GetGamepadFocusOverlay;
    public VR_IVROverlay_FnTable.SetGamepadFocusOverlay_callback SetGamepadFocusOverlay;
    public VR_IVROverlay_FnTable.SetOverlayNeighbor_callback SetOverlayNeighbor;
    public VR_IVROverlay_FnTable.MoveGamepadFocusToNeighbor_callback MoveGamepadFocusToNeighbor;
    public VR_IVROverlay_FnTable.SetOverlayDualAnalogTransform_callback SetOverlayDualAnalogTransform;
    public VR_IVROverlay_FnTable.GetOverlayDualAnalogTransform_callback GetOverlayDualAnalogTransform;
    public VR_IVROverlay_FnTable.SetOverlayTexture_callback SetOverlayTexture;
    public VR_IVROverlay_FnTable.ClearOverlayTexture_callback ClearOverlayTexture;
    public VR_IVROverlay_FnTable.SetOverlayRaw_callback SetOverlayRaw;
    public VR_IVROverlay_FnTable.SetOverlayFromFile_callback SetOverlayFromFile;
    public VR_IVROverlay_FnTable.GetOverlayTexture_callback GetOverlayTexture;
    public VR_IVROverlay_FnTable.ReleaseNativeOverlayHandle_callback ReleaseNativeOverlayHandle;
    public VR_IVROverlay_FnTable.GetOverlayTextureSize_callback GetOverlayTextureSize;
    public VR_IVROverlay_FnTable.CreateDashboardOverlay_callback CreateDashboardOverlay;
    public VR_IVROverlay_FnTable.IsDashboardVisible_callback IsDashboardVisible;
    public VR_IVROverlay_FnTable.IsActiveDashboardOverlay_callback IsActiveDashboardOverlay;
    public VR_IVROverlay_FnTable.SetDashboardOverlaySceneProcess_callback SetDashboardOverlaySceneProcess;
    public VR_IVROverlay_FnTable.GetDashboardOverlaySceneProcess_callback GetDashboardOverlaySceneProcess;
    public VR_IVROverlay_FnTable.ShowDashboard_callback ShowDashboard;
    public VR_IVROverlay_FnTable.GetPrimaryDashboardDevice_callback GetPrimaryDashboardDevice;
    public VR_IVROverlay_FnTable.ShowKeyboard_callback ShowKeyboard;
    public VR_IVROverlay_FnTable.ShowKeyboardForOverlay_callback ShowKeyboardForOverlay;
    public VR_IVROverlay_FnTable.GetKeyboardText_callback GetKeyboardText;
    public VR_IVROverlay_FnTable.HideKeyboard_callback HideKeyboard;
    public VR_IVROverlay_FnTable.SetKeyboardTransformAbsolute_callback SetKeyboardTransformAbsolute;
    public VR_IVROverlay_FnTable.SetKeyboardPositionForOverlay_callback SetKeyboardPositionForOverlay;
    public VR_IVROverlay_FnTable.SetOverlayIntersectionMask_callback SetOverlayIntersectionMask;
    public VR_IVROverlay_FnTable.GetOverlayFlags_callback GetOverlayFlags;
    public VR_IVROverlay_FnTable.ShowMessageOverlay_callback ShowMessageOverlay;
    public VR_IVROverlay_FnTable.CloseMessageOverlay_callback CloseMessageOverlay;

    public VR_IVROverlay_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("FindOverlay", "CreateOverlay", "DestroyOverlay", "SetHighQualityOverlay", "GetHighQualityOverlay", "GetOverlayKey", "GetOverlayName", "SetOverlayName", "GetOverlayImageData", "GetOverlayErrorNameFromEnum", "SetOverlayRenderingPid", "GetOverlayRenderingPid", "SetOverlayFlag", "GetOverlayFlag", "SetOverlayColor", "GetOverlayColor", "SetOverlayAlpha", "GetOverlayAlpha", "SetOverlayTexelAspect", "GetOverlayTexelAspect", "SetOverlaySortOrder", "GetOverlaySortOrder", "SetOverlayWidthInMeters", "GetOverlayWidthInMeters", "SetOverlayAutoCurveDistanceRangeInMeters", "GetOverlayAutoCurveDistanceRangeInMeters", "SetOverlayTextureColorSpace", "GetOverlayTextureColorSpace", "SetOverlayTextureBounds", "GetOverlayTextureBounds", "GetOverlayRenderModel", "SetOverlayRenderModel", "GetOverlayTransformType", "SetOverlayTransformAbsolute", "GetOverlayTransformAbsolute", "SetOverlayTransformTrackedDeviceRelative", "GetOverlayTransformTrackedDeviceRelative", "SetOverlayTransformTrackedDeviceComponent", "GetOverlayTransformTrackedDeviceComponent", "GetOverlayTransformOverlayRelative", "SetOverlayTransformOverlayRelative", "ShowOverlay", "HideOverlay", "IsOverlayVisible", "GetTransformForOverlayCoordinates", "PollNextOverlayEvent", "GetOverlayInputMethod", "SetOverlayInputMethod", "GetOverlayMouseScale", "SetOverlayMouseScale", "ComputeOverlayIntersection", "IsHoverTargetOverlay", "GetGamepadFocusOverlay", "SetGamepadFocusOverlay", "SetOverlayNeighbor", "MoveGamepadFocusToNeighbor", "SetOverlayDualAnalogTransform", "GetOverlayDualAnalogTransform", "SetOverlayTexture", "ClearOverlayTexture", "SetOverlayRaw", "SetOverlayFromFile", "GetOverlayTexture", "ReleaseNativeOverlayHandle", "GetOverlayTextureSize", "CreateDashboardOverlay", "IsDashboardVisible", "IsActiveDashboardOverlay", "SetDashboardOverlaySceneProcess", "GetDashboardOverlaySceneProcess", "ShowDashboard", "GetPrimaryDashboardDevice", "ShowKeyboard", "ShowKeyboardForOverlay", "GetKeyboardText", "HideKeyboard", "SetKeyboardTransformAbsolute", "SetKeyboardPositionForOverlay", "SetOverlayIntersectionMask", "GetOverlayFlags", "ShowMessageOverlay", "CloseMessageOverlay");
    }

    public VR_IVROverlay_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVROverlay_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVROverlay_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface ClearOverlayTexture_callback extends Callback
    {
        int apply(long var1);
    }

    public interface CloseMessageOverlay_callback extends Callback
    {
        void apply();
    }

    public interface ComputeOverlayIntersection_callback extends Callback
    {
        byte apply(long var1, VROverlayIntersectionParams_t var3, VROverlayIntersectionResults_t var4);
    }

    public interface CreateDashboardOverlay_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, LongByReference var3, LongByReference var4);
    }

    public interface CreateOverlay_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, LongByReference var3);
    }

    public interface DestroyOverlay_callback extends Callback
    {
        int apply(long var1);
    }

    public interface FindOverlay_callback extends Callback
    {
        int apply(Pointer var1, LongByReference var2);
    }

    public interface GetDashboardOverlaySceneProcess_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface GetGamepadFocusOverlay_callback extends Callback
    {
        long apply();
    }

    public interface GetHighQualityOverlay_callback extends Callback
    {
        long apply();
    }

    public interface GetKeyboardText_callback extends Callback
    {
        int apply(Pointer var1, int var2);
    }

    public interface GetOverlayAlpha_callback extends Callback
    {
        int apply(long var1, FloatByReference var3);
    }

    public interface GetOverlayAutoCurveDistanceRangeInMeters_callback extends Callback
    {
        int apply(long var1, FloatByReference var3, FloatByReference var4);
    }

    public interface GetOverlayColor_callback extends Callback
    {
        int apply(long var1, FloatByReference var3, FloatByReference var4, FloatByReference var5);
    }

    public interface GetOverlayDualAnalogTransform_callback extends Callback
    {
        int apply(long var1, int var3, HmdVector2_t var4, FloatByReference var5);
    }

    public interface GetOverlayErrorNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetOverlayFlag_callback extends Callback
    {
        int apply(long var1, int var3, Pointer var4);
    }

    public interface GetOverlayFlags_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface GetOverlayImageData_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4, IntByReference var5, IntByReference var6);
    }

    public interface GetOverlayInputMethod_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface GetOverlayKey_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4, IntByReference var5);
    }

    public interface GetOverlayMouseScale_callback extends Callback
    {
        int apply(long var1, HmdVector2_t var3);
    }

    public interface GetOverlayName_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4, IntByReference var5);
    }

    public interface GetOverlayRenderModel_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4, HmdColor_t var5, IntByReference var6);
    }

    public interface GetOverlayRenderingPid_callback extends Callback
    {
        int apply(long var1);
    }

    public interface GetOverlaySortOrder_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface GetOverlayTexelAspect_callback extends Callback
    {
        int apply(long var1, FloatByReference var3);
    }

    public interface GetOverlayTextureBounds_callback extends Callback
    {
        int apply(long var1, VRTextureBounds_t var3);
    }

    public interface GetOverlayTextureColorSpace_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface GetOverlayTextureSize_callback extends Callback
    {
        int apply(long var1, IntByReference var3, IntByReference var4);
    }

    public interface GetOverlayTexture_callback extends Callback
    {
        int apply(long var1, PointerByReference var3, Pointer var4, IntByReference var5, IntByReference var6, IntByReference var7, IntByReference var8, IntByReference var9, VRTextureBounds_t var10);
    }

    public interface GetOverlayTransformAbsolute_callback extends Callback
    {
        int apply(long var1, IntByReference var3, HmdMatrix34_t var4);
    }

    public interface GetOverlayTransformOverlayRelative_callback extends Callback
    {
        int apply(long var1, LongByReference var3, HmdMatrix34_t var4);
    }

    public interface GetOverlayTransformTrackedDeviceComponent_callback extends Callback
    {
        int apply(long var1, IntByReference var3, Pointer var4, int var5);
    }

    public interface GetOverlayTransformTrackedDeviceRelative_callback extends Callback
    {
        int apply(long var1, IntByReference var3, HmdMatrix34_t var4);
    }

    public interface GetOverlayTransformType_callback extends Callback
    {
        int apply(long var1, IntByReference var3);
    }

    public interface GetOverlayWidthInMeters_callback extends Callback
    {
        int apply(long var1, FloatByReference var3);
    }

    public interface GetPrimaryDashboardDevice_callback extends Callback
    {
        int apply();
    }

    public interface GetTransformForOverlayCoordinates_callback extends Callback
    {
        int apply(long var1, int var3, HmdVector2_t.ByValue var4, HmdMatrix34_t var5);
    }

    public interface HideKeyboard_callback extends Callback
    {
        void apply();
    }

    public interface HideOverlay_callback extends Callback
    {
        int apply(long var1);
    }

    public interface IsActiveDashboardOverlay_callback extends Callback
    {
        byte apply(long var1);
    }

    public interface IsDashboardVisible_callback extends Callback
    {
        byte apply();
    }

    public interface IsHoverTargetOverlay_callback extends Callback
    {
        byte apply(long var1);
    }

    public interface IsOverlayVisible_callback extends Callback
    {
        byte apply(long var1);
    }

    public interface MoveGamepadFocusToNeighbor_callback extends Callback
    {
        int apply(int var1, long var2);
    }

    public interface PollNextOverlayEvent_callback extends Callback
    {
        byte apply(long var1, VREvent_t var3, int var4);
    }

    public interface ReleaseNativeOverlayHandle_callback extends Callback
    {
        int apply(long var1, Pointer var3);
    }

    public interface SetDashboardOverlaySceneProcess_callback extends Callback
    {
        int apply(long var1, int var3);
    }

    public interface SetGamepadFocusOverlay_callback extends Callback
    {
        int apply(long var1);
    }

    public interface SetHighQualityOverlay_callback extends Callback
    {
        int apply(long var1);
    }

    public interface SetKeyboardPositionForOverlay_callback extends Callback
    {
        void apply(long var1, HmdRect2_t.ByValue var3);
    }

    public interface SetKeyboardTransformAbsolute_callback extends Callback
    {
        void apply(int var1, HmdMatrix34_t var2);
    }

    public interface SetOverlayAlpha_callback extends Callback
    {
        int apply(long var1, float var3);
    }

    public interface SetOverlayAutoCurveDistanceRangeInMeters_callback extends Callback
    {
        int apply(long var1, float var3, float var4);
    }

    public interface SetOverlayColor_callback extends Callback
    {
        int apply(long var1, float var3, float var4, float var5);
    }

    public interface SetOverlayDualAnalogTransform_callback extends Callback
    {
        int apply(long var1, int var3, HmdVector2_t var4, float var5);
    }

    public interface SetOverlayFlag_callback extends Callback
    {
        int apply(long var1, int var3, byte var4);
    }

    public interface SetOverlayFromFile_callback extends Callback
    {
        int apply(long var1, Pointer var3);
    }

    public interface SetOverlayInputMethod_callback extends Callback
    {
        int apply(long var1, int var3);
    }

    public interface SetOverlayIntersectionMask_callback extends Callback
    {
        int apply(long var1, VROverlayIntersectionMaskPrimitive_t var3, int var4, int var5);
    }

    public interface SetOverlayMouseScale_callback extends Callback
    {
        int apply(long var1, HmdVector2_t var3);
    }

    public interface SetOverlayName_callback extends Callback
    {
        int apply(long var1, Pointer var3);
    }

    public interface SetOverlayNeighbor_callback extends Callback
    {
        int apply(int var1, long var2, long var4);
    }

    public interface SetOverlayRaw_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4, int var5, int var6);
    }

    public interface SetOverlayRenderModel_callback extends Callback
    {
        int apply(long var1, Pointer var3, HmdColor_t var4);
    }

    public interface SetOverlayRenderingPid_callback extends Callback
    {
        int apply(long var1, int var3);
    }

    public interface SetOverlaySortOrder_callback extends Callback
    {
        int apply(long var1, int var3);
    }

    public interface SetOverlayTexelAspect_callback extends Callback
    {
        int apply(long var1, float var3);
    }

    public interface SetOverlayTextureBounds_callback extends Callback
    {
        int apply(long var1, VRTextureBounds_t var3);
    }

    public interface SetOverlayTextureColorSpace_callback extends Callback
    {
        int apply(long var1, int var3);
    }

    public interface SetOverlayTexture_callback extends Callback
    {
        int apply(long var1, Texture_t var3);
    }

    public interface SetOverlayTransformAbsolute_callback extends Callback
    {
        int apply(long var1, int var3, HmdMatrix34_t var4);
    }

    public interface SetOverlayTransformOverlayRelative_callback extends Callback
    {
        int apply(long var1, long var3, HmdMatrix34_t var5);
    }

    public interface SetOverlayTransformTrackedDeviceComponent_callback extends Callback
    {
        int apply(long var1, int var3, Pointer var4);
    }

    public interface SetOverlayTransformTrackedDeviceRelative_callback extends Callback
    {
        int apply(long var1, int var3, HmdMatrix34_t var4);
    }

    public interface SetOverlayWidthInMeters_callback extends Callback
    {
        int apply(long var1, float var3);
    }

    public interface ShowDashboard_callback extends Callback
    {
        void apply(Pointer var1);
    }

    public interface ShowKeyboardForOverlay_callback extends Callback
    {
        int apply(long var1, int var3, int var4, Pointer var5, int var6, Pointer var7, byte var8, long var9);
    }

    public interface ShowKeyboard_callback extends Callback
    {
        int apply(int var1, int var2, Pointer var3, int var4, Pointer var5, byte var6, long var7);
    }

    public interface ShowMessageOverlay_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, Pointer var3, Pointer var4, Pointer var5, Pointer var6);
    }

    public interface ShowOverlay_callback extends Callback
    {
        int apply(long var1);
    }
}
