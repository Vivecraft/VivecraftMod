package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class RenderModel_ControllerMode_State_t extends Structure
{
    public byte bScrollWheelVisible;

    public RenderModel_ControllerMode_State_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("bScrollWheelVisible");
    }

    public RenderModel_ControllerMode_State_t(byte bScrollWheelVisible)
    {
        this.bScrollWheelVisible = bScrollWheelVisible;
    }

    public RenderModel_ControllerMode_State_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends RenderModel_ControllerMode_State_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends RenderModel_ControllerMode_State_t implements com.sun.jna.Structure.ByValue
    {
    }
}
