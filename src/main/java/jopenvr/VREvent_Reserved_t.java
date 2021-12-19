package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Reserved_t extends Structure
{
    public long reserved0;
    public long reserved1;
    public long reserved2;
    public long reserved3;
    public long reserved4;
    public long reserved5;

    public VREvent_Reserved_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("reserved0", "reserved1", "reserved2", "reserved3", "reserved4", "reserved5");
    }

    public VREvent_Reserved_t(long reserved0, long reserved1, long reserved2, long reserved3, long reserved4, long reserved5)
    {
        this.reserved0 = reserved0;
        this.reserved1 = reserved1;
        this.reserved2 = reserved2;
        this.reserved3 = reserved3;
        this.reserved4 = reserved4;
        this.reserved5 = reserved5;
    }

    public VREvent_Reserved_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Reserved_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Reserved_t implements com.sun.jna.Structure.ByValue
    {
    }
}
