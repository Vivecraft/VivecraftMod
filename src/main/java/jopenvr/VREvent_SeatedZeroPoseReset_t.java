package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_SeatedZeroPoseReset_t extends Structure
{
    public byte bResetBySystemMenu;

    public VREvent_SeatedZeroPoseReset_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("bResetBySystemMenu");
    }

    public VREvent_SeatedZeroPoseReset_t(byte bResetBySystemMenu)
    {
        this.bResetBySystemMenu = bResetBySystemMenu;
    }

    public VREvent_SeatedZeroPoseReset_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_SeatedZeroPoseReset_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_SeatedZeroPoseReset_t implements com.sun.jna.Structure.ByValue
    {
    }
}
