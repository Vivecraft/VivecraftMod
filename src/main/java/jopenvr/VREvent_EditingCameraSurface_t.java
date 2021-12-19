package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_EditingCameraSurface_t extends Structure
{
    public long overlayHandle;
    public int nVisualMode;

    public VREvent_EditingCameraSurface_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("overlayHandle", "nVisualMode");
    }

    public VREvent_EditingCameraSurface_t(long overlayHandle, int nVisualMode)
    {
        this.overlayHandle = overlayHandle;
        this.nVisualMode = nVisualMode;
    }

    public VREvent_EditingCameraSurface_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_EditingCameraSurface_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_EditingCameraSurface_t implements com.sun.jna.Structure.ByValue
    {
    }
}
