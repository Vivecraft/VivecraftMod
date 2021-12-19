package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdMatrix44_t extends Structure
{
    public float[] m = new float[16];

    public HmdMatrix44_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m");
    }

    public HmdMatrix44_t(float[] m)
    {
        if (m.length != this.m.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.m = m;
        }
    }

    public HmdMatrix44_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdMatrix44_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdMatrix44_t implements com.sun.jna.Structure.ByValue
    {
    }
}
