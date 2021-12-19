package jopenvr;

import com.sun.jna.Pointer;
import java.util.Arrays;
import java.util.List;

public class VREvent_t extends MispackedStructure
{
    public int eventType;
    public int trackedDeviceIndex;
    public float eventAgeSeconds;
    public VREvent_Data_t data;

    public VREvent_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("eventType", "trackedDeviceIndex", "eventAgeSeconds", "data");
    }

    public VREvent_t(int eventType, int trackedDeviceIndex, float eventAgeSeconds, VREvent_Data_t data)
    {
        this.eventType = eventType;
        this.trackedDeviceIndex = trackedDeviceIndex;
        this.eventAgeSeconds = eventAgeSeconds;
        this.data = data;
    }

    public VREvent_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_t implements com.sun.jna.Structure.ByValue
    {
    }
}
