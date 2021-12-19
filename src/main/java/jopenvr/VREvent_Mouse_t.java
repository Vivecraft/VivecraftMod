package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Mouse_t extends Structure
{
    public float x;
    public float y;
    public int button;

    public VREvent_Mouse_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("x", "y", "button");
    }

    public VREvent_Mouse_t(float x, float y, int button)
    {
        this.x = x;
        this.y = y;
        this.button = button;
    }

    public VREvent_Mouse_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Mouse_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Mouse_t implements com.sun.jna.Structure.ByValue
    {
    }
}
