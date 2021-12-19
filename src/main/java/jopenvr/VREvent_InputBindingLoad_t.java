package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_InputBindingLoad_t extends Structure
{
    public long ulAppContainer;
    public long pathMessage;
    public long pathUrl;
    public long pathControllerType;

    public VREvent_InputBindingLoad_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("ulAppContainer", "pathMessage", "pathUrl", "pathControllerType");
    }

    public VREvent_InputBindingLoad_t(long ulAppContainer, long pathMessage, long pathUrl, long pathControllerType)
    {
        this.ulAppContainer = ulAppContainer;
        this.pathMessage = pathMessage;
        this.pathUrl = pathUrl;
        this.pathControllerType = pathControllerType;
    }

    public VREvent_InputBindingLoad_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_InputBindingLoad_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_InputBindingLoad_t implements com.sun.jna.Structure.ByValue
    {
    }
}
