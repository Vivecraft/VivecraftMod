package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VROverlayIntersectionParams_t extends Structure
{
    public HmdVector3_t vSource;
    public HmdVector3_t vDirection;
    public int eOrigin;

    public VROverlayIntersectionParams_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("vSource", "vDirection", "eOrigin");
    }

    public VROverlayIntersectionParams_t(HmdVector3_t vSource, HmdVector3_t vDirection, int eOrigin)
    {
        this.vSource = vSource;
        this.vDirection = vDirection;
        this.eOrigin = eOrigin;
    }

    public VROverlayIntersectionParams_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VROverlayIntersectionParams_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VROverlayIntersectionParams_t implements com.sun.jna.Structure.ByValue
    {
    }
}
