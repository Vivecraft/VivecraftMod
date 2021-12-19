package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_PerformanceTest_t extends Structure
{
    public int m_nFidelityLevel;

    public VREvent_PerformanceTest_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_nFidelityLevel");
    }

    public VREvent_PerformanceTest_t(int m_nFidelityLevel)
    {
        this.m_nFidelityLevel = m_nFidelityLevel;
    }

    public VREvent_PerformanceTest_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_PerformanceTest_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_PerformanceTest_t implements com.sun.jna.Structure.ByValue
    {
    }
}
