package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRTextureWithPoseAndDepth_t extends Structure
{
    public VRTextureDepthInfo_t depth;

    public VRTextureWithPoseAndDepth_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("depth");
    }

    public VRTextureWithPoseAndDepth_t(VRTextureDepthInfo_t depth)
    {
        this.depth = depth;
    }

    public VRTextureWithPoseAndDepth_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRTextureWithPoseAndDepth_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRTextureWithPoseAndDepth_t implements com.sun.jna.Structure.ByValue
    {
    }
}
