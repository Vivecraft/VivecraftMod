package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HmdRect2_t extends Structure
{
    public HmdVector2_t vTopLeft;
    public HmdVector2_t vBottomRight;

    public HmdRect2_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("vTopLeft", "vBottomRight");
    }

    public HmdRect2_t(HmdVector2_t vTopLeft, HmdVector2_t vBottomRight)
    {
        this.vTopLeft = vTopLeft;
        this.vBottomRight = vBottomRight;
    }

    public HmdRect2_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HmdRect2_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HmdRect2_t implements com.sun.jna.Structure.ByValue
    {
    }
}
