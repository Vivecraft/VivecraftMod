package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class InputSkeletalActionData_t extends Structure
{
    public byte bActive;
    public long activeOrigin;

    public InputSkeletalActionData_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("bActive", "activeOrigin");
    }

    public InputSkeletalActionData_t(byte bActive, long activeOrigin)
    {
        this.bActive = bActive;
        this.activeOrigin = activeOrigin;
    }

    public InputSkeletalActionData_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends InputSkeletalActionData_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends InputSkeletalActionData_t implements com.sun.jna.Structure.ByValue
    {
    }
}
