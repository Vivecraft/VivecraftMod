package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdColor_t extends Structure
{
    public float r;
    public float g;
    public float b;
    public float a;

    public HmdColor_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("r", "g", "b", "a");
    }

    public HmdColor_t(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public HmdColor_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdColor_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdColor_t implements com.sun.jna.Structure.ByValue
    {
    }
}
