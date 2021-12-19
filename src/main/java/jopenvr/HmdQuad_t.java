package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdQuad_t extends Structure
{
    public HmdVector3_t[] vCorners = new HmdVector3_t[4];

    public HmdQuad_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("vCorners");
    }

    public HmdQuad_t(HmdVector3_t[] vCorners)
    {
        if (vCorners.length != this.vCorners.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.vCorners = vCorners;
        }
    }

    public HmdQuad_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdQuad_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdQuad_t implements com.sun.jna.Structure.ByValue
    {
    }
}
