package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Process_t extends Structure
{
    public int pid;
    public int oldPid;
    public byte bForced;
    public byte bConnectionLost;

    public VREvent_Process_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("pid", "oldPid", "bForced", "bConnectionLost");
    }

    public VREvent_Process_t(int pid, int oldPid, byte bForced, byte bConnectionLost)
    {
        this.pid = pid;
        this.oldPid = oldPid;
        this.bForced = bForced;
        this.bConnectionLost = bConnectionLost;
    }

    public VREvent_Process_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Process_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Process_t implements com.sun.jna.Structure.ByValue
    {
    }
}
