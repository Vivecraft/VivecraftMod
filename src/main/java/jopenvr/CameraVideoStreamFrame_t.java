package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class CameraVideoStreamFrame_t extends Structure
{
    public int m_nStreamFormat;
    public int m_nWidth;
    public int m_nHeight;
    public int m_nImageDataSize;
    public int m_nFrameSequence;
    public int m_nISPFrameTimeStamp;
    public int m_nISPReferenceTimeStamp;
    public int m_nSyncCounter;
    public int m_nCamSyncEvents;
    public int m_nExposureTime;
    public int m_nBufferIndex;
    public int m_nBufferCount;
    public double m_flFrameElapsedTime;
    public double m_flFrameCaptureTime;
    public long m_nFrameCaptureTicks;
    public byte m_bPoseIsValid;
    public HmdMatrix34_t m_matDeviceToAbsoluteTracking;
    public float[] m_Pad = new float[4];
    public Pointer m_pImageData;

    public CameraVideoStreamFrame_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_nStreamFormat", "m_nWidth", "m_nHeight", "m_nImageDataSize", "m_nFrameSequence", "m_nISPFrameTimeStamp", "m_nISPReferenceTimeStamp", "m_nSyncCounter", "m_nCamSyncEvents", "m_nExposureTime", "m_nBufferIndex", "m_nBufferCount", "m_flFrameElapsedTime", "m_flFrameCaptureTime", "m_nFrameCaptureTicks", "m_bPoseIsValid", "m_matDeviceToAbsoluteTracking", "m_Pad", "m_pImageData");
    }

    public CameraVideoStreamFrame_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends CameraVideoStreamFrame_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends CameraVideoStreamFrame_t implements com.sun.jna.Structure.ByValue
    {
    }
}
