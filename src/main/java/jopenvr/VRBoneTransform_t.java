package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRBoneTransform_t extends Structure
{
    public HmdVector4_t position;
    public HmdQuaternionf_t orientation;

    public VRBoneTransform_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("position", "orientation");
    }

    public VRBoneTransform_t(HmdVector4_t position, HmdQuaternionf_t orientation)
    {
        this.position = position;
        this.orientation = orientation;
    }

    public VRBoneTransform_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRBoneTransform_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRBoneTransform_t implements com.sun.jna.Structure.ByValue
    {
    }
}
