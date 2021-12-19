package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdVector3_t extends Structure
{
    public float[] v = new float[3];

    public HmdVector3_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("v");
    }

    public HmdVector3_t(float[] v)
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

    public HmdVector3_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdVector3_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdVector3_t implements com.sun.jna.Structure.ByValue
    {
    }
}
