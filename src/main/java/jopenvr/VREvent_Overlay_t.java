package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Overlay_t extends Structure
{
    public long overlayHandle;
    public long devicePath;

    public VREvent_Overlay_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("overlayHandle", "devicePath");
    }

    public VREvent_Overlay_t(long overlayHandle, long devicePath)
    {
        this.overlayHandle = overlayHandle;
        this.devicePath = devicePath;
    }

    public VREvent_Overlay_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Overlay_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Overlay_t implements com.sun.jna.Structure.ByValue
    {
    }
}
