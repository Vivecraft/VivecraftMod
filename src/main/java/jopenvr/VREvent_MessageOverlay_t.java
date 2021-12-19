package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_MessageOverlay_t extends Structure
{
    public int unVRMessageOverlayResponse;

    public VREvent_MessageOverlay_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("unVRMessageOverlayResponse");
    }

    public VREvent_MessageOverlay_t(int unVRMessageOverlayResponse)
    {
        this.unVRMessageOverlayResponse = unVRMessageOverlayResponse;
    }

    public VREvent_MessageOverlay_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_MessageOverlay_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_MessageOverlay_t implements com.sun.jna.Structure.ByValue
    {
    }
}
