package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdMatrix33_t extends Structure
{
    public float[] m = new float[9];

    public HmdMatrix33_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m");
    }

    public HmdMatrix33_t(float[] m)
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

    public HmdMatrix33_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdMatrix33_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdMatrix33_t implements com.sun.jna.Structure.ByValue
    {
    }
}
