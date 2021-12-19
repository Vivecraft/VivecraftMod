package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Screenshot_t extends Structure
{
    public int handle;
    public int type;

    public VREvent_Screenshot_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("handle", "type");
    }

    public VREvent_Screenshot_t(int handle, int type)
    {
        this.handle = handle;
        this.type = type;
    }

    public VREvent_Screenshot_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Screenshot_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Screenshot_t implements com.sun.jna.Structure.ByValue
    {
    }
}
