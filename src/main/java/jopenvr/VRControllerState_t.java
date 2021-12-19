package jopenvr;

import com.sun.jna.Pointer;
import java.util.Arrays;
import java.util.List;

public class VRControllerState_t extends MispackedStructure
{
    public int unPacketNum;
    public long ulButtonPressed;
    public long ulButtonTouched;
    public VRControllerAxis_t[] rAxis = new VRControllerAxis_t[5];

    public VRControllerState_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("unPacketNum", "ulButtonPressed", "ulButtonTouched", "rAxis");
    }

    public VRControllerState_t(int unPacketNum, long ulButtonPressed, long ulButtonTouched, VRControllerAxis_t[] rAxis)
    {
        this.unPacketNum = unPacketNum;
        this.ulButtonPressed = ulButtonPressed;
        this.ulButtonTouched = ulButtonTouched;

        if (rAxis.length != this.rAxis.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.rAxis = rAxis;
        }
    }

    public VRControllerState_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRControllerState_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRControllerState_t implements com.sun.jna.Structure.ByValue
    {
    }
}
