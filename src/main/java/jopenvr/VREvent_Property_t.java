package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Property_t extends Structure
{
    public long container;
    public int prop;

    public VREvent_Property_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("container", "prop");
    }

    public VREvent_Property_t(long container, int prop)
    {
        this.container = container;
        this.prop = prop;
    }

    public VREvent_Property_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Property_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Property_t implements com.sun.jna.Structure.ByValue
    {
    }
}
