package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Union;

public class VREvent_Data_t extends Union
{
    public VREvent_Reserved_t reserved;
    public VREvent_Controller_t controller;
    public VREvent_Mouse_t mouse;
    public VREvent_Scroll_t scroll;
    public VREvent_Process_t process;
    public VREvent_Notification_t notification;
    public VREvent_Overlay_t overlay;
    public VREvent_Status_t status;
    public VREvent_Keyboard_t keyboard;
    public VREvent_Ipd_t ipd;
    public VREvent_Chaperone_t chaperone;
    public VREvent_PerformanceTest_t performanceTest;
    public VREvent_TouchPadMove_t touchPadMove;
    public VREvent_SeatedZeroPoseReset_t seatedZeroPoseReset;
    public VREvent_Screenshot_t screenshot;
    public VREvent_ScreenshotProgress_t screenshotProgress;
    public VREvent_ApplicationLaunch_t applicationLaunch;
    public VREvent_EditingCameraSurface_t cameraSurface;
    public VREvent_MessageOverlay_t messageOverlay;
    public VREvent_Property_t property;
    public VREvent_DualAnalog_t dualAnalog;
    public VREvent_HapticVibration_t hapticVibration;
    public VREvent_WebConsole_t webConsole;
    public VREvent_InputBindingLoad_t inputBinding;
    public VREvent_InputActionManifestLoad_t actionManifest;
    public VREvent_SpatialAnchor_t spatialAnchor;

    public VREvent_Data_t()
    {
    }

    public VREvent_Data_t(VREvent_Reserved_t reserved)
    {
        this.reserved = reserved;
        this.setType(VREvent_Reserved_t.class);
    }

    public VREvent_Data_t(VREvent_Controller_t controller)
    {
        this.controller = controller;
        this.setType(VREvent_Controller_t.class);
    }

    public VREvent_Data_t(VREvent_Mouse_t mouse)
    {
        this.mouse = mouse;
        this.setType(VREvent_Mouse_t.class);
    }

    public VREvent_Data_t(VREvent_Scroll_t scroll)
    {
        this.scroll = scroll;
        this.setType(VREvent_Scroll_t.class);
    }

    public VREvent_Data_t(VREvent_Process_t process)
    {
        this.process = process;
        this.setType(VREvent_Process_t.class);
    }

    public VREvent_Data_t(VREvent_Notification_t notification)
    {
        this.notification = notification;
        this.setType(VREvent_Notification_t.class);
    }

    public VREvent_Data_t(VREvent_Overlay_t overlay)
    {
        this.overlay = overlay;
        this.setType(VREvent_Overlay_t.class);
    }

    public VREvent_Data_t(VREvent_Status_t status)
    {
        this.status = status;
        this.setType(VREvent_Status_t.class);
    }

    public VREvent_Data_t(VREvent_Keyboard_t keyboard)
    {
        this.keyboard = keyboard;
        this.setType(VREvent_Keyboard_t.class);
    }

    public VREvent_Data_t(VREvent_Ipd_t ipd)
    {
        this.ipd = ipd;
        this.setType(VREvent_Ipd_t.class);
    }

    public VREvent_Data_t(VREvent_Chaperone_t chaperone)
    {
        this.chaperone = chaperone;
        this.setType(VREvent_Chaperone_t.class);
    }

    public VREvent_Data_t(VREvent_PerformanceTest_t performanceTest)
    {
        this.performanceTest = performanceTest;
        this.setType(VREvent_PerformanceTest_t.class);
    }

    public VREvent_Data_t(VREvent_TouchPadMove_t touchPadMove)
    {
        this.touchPadMove = touchPadMove;
        this.setType(VREvent_TouchPadMove_t.class);
    }

    public VREvent_Data_t(VREvent_SeatedZeroPoseReset_t seatedZeroPoseReset)
    {
        this.seatedZeroPoseReset = seatedZeroPoseReset;
        this.setType(VREvent_SeatedZeroPoseReset_t.class);
    }

    public VREvent_Data_t(VREvent_Screenshot_t screenshot)
    {
        this.screenshot = screenshot;
        this.setType(VREvent_Screenshot_t.class);
    }

    public VREvent_Data_t(VREvent_ScreenshotProgress_t screenshotProgress)
    {
        this.screenshotProgress = screenshotProgress;
        this.setType(VREvent_ScreenshotProgress_t.class);
    }

    public VREvent_Data_t(VREvent_ApplicationLaunch_t applicationLaunch)
    {
        this.applicationLaunch = applicationLaunch;
        this.setType(VREvent_ApplicationLaunch_t.class);
    }

    public VREvent_Data_t(VREvent_EditingCameraSurface_t cameraSurface)
    {
        this.cameraSurface = cameraSurface;
        this.setType(VREvent_EditingCameraSurface_t.class);
    }

    public VREvent_Data_t(VREvent_MessageOverlay_t messageOverlay)
    {
        this.messageOverlay = messageOverlay;
        this.setType(VREvent_MessageOverlay_t.class);
    }

    public VREvent_Data_t(VREvent_Property_t property)
    {
        this.property = property;
        this.setType(VREvent_Property_t.class);
    }

    public VREvent_Data_t(VREvent_DualAnalog_t dualAnalog)
    {
        this.dualAnalog = dualAnalog;
        this.setType(VREvent_DualAnalog_t.class);
    }

    public VREvent_Data_t(VREvent_HapticVibration_t hapticVibration)
    {
        this.hapticVibration = hapticVibration;
        this.setType(VREvent_HapticVibration_t.class);
    }

    public VREvent_Data_t(VREvent_WebConsole_t webConsole)
    {
        this.webConsole = webConsole;
        this.setType(VREvent_WebConsole_t.class);
    }

    public VREvent_Data_t(VREvent_InputBindingLoad_t inputBinding)
    {
        this.inputBinding = inputBinding;
        this.setType(VREvent_InputBindingLoad_t.class);
    }

    public VREvent_Data_t(VREvent_InputActionManifestLoad_t actionManifest)
    {
        this.actionManifest = actionManifest;
        this.setType(VREvent_InputActionManifestLoad_t.class);
    }

    public VREvent_Data_t(VREvent_SpatialAnchor_t spatialAnchor)
    {
        this.spatialAnchor = spatialAnchor;
        this.setType(VREvent_SpatialAnchor_t.class);
    }

    public VREvent_Data_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Data_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Data_t implements com.sun.jna.Structure.ByValue
    {
    }
}
