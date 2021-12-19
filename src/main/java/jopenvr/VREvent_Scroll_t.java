package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Scroll_t extends Structure
{
    public float xdelta;
    public float ydelta;
    public int unused;
    public float viewportscale;

    public VREvent_Scroll_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("xdelta", "ydelta", "unused", "viewportscale");
    }

    public VREvent_Scroll_t(float xdelta, float ydelta, int unused, float viewportscale)
    {
        this.xdelta = xdelta;
        this.ydelta = ydelta;
        this.unused = unused;
        this.viewportscale = viewportscale;
    }

    public VREvent_Scroll_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Scroll_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Scroll_t implements com.sun.jna.Structure.ByValue
    {
    }
}
