package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_WebConsole_t extends Structure
{
    public long webConsoleHandle;

    public VREvent_WebConsole_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("webConsoleHandle");
    }

    public VREvent_WebConsole_t(long webConsoleHandle)
    {
        this.webConsoleHandle = webConsoleHandle;
    }

    public VREvent_WebConsole_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_WebConsole_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_WebConsole_t implements com.sun.jna.Structure.ByValue
    {
    }
}
