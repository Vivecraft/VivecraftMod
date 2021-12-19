package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class InputDigitalActionData_t extends Structure
{
    public byte bActive;
    public long activeOrigin;
    public byte bState;
    public byte bChanged;
    public float fUpdateTime;

    public InputDigitalActionData_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("bActive", "activeOrigin", "bState", "bChanged", "fUpdateTime");
    }

    public InputDigitalActionData_t(byte bActive, long activeOrigin, byte bState, byte bChanged, float fUpdateTime)
    {
        this.bActive = bActive;
        this.activeOrigin = activeOrigin;
        this.bState = bState;
        this.bChanged = bChanged;
        this.fUpdateTime = fUpdateTime;
    }

    public InputDigitalActionData_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends InputDigitalActionData_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends InputDigitalActionData_t implements com.sun.jna.Structure.ByValue
    {
    }
}
