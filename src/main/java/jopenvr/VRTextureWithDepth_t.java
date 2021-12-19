package jopenvr;

import com.sun.jna.Pointer;
import java.util.Arrays;
import java.util.List;

public class VRTextureWithDepth_t extends Texture_t
{
    public VRTextureDepthInfo_t depth;

    public VRTextureWithDepth_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("handle", "eType", "eColorSpace", "depth");
    }

    public VRTextureWithDepth_t(VRTextureDepthInfo_t depth)
    {
        this.depth = depth;
    }

    public VRTextureWithDepth_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRTextureWithDepth_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRTextureWithDepth_t implements com.sun.jna.Structure.ByValue
    {
    }
}
