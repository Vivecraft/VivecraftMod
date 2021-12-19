package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_InputActionManifestLoad_t extends Structure
{
    public long pathAppKey;
    public long pathMessage;
    public long pathMessageParam;
    public long pathManifestPath;

    public VREvent_InputActionManifestLoad_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("pathAppKey", "pathMessage", "pathMessageParam", "pathManifestPath");
    }

    public VREvent_InputActionManifestLoad_t(long pathAppKey, long pathMessage, long pathMessageParam, long pathManifestPath)
    {
        this.pathAppKey = pathAppKey;
        this.pathMessage = pathMessage;
        this.pathMessageParam = pathMessageParam;
        this.pathManifestPath = pathManifestPath;
    }

    public VREvent_InputActionManifestLoad_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_InputActionManifestLoad_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_InputActionManifestLoad_t implements com.sun.jna.Structure.ByValue
    {
    }
}
