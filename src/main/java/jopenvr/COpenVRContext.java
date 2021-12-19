package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

public class COpenVRContext extends Structure
{
    public IntByReference m_pVRSystem;
    public IntByReference m_pVRChaperone;
    public IntByReference m_pVRChaperoneSetup;
    public IntByReference m_pVRCompositor;
    public IntByReference m_pVROverlay;
    public IntByReference m_pVRResources;
    public IntByReference m_pVRRenderModels;
    public IntByReference m_pVRExtendedDisplay;
    public IntByReference m_pVRSettings;
    public IntByReference m_pVRApplications;
    public IntByReference m_pVRTrackedCamera;
    public IntByReference m_pVRScreenshots;
    public IntByReference m_pVRDriverManager;
    public IntByReference m_pVRInput;
    public IntByReference m_pVRIOBuffer;
    public IntByReference m_pVRSpatialAnchors;
    public IntByReference m_pVRNotifications;

    public COpenVRContext()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_pVRSystem", "m_pVRChaperone", "m_pVRChaperoneSetup", "m_pVRCompositor", "m_pVROverlay", "m_pVRResources", "m_pVRRenderModels", "m_pVRExtendedDisplay", "m_pVRSettings", "m_pVRApplications", "m_pVRTrackedCamera", "m_pVRScreenshots", "m_pVRDriverManager", "m_pVRInput", "m_pVRIOBuffer", "m_pVRSpatialAnchors", "m_pVRNotifications");
    }

    public COpenVRContext(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends COpenVRContext implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends COpenVRContext implements com.sun.jna.Structure.ByValue
    {
    }
}
