package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_ProgressUpdate_t extends Structure
{
    public long ulApplicationPropertyContainer;
    public long pathDevice;
    public long pathInputSource;
    public long pathProgressAction;
    public long pathIcon;
    public float fProgress;

    public VREvent_ProgressUpdate_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("ulApplicationPropertyContainer", "pathDevice", "pathInputSource", "pathProgressAction", "pathIcon", "fProgress");
    }

    public VREvent_ProgressUpdate_t(long ulApplicationPropertyContainer, long pathDevice, long pathInputSource, long pathProgressAction, long pathIcon, float fProgress)
    {
        this.ulApplicationPropertyContainer = ulApplicationPropertyContainer;
        this.pathDevice = pathDevice;
        this.pathInputSource = pathInputSource;
        this.pathProgressAction = pathProgressAction;
        this.pathIcon = pathIcon;
        this.fProgress = fProgress;
    }

    public VREvent_ProgressUpdate_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_ProgressUpdate_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_ProgressUpdate_t implements com.sun.jna.Structure.ByValue
    {
    }
}
