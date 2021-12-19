package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRControllerAxis_t extends Structure
{
    public float x;
    public float y;

    public VRControllerAxis_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("x", "y");
    }

    public VRControllerAxis_t(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public VRControllerAxis_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRControllerAxis_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRControllerAxis_t implements com.sun.jna.Structure.ByValue
    {
    }
}
