package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_ApplicationLaunch_t extends Structure
{
    public int pid;
    public int unArgsHandle;

    public VREvent_ApplicationLaunch_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("pid", "unArgsHandle");
    }

    public VREvent_ApplicationLaunch_t(int pid, int unArgsHandle)
    {
        this.pid = pid;
        this.unArgsHandle = unArgsHandle;
    }

    public VREvent_ApplicationLaunch_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_ApplicationLaunch_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_ApplicationLaunch_t implements com.sun.jna.Structure.ByValue
    {
    }
}
