package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdQuaternionf_t extends Structure
{
    public float w;
    public float x;
    public float y;
    public float z;

    public HmdQuaternionf_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("w", "x", "y", "z");
    }

    public HmdQuaternionf_t(float w, float x, float y, float z)
    {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public HmdQuaternionf_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdQuaternionf_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdQuaternionf_t implements com.sun.jna.Structure.ByValue
    {
    }
}
