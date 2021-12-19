package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_ScreenshotProgress_t extends Structure
{
    public float progress;

    public VREvent_ScreenshotProgress_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("progress");
    }

    public VREvent_ScreenshotProgress_t(float progress)
    {
        this.progress = progress;
    }

    public VREvent_ScreenshotProgress_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_ScreenshotProgress_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_ScreenshotProgress_t implements com.sun.jna.Structure.ByValue
    {
    }
}
