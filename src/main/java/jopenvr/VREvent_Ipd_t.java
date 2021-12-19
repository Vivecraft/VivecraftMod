package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Ipd_t extends Structure
{
    public float ipdMeters;

    public VREvent_Ipd_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("ipdMeters");
    }

    public VREvent_Ipd_t(float ipdMeters)
    {
        this.ipdMeters = ipdMeters;
    }

    public VREvent_Ipd_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Ipd_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Ipd_t implements com.sun.jna.Structure.ByValue
    {
    }
}
