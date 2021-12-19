package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_TouchPadMove_t extends Structure
{
    public byte bFingerDown;
    public float flSecondsFingerDown;
    public float fValueXFirst;
    public float fValueYFirst;
    public float fValueXRaw;
    public float fValueYRaw;

    public VREvent_TouchPadMove_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("bFingerDown", "flSecondsFingerDown", "fValueXFirst", "fValueYFirst", "fValueXRaw", "fValueYRaw");
    }

    public VREvent_TouchPadMove_t(byte bFingerDown, float flSecondsFingerDown, float fValueXFirst, float fValueYFirst, float fValueXRaw, float fValueYRaw)
    {
        this.bFingerDown = bFingerDown;
        this.flSecondsFingerDown = flSecondsFingerDown;
        this.fValueXFirst = fValueXFirst;
        this.fValueYFirst = fValueYFirst;
        this.fValueXRaw = fValueXRaw;
        this.fValueYRaw = fValueYRaw;
    }

    public VREvent_TouchPadMove_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_TouchPadMove_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_TouchPadMove_t implements com.sun.jna.Structure.ByValue
    {
    }
}
