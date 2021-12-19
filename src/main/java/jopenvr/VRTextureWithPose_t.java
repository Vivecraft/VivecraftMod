package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRTextureWithPose_t extends Structure
{
    public HmdMatrix34_t mDeviceToAbsoluteTracking;

    public VRTextureWithPose_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("mDeviceToAbsoluteTracking");
    }

    public VRTextureWithPose_t(HmdMatrix34_t mDeviceToAbsoluteTracking)
    {
        this.mDeviceToAbsoluteTracking = mDeviceToAbsoluteTracking;
    }

    public VRTextureWithPose_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRTextureWithPose_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRTextureWithPose_t implements com.sun.jna.Structure.ByValue
    {
    }
}
