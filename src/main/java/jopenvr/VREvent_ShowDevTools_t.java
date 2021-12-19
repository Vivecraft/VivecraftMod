package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_ShowDevTools_t extends Structure
{
    public int nBrowserIdentifier;

    public VREvent_ShowDevTools_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("nBrowserIdentifier");
    }

    public VREvent_ShowDevTools_t(int nBrowserIdentifier)
    {
        this.nBrowserIdentifier = nBrowserIdentifier;
    }

    public VREvent_ShowDevTools_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_ShowDevTools_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_ShowDevTools_t implements com.sun.jna.Structure.ByValue
    {
    }
}
