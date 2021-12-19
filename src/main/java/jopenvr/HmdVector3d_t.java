package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdVector3d_t extends Structure
{
    public double[] v = new double[3];

    public HmdVector3d_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("v");
    }

    public HmdVector3d_t(double[] v)
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

    public HmdVector3d_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdVector3d_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdVector3d_t implements com.sun.jna.Structure.ByValue
    {
    }
}
