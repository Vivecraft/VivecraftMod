package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Keyboard_t extends Structure
{
    public byte[] cNewInput = new byte[8];
    public long uUserValue;

    public VREvent_Keyboard_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("cNewInput", "uUserValue");
    }

    public VREvent_Keyboard_t(byte[] cNewInput, long uUserValue)
    {
        if (cNewInput.length != this.cNewInput.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.cNewInput = cNewInput;
            this.uUserValue = uUserValue;
        }
    }

    public VREvent_Keyboard_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Keyboard_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Keyboard_t implements com.sun.jna.Structure.ByValue
    {
    }
}
