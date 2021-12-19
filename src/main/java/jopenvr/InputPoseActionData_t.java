package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class InputPoseActionData_t extends Structure
{
    public byte bActive;
    public long activeOrigin;
    public TrackedDevicePose_t pose;

    public InputPoseActionData_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("bActive", "activeOrigin", "pose");
    }

    public InputPoseActionData_t(byte bActive, long activeOrigin, TrackedDevicePose_t pose)
    {
        this.bActive = bActive;
        this.activeOrigin = activeOrigin;
        this.pose = pose;
    }

    public InputPoseActionData_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends InputPoseActionData_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends InputPoseActionData_t implements com.sun.jna.Structure.ByValue
    {
    }
}
