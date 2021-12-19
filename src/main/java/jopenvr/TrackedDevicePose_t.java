package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class TrackedDevicePose_t extends Structure
{
    public HmdMatrix34_t mDeviceToAbsoluteTracking;
    public HmdVector3_t vVelocity;
    public HmdVector3_t vAngularVelocity;
    public int eTrackingResult;
    public byte bPoseIsValid;
    public byte bDeviceIsConnected;

    public TrackedDevicePose_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("mDeviceToAbsoluteTracking", "vVelocity", "vAngularVelocity", "eTrackingResult", "bPoseIsValid", "bDeviceIsConnected");
    }

    public TrackedDevicePose_t(HmdMatrix34_t mDeviceToAbsoluteTracking, HmdVector3_t vVelocity, HmdVector3_t vAngularVelocity, int eTrackingResult, byte bPoseIsValid, byte bDeviceIsConnected)
    {
        this.mDeviceToAbsoluteTracking = mDeviceToAbsoluteTracking;
        this.vVelocity = vVelocity;
        this.vAngularVelocity = vAngularVelocity;
        this.eTrackingResult = eTrackingResult;
        this.bPoseIsValid = bPoseIsValid;
        this.bDeviceIsConnected = bDeviceIsConnected;
    }

    public TrackedDevicePose_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends TrackedDevicePose_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends TrackedDevicePose_t implements com.sun.jna.Structure.ByValue
    {
    }
}
