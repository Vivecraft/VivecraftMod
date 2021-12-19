package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class InputAnalogActionData_t extends Structure
{
    public byte bActive;
    public long activeOrigin;
    public float x;
    public float y;
    public float z;
    public float deltaX;
    public float deltaY;
    public float deltaZ;
    public float fUpdateTime;

    public InputAnalogActionData_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("bActive", "activeOrigin", "x", "y", "z", "deltaX", "deltaY", "deltaZ", "fUpdateTime");
    }

    public InputAnalogActionData_t(byte bActive, long activeOrigin, float x, float y, float z, float deltaX, float deltaY, float deltaZ, float fUpdateTime)
    {
        this.bActive = bActive;
        this.activeOrigin = activeOrigin;
        this.x = x;
        this.y = y;
        this.z = z;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.fUpdateTime = fUpdateTime;
    }

    public InputAnalogActionData_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends InputAnalogActionData_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends InputAnalogActionData_t implements com.sun.jna.Structure.ByValue
    {
    }
}
