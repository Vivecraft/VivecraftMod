package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class DriverDirectMode_FrameTiming extends Structure
{
    public int m_nSize;
    public int m_nNumFramePresents;
    public int m_nNumMisPresented;
    public int m_nNumDroppedFrames;
    public int m_nReprojectionFlags;

    public DriverDirectMode_FrameTiming()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_nSize", "m_nNumFramePresents", "m_nNumMisPresented", "m_nNumDroppedFrames", "m_nReprojectionFlags");
    }

    public DriverDirectMode_FrameTiming(int m_nSize, int m_nNumFramePresents, int m_nNumMisPresented, int m_nNumDroppedFrames, int m_nReprojectionFlags)
    {
        this.m_nSize = m_nSize;
        this.m_nNumFramePresents = m_nNumFramePresents;
        this.m_nNumMisPresented = m_nNumMisPresented;
        this.m_nNumDroppedFrames = m_nNumDroppedFrames;
        this.m_nReprojectionFlags = m_nReprojectionFlags;
    }

    public DriverDirectMode_FrameTiming(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends DriverDirectMode_FrameTiming implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends DriverDirectMode_FrameTiming implements com.sun.jna.Structure.ByValue
    {
    }
}
