package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class InputOriginInfo_t extends Structure
{
    public long devicePath;
    public int trackedDeviceIndex;
    public byte[] rchRenderModelComponentName = new byte[128];

    public InputOriginInfo_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("devicePath", "trackedDeviceIndex", "rchRenderModelComponentName");
    }

    public InputOriginInfo_t(long devicePath, int trackedDeviceIndex, byte[] rchRenderModelComponentName)
    {
        this.devicePath = devicePath;
        this.trackedDeviceIndex = trackedDeviceIndex;

        if (rchRenderModelComponentName.length != this.rchRenderModelComponentName.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.rchRenderModelComponentName = rchRenderModelComponentName;
        }
    }

    public InputOriginInfo_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends InputOriginInfo_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends InputOriginInfo_t implements com.sun.jna.Structure.ByValue
    {
    }
}
