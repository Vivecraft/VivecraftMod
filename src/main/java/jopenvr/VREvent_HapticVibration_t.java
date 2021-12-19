package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_HapticVibration_t extends Structure
{
    public long containerHandle;
    public long componentHandle;
    public float fDurationSeconds;
    public float fFrequency;
    public float fAmplitude;

    public VREvent_HapticVibration_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("containerHandle", "componentHandle", "fDurationSeconds", "fFrequency", "fAmplitude");
    }

    public VREvent_HapticVibration_t(long containerHandle, long componentHandle, float fDurationSeconds, float fFrequency, float fAmplitude)
    {
        this.containerHandle = containerHandle;
        this.componentHandle = componentHandle;
        this.fDurationSeconds = fDurationSeconds;
        this.fFrequency = fFrequency;
        this.fAmplitude = fAmplitude;
    }

    public VREvent_HapticVibration_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_HapticVibration_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_HapticVibration_t implements com.sun.jna.Structure.ByValue
    {
    }
}
