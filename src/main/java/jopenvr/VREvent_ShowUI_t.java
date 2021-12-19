package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_ShowUI_t extends Structure
{
    public int eType;

    public VREvent_ShowUI_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("eType");
    }

    public VREvent_ShowUI_t(int eType)
    {
        this.eType = eType;
    }

    public VREvent_ShowUI_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_ShowUI_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_ShowUI_t implements com.sun.jna.Structure.ByValue
    {
    }
}
