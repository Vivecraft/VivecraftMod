package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdVector4_t extends Structure
{
    public float[] v = new float[4];

    public HmdVector4_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("v");
    }

    public HmdVector4_t(float[] v)
    {
        if (v.length != this.v.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.v = v;
        }
    }

    public HmdVector4_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdVector4_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdVector4_t implements com.sun.jna.Structure.ByValue
    {
    }
}
