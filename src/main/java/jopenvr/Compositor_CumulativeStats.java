package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class Compositor_CumulativeStats extends Structure
{
    public int m_nPid;
    public int m_nNumFramePresents;
    public int m_nNumDroppedFrames;
    public int m_nNumReprojectedFrames;
    public int m_nNumFramePresentsOnStartup;
    public int m_nNumDroppedFramesOnStartup;
    public int m_nNumReprojectedFramesOnStartup;
    public int m_nNumLoading;
    public int m_nNumFramePresentsLoading;
    public int m_nNumDroppedFramesLoading;
    public int m_nNumReprojectedFramesLoading;
    public int m_nNumTimedOut;
    public int m_nNumFramePresentsTimedOut;
    public int m_nNumDroppedFramesTimedOut;
    public int m_nNumReprojectedFramesTimedOut;

    public Compositor_CumulativeStats()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_nPid", "m_nNumFramePresents", "m_nNumDroppedFrames", "m_nNumReprojectedFrames", "m_nNumFramePresentsOnStartup", "m_nNumDroppedFramesOnStartup", "m_nNumReprojectedFramesOnStartup", "m_nNumLoading", "m_nNumFramePresentsLoading", "m_nNumDroppedFramesLoading", "m_nNumReprojectedFramesLoading", "m_nNumTimedOut", "m_nNumFramePresentsTimedOut", "m_nNumDroppedFramesTimedOut", "m_nNumReprojectedFramesTimedOut");
    }

    public Compositor_CumulativeStats(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends Compositor_CumulativeStats implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends Compositor_CumulativeStats implements com.sun.jna.Structure.ByValue
    {
    }
}
