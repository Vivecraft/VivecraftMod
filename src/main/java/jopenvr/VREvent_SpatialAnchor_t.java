package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_SpatialAnchor_t extends Structure
{
    public int unHandle;

    public VREvent_SpatialAnchor_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("unHandle");
    }

    public VREvent_SpatialAnchor_t(int unHandle)
    {
        this.unHandle = unHandle;
    }

    public VREvent_SpatialAnchor_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_SpatialAnchor_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_SpatialAnchor_t implements com.sun.jna.Structure.ByValue
    {
    }
}
