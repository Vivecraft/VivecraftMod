package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdVector2_t extends Structure
{
    public float[] v = new float[2];

    public HmdVector2_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("v");
    }

    public HmdVector2_t(float[] v)
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

    public HmdVector2_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdVector2_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdVector2_t implements com.sun.jna.Structure.ByValue
    {
    }
}
