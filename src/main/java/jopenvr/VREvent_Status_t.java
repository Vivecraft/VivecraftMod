package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Status_t extends Structure
{
    public int statusState;

    public VREvent_Status_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("statusState");
    }

    public VREvent_Status_t(int statusState)
    {
        this.statusState = statusState;
    }

    public VREvent_Status_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Status_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Status_t implements com.sun.jna.Structure.ByValue
    {
    }
}
