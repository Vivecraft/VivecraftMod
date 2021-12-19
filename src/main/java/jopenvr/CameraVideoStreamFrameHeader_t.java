package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class CameraVideoStreamFrameHeader_t extends Structure
{
    public int eFrameType;
    public int nWidth;
    public int nHeight;
    public int nBytesPerPixel;
    public int nFrameSequence;
    public TrackedDevicePose_t standingTrackedDevicePose;
    public long ulFrameExposureTime;

    public CameraVideoStreamFrameHeader_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("eFrameType", "nWidth", "nHeight", "nBytesPerPixel", "nFrameSequence", "standingTrackedDevicePose", "ulFrameExposureTime");
    }

    public CameraVideoStreamFrameHeader_t(int eFrameType, int nWidth, int nHeight, int nBytesPerPixel, int nFrameSequence, TrackedDevicePose_t standingTrackedDevicePose, long ulFrameExposureTime)
    {
        this.eFrameType = eFrameType;
        this.nWidth = nWidth;
        this.nHeight = nHeight;
        this.nBytesPerPixel = nBytesPerPixel;
        this.nFrameSequence = nFrameSequence;
        this.standingTrackedDevicePose = standingTrackedDevicePose;
        this.ulFrameExposureTime = ulFrameExposureTime;
    }

    public CameraVideoStreamFrameHeader_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends CameraVideoStreamFrameHeader_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends CameraVideoStreamFrameHeader_t implements com.sun.jna.Structure.ByValue
    {
    }
}
