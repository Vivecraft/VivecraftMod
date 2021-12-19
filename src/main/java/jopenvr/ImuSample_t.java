package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class ImuSample_t extends Structure
{
    public double fSampleTime;
    public HmdVector3d_t vAccel;
    public HmdVector3d_t vGyro;
    public int unOffScaleFlags;

    public ImuSample_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("fSampleTime", "vAccel", "vGyro", "unOffScaleFlags");
    }

    public ImuSample_t(double fSampleTime, HmdVector3d_t vAccel, HmdVector3d_t vGyro, int unOffScaleFlags)
    {
        this.fSampleTime = fSampleTime;
        this.vAccel = vAccel;
        this.vGyro = vGyro;
        this.unOffScaleFlags = unOffScaleFlags;
    }

    public ImuSample_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends ImuSample_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends ImuSample_t implements com.sun.jna.Structure.ByValue
    {
    }
}
