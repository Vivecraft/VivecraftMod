package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVRCompositor_FnTable extends Structure
{
    public VR_IVRCompositor_FnTable.SetTrackingSpace_callback SetTrackingSpace;
    public VR_IVRCompositor_FnTable.GetTrackingSpace_callback GetTrackingSpace;
    public VR_IVRCompositor_FnTable.WaitGetPoses_callback WaitGetPoses;
    public VR_IVRCompositor_FnTable.GetLastPoses_callback GetLastPoses;
    public VR_IVRCompositor_FnTable.GetLastPoseForTrackedDeviceIndex_callback GetLastPoseForTrackedDeviceIndex;
    public VR_IVRCompositor_FnTable.Submit_callback Submit;
    public VR_IVRCompositor_FnTable.ClearLastSubmittedFrame_callback ClearLastSubmittedFrame;
    public VR_IVRCompositor_FnTable.PostPresentHandoff_callback PostPresentHandoff;
    public VR_IVRCompositor_FnTable.GetFrameTiming_callback GetFrameTiming;
    public VR_IVRCompositor_FnTable.GetFrameTimings_callback GetFrameTimings;
    public VR_IVRCompositor_FnTable.GetFrameTimeRemaining_callback GetFrameTimeRemaining;
    public VR_IVRCompositor_FnTable.GetCumulativeStats_callback GetCumulativeStats;
    public VR_IVRCompositor_FnTable.FadeToColor_callback FadeToColor;
    public VR_IVRCompositor_FnTable.GetCurrentFadeColor_callback GetCurrentFadeColor;
    public VR_IVRCompositor_FnTable.FadeGrid_callback FadeGrid;
    public VR_IVRCompositor_FnTable.GetCurrentGridAlpha_callback GetCurrentGridAlpha;
    public VR_IVRCompositor_FnTable.SetSkyboxOverride_callback SetSkyboxOverride;
    public VR_IVRCompositor_FnTable.ClearSkyboxOverride_callback ClearSkyboxOverride;
    public VR_IVRCompositor_FnTable.CompositorBringToFront_callback CompositorBringToFront;
    public VR_IVRCompositor_FnTable.CompositorGoToBack_callback CompositorGoToBack;
    public VR_IVRCompositor_FnTable.CompositorQuit_callback CompositorQuit;
    public VR_IVRCompositor_FnTable.IsFullscreen_callback IsFullscreen;
    public VR_IVRCompositor_FnTable.GetCurrentSceneFocusProcess_callback GetCurrentSceneFocusProcess;
    public VR_IVRCompositor_FnTable.GetLastFrameRenderer_callback GetLastFrameRenderer;
    public VR_IVRCompositor_FnTable.CanRenderScene_callback CanRenderScene;
    public VR_IVRCompositor_FnTable.ShowMirrorWindow_callback ShowMirrorWindow;
    public VR_IVRCompositor_FnTable.HideMirrorWindow_callback HideMirrorWindow;
    public VR_IVRCompositor_FnTable.IsMirrorWindowVisible_callback IsMirrorWindowVisible;
    public VR_IVRCompositor_FnTable.CompositorDumpImages_callback CompositorDumpImages;
    public VR_IVRCompositor_FnTable.ShouldAppRenderWithLowResources_callback ShouldAppRenderWithLowResources;
    public VR_IVRCompositor_FnTable.ForceInterleavedReprojectionOn_callback ForceInterleavedReprojectionOn;
    public VR_IVRCompositor_FnTable.ForceReconnectProcess_callback ForceReconnectProcess;
    public VR_IVRCompositor_FnTable.SuspendRendering_callback SuspendRendering;
    public VR_IVRCompositor_FnTable.GetMirrorTextureD3D11_callback GetMirrorTextureD3D11;
    public VR_IVRCompositor_FnTable.ReleaseMirrorTextureD3D11_callback ReleaseMirrorTextureD3D11;
    public VR_IVRCompositor_FnTable.GetMirrorTextureGL_callback GetMirrorTextureGL;
    public VR_IVRCompositor_FnTable.ReleaseSharedGLTexture_callback ReleaseSharedGLTexture;
    public VR_IVRCompositor_FnTable.LockGLSharedTextureForAccess_callback LockGLSharedTextureForAccess;
    public VR_IVRCompositor_FnTable.UnlockGLSharedTextureForAccess_callback UnlockGLSharedTextureForAccess;
    public VR_IVRCompositor_FnTable.GetVulkanInstanceExtensionsRequired_callback GetVulkanInstanceExtensionsRequired;
    public VR_IVRCompositor_FnTable.GetVulkanDeviceExtensionsRequired_callback GetVulkanDeviceExtensionsRequired;
    public VR_IVRCompositor_FnTable.SetExplicitTimingMode_callback SetExplicitTimingMode;
    public VR_IVRCompositor_FnTable.SubmitExplicitTimingData_callback SubmitExplicitTimingData;
    public VR_IVRCompositor_FnTable.IsMotionSmoothingEnabled_callback IsMotionSmoothingEnabled;
    public VR_IVRCompositor_FnTable.IsMotionSmoothingSupported_callback IsMotionSmoothingSupported;
    public VR_IVRCompositor_FnTable.IsCurrentSceneFocusAppLoading_callback IsCurrentSceneFocusAppLoading;

    public VR_IVRCompositor_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("SetTrackingSpace", "GetTrackingSpace", "WaitGetPoses", "GetLastPoses", "GetLastPoseForTrackedDeviceIndex", "Submit", "ClearLastSubmittedFrame", "PostPresentHandoff", "GetFrameTiming", "GetFrameTimings", "GetFrameTimeRemaining", "GetCumulativeStats", "FadeToColor", "GetCurrentFadeColor", "FadeGrid", "GetCurrentGridAlpha", "SetSkyboxOverride", "ClearSkyboxOverride", "CompositorBringToFront", "CompositorGoToBack", "CompositorQuit", "IsFullscreen", "GetCurrentSceneFocusProcess", "GetLastFrameRenderer", "CanRenderScene", "ShowMirrorWindow", "HideMirrorWindow", "IsMirrorWindowVisible", "CompositorDumpImages", "ShouldAppRenderWithLowResources", "ForceInterleavedReprojectionOn", "ForceReconnectProcess", "SuspendRendering", "GetMirrorTextureD3D11", "ReleaseMirrorTextureD3D11", "GetMirrorTextureGL", "ReleaseSharedGLTexture", "LockGLSharedTextureForAccess", "UnlockGLSharedTextureForAccess", "GetVulkanInstanceExtensionsRequired", "GetVulkanDeviceExtensionsRequired", "SetExplicitTimingMode", "SubmitExplicitTimingData", "IsMotionSmoothingEnabled", "IsMotionSmoothingSupported", "IsCurrentSceneFocusAppLoading");
    }

    public VR_IVRCompositor_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRCompositor_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRCompositor_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface CanRenderScene_callback extends Callback
    {
        byte apply();
    }

    public interface ClearLastSubmittedFrame_callback extends Callback
    {
        void apply();
    }

    public interface ClearSkyboxOverride_callback extends Callback
    {
        void apply();
    }

    public interface CompositorBringToFront_callback extends Callback
    {
        void apply();
    }

    public interface CompositorDumpImages_callback extends Callback
    {
        void apply();
    }

    public interface CompositorGoToBack_callback extends Callback
    {
        void apply();
    }

    public interface CompositorQuit_callback extends Callback
    {
        void apply();
    }

    public interface FadeGrid_callback extends Callback
    {
        void apply(float var1, byte var2);
    }

    public interface FadeToColor_callback extends Callback
    {
        void apply(float var1, float var2, float var3, float var4, float var5, byte var6);
    }

    public interface ForceInterleavedReprojectionOn_callback extends Callback
    {
        void apply(byte var1);
    }

    public interface ForceReconnectProcess_callback extends Callback
    {
        void apply();
    }

    public interface GetCumulativeStats_callback extends Callback
    {
        void apply(Compositor_CumulativeStats var1, int var2);
    }

    public interface GetCurrentFadeColor_callback extends Callback
    {
        HmdColor_t.ByValue apply(byte var1);
    }

    public interface GetCurrentGridAlpha_callback extends Callback
    {
        float apply();
    }

    public interface GetCurrentSceneFocusProcess_callback extends Callback
    {
        int apply();
    }

    public interface GetFrameTimeRemaining_callback extends Callback
    {
        float apply();
    }

    public interface GetFrameTiming_callback extends Callback
    {
        byte apply(Compositor_FrameTiming var1, int var2);
    }

    public interface GetFrameTimings_callback extends Callback
    {
        int apply(Compositor_FrameTiming var1, int var2);
    }

    public interface GetLastFrameRenderer_callback extends Callback
    {
        int apply();
    }

    public interface GetLastPoseForTrackedDeviceIndex_callback extends Callback
    {
        int apply(int var1, TrackedDevicePose_t var2, TrackedDevicePose_t var3);
    }

    public interface GetLastPoses_callback extends Callback
    {
        int apply(TrackedDevicePose_t var1, int var2, TrackedDevicePose_t var3, int var4);
    }

    public interface GetMirrorTextureD3D11_callback extends Callback
    {
        int apply(int var1, Pointer var2, PointerByReference var3);
    }

    public interface GetMirrorTextureGL_callback extends Callback
    {
        int apply(int var1, IntByReference var2, PointerByReference var3);
    }

    public interface GetTrackingSpace_callback extends Callback
    {
        int apply();
    }

    public interface GetVulkanDeviceExtensionsRequired_callback extends Callback
    {
        int apply(JOpenVRLibrary.VkPhysicalDevice_T var1, Pointer var2, int var3);
    }

    public interface GetVulkanInstanceExtensionsRequired_callback extends Callback
    {
        int apply(Pointer var1, int var2);
    }

    public interface HideMirrorWindow_callback extends Callback
    {
        void apply();
    }

    public interface IsCurrentSceneFocusAppLoading_callback extends Callback
    {
        byte apply();
    }

    public interface IsFullscreen_callback extends Callback
    {
        byte apply();
    }

    public interface IsMirrorWindowVisible_callback extends Callback
    {
        byte apply();
    }

    public interface IsMotionSmoothingEnabled_callback extends Callback
    {
        byte apply();
    }

    public interface IsMotionSmoothingSupported_callback extends Callback
    {
        byte apply();
    }

    public interface LockGLSharedTextureForAccess_callback extends Callback
    {
        void apply(Pointer var1);
    }

    public interface PostPresentHandoff_callback extends Callback
    {
        void apply();
    }

    public interface ReleaseMirrorTextureD3D11_callback extends Callback
    {
        void apply(Pointer var1);
    }

    public interface ReleaseSharedGLTexture_callback extends Callback
    {
        byte apply(int var1, Pointer var2);
    }

    public interface SetExplicitTimingMode_callback extends Callback
    {
        void apply(int var1);
    }

    public interface SetSkyboxOverride_callback extends Callback
    {
        int apply(Texture_t var1, int var2);
    }

    public interface SetTrackingSpace_callback extends Callback
    {
        void apply(int var1);
    }

    public interface ShouldAppRenderWithLowResources_callback extends Callback
    {
        byte apply();
    }

    public interface ShowMirrorWindow_callback extends Callback
    {
        void apply();
    }

    public interface SubmitExplicitTimingData_callback extends Callback
    {
        int apply();
    }

    public interface Submit_callback extends Callback
    {
        int apply(int var1, Texture_t var2, VRTextureBounds_t var3, int var4);
    }

    public interface SuspendRendering_callback extends Callback
    {
        void apply(byte var1);
    }

    public interface UnlockGLSharedTextureForAccess_callback extends Callback
    {
        void apply(Pointer var1);
    }

    public interface WaitGetPoses_callback extends Callback
    {
        int apply(TrackedDevicePose_t var1, int var2, TrackedDevicePose_t var3, int var4);
    }
}
