package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdQuaternion_t extends Structure
{
    public double w;
    public double x;
    public double y;
    public double z;

    public HmdQuaternion_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("w", "x", "y", "z");
    }

    public HmdQuaternion_t(double w, double x, double y, double z)
    {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public HmdQuaternion_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdQuaternion_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdQuaternion_t implements com.sun.jna.Structure.ByValue
    {
    }
}
