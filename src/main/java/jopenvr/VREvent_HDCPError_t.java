package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_HDCPError_t extends Structure
{
    public int eCode;

    public VREvent_HDCPError_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("eCode");
    }

    public VREvent_HDCPError_t(int eCode)
    {
        this.eCode = eCode;
    }

    public VREvent_HDCPError_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_HDCPError_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_HDCPError_t implements com.sun.jna.Structure.ByValue
    {
    }
}
