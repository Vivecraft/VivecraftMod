package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_DualAnalog_t extends Structure
{
    public float x;
    public float y;
    public float transformedX;
    public float transformedY;
    public int which;

    public VREvent_DualAnalog_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("x", "y", "transformedX", "transformedY", "which");
    }

    public VREvent_DualAnalog_t(float x, float y, float transformedX, float transformedY, int which)
    {
        this.x = x;
        this.y = y;
        this.transformedX = transformedX;
        this.transformedY = transformedY;
        this.which = which;
    }

    public VREvent_DualAnalog_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_DualAnalog_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_DualAnalog_t implements com.sun.jna.Structure.ByValue
    {
    }
}
