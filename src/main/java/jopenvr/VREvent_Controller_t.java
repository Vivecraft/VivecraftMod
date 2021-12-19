package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Controller_t extends Structure
{
    public int button;

    public VREvent_Controller_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("button");
    }

    public VREvent_Controller_t(int button)
    {
        this.button = button;
    }

    public VREvent_Controller_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Controller_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Controller_t implements com.sun.jna.Structure.ByValue
    {
    }
}
