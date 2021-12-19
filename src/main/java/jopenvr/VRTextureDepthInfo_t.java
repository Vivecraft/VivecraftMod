package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRTextureDepthInfo_t extends Structure
{
    public Pointer handle;
    public HmdMatrix44_t mProjection;
    public HmdVector2_t vRange;

    public VRTextureDepthInfo_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("handle", "mProjection", "vRange");
    }

    public VRTextureDepthInfo_t(Pointer handle, HmdMatrix44_t mProjection, HmdVector2_t vRange)
    {
        this.handle = handle;
        this.mProjection = mProjection;
        this.vRange = vRange;
    }

    public VRTextureDepthInfo_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRTextureDepthInfo_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRTextureDepthInfo_t implements com.sun.jna.Structure.ByValue
    {
    }
}
