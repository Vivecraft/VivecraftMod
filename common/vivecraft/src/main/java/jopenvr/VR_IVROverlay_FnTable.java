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
    public FindOverlay_callback FindOverlay;
    public CreateOverlay_callback CreateOverlay;
    public DestroyOverlay_callback DestroyOverlay;
    public SetHighQualityOverlay_callback SetHighQualityOverlay;
    public GetHighQualityOverlay_callback GetHighQualityOverlay;
    public GetOverlayKey_callback GetOverlayKey;
    public GetOverlayName_callback GetOverlayName;
    public SetOverlayName_callback SetOverlayName;
    public GetOverlayImageData_callback GetOverlayImageData;
    public GetOverlayErrorNameFromEnum_callback GetOverlayErrorNameFromEnum;
    public SetOverlayRenderingPid_callback SetOverlayRenderingPid;
    public GetOverlayRenderingPid_callback GetOverlayRenderingPid;
    public SetOverlayFlag_callback SetOverlayFlag;
    public GetOverlayFlag_callback GetOverlayFlag;
    public SetOverlayColor_callback SetOverlayColor;
    public GetOverlayColor_callback GetOverlayColor;
    public SetOverlayAlpha_callback SetOverlayAlpha;
    public GetOverlayAlpha_callback GetOverlayAlpha;
    public SetOverlayTexelAspect_callback SetOverlayTexelAspect;
    public GetOverlayTexelAspect_callback GetOverlayTexelAspect;
    public SetOverlaySortOrder_callback SetOverlaySortOrder;
    public GetOverlaySortOrder_callback GetOverlaySortOrder;
    public SetOverlayWidthInMeters_callback SetOverlayWidthInMeters;
    public GetOverlayWidthInMeters_callback GetOverlayWidthInMeters;
    public SetOverlayAutoCurveDistanceRangeInMeters_callback SetOverlayAutoCurveDistanceRangeInMeters;
    public GetOverlayAutoCurveDistanceRangeInMeters_callback GetOverlayAutoCurveDistanceRangeInMeters;
    public SetOverlayTextureColorSpace_callback SetOverlayTextureColorSpace;
    public GetOverlayTextureColorSpace_callback GetOverlayTextureColorSpace;
    public SetOverlayTextureBounds_callback SetOverlayTextureBounds;
    public GetOverlayTextureBounds_callback GetOverlayTextureBounds;
    public GetOverlayRenderModel_callback GetOverlayRenderModel;
    public SetOverlayRenderModel_callback SetOverlayRenderModel;
    public GetOverlayTransformType_callback GetOverlayTransformType;
    public SetOverlayTransformAbsolute_callback SetOverlayTransformAbsolute;
    public GetOverlayTransformAbsolute_callback GetOverlayTransformAbsolute;
    public SetOverlayTransformTrackedDeviceRelative_callback SetOverlayTransformTrackedDeviceRelative;
    public GetOverlayTransformTrackedDeviceRelative_callback GetOverlayTransformTrackedDeviceRelative;
    public SetOverlayTransformTrackedDeviceComponent_callback SetOverlayTransformTrackedDeviceComponent;
    public GetOverlayTransformTrackedDeviceComponent_callback GetOverlayTransformTrackedDeviceComponent;
    public GetOverlayTransformOverlayRelative_callback GetOverlayTransformOverlayRelative;
    public SetOverlayTransformOverlayRelative_callback SetOverlayTransformOverlayRelative;
    public ShowOverlay_callback ShowOverlay;
    public HideOverlay_callback HideOverlay;
    public IsOverlayVisible_callback IsOverlayVisible;
    public GetTransformForOverlayCoordinates_callback GetTransformForOverlayCoordinates;
    public PollNextOverlayEvent_callback PollNextOverlayEvent;
    public GetOverlayInputMethod_callback GetOverlayInputMethod;
    public SetOverlayInputMethod_callback SetOverlayInputMethod;
    public GetOverlayMouseScale_callback GetOverlayMouseScale;
    public SetOverlayMouseScale_callback SetOverlayMouseScale;
    public ComputeOverlayIntersection_callback ComputeOverlayIntersection;
    public IsHoverTargetOverlay_callback IsHoverTargetOverlay;
    public GetGamepadFocusOverlay_callback GetGamepadFocusOverlay;
    public SetGamepadFocusOverlay_callback SetGamepadFocusOverlay;
    public SetOverlayNeighbor_callback SetOverlayNeighbor;
    public MoveGamepadFocusToNeighbor_callback MoveGamepadFocusToNeighbor;
    public SetOverlayDualAnalogTransform_callback SetOverlayDualAnalogTransform;
    public GetOverlayDualAnalogTransform_callback GetOverlayDualAnalogTransform;
    public SetOverlayTexture_callback SetOverlayTexture;
    public ClearOverlayTexture_callback ClearOverlayTexture;
    public SetOverlayRaw_callback SetOverlayRaw;
    public SetOverlayFromFile_callback SetOverlayFromFile;
    public GetOverlayTexture_callback GetOverlayTexture;
    public ReleaseNativeOverlayHandle_callback ReleaseNativeOverlayHandle;
    public GetOverlayTextureSize_callback GetOverlayTextureSize;
    public CreateDashboardOverlay_callback CreateDashboardOverlay;
    public IsDashboardVisible_callback IsDashboardVisible;
    public IsActiveDashboardOverlay_callback IsActiveDashboardOverlay;
    public SetDashboardOverlaySceneProcess_callback SetDashboardOverlaySceneProcess;
    public GetDashboardOverlaySceneProcess_callback GetDashboardOverlaySceneProcess;
    public ShowDashboard_callback ShowDashboard;
    public GetPrimaryDashboardDevice_callback GetPrimaryDashboardDevice;
    public ShowKeyboard_callback ShowKeyboard;
    public ShowKeyboardForOverlay_callback ShowKeyboardForOverlay;
    public GetKeyboardText_callback GetKeyboardText;
    public HideKeyboard_callback HideKeyboard;
    public SetKeyboardTransformAbsolute_callback SetKeyboardTransformAbsolute;
    public SetKeyboardPositionForOverlay_callback SetKeyboardPositionForOverlay;
    public SetOverlayIntersectionMask_callback SetOverlayIntersectionMask;
    public GetOverlayFlags_callback GetOverlayFlags;
    public ShowMessageOverlay_callback ShowMessageOverlay;
    public CloseMessageOverlay_callback CloseMessageOverlay;

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

    public static class ByReference extends VR_IVROverlay_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVROverlay_FnTable implements Structure.ByValue
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
