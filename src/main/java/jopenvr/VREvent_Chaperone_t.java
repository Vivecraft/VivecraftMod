package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Chaperone_t extends Structure
{
    public long m_nPreviousUniverse;
    public long m_nCurrentUniverse;

    public VREvent_Chaperone_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_nPreviousUniverse", "m_nCurrentUniverse");
    }

    public VREvent_Chaperone_t(long m_nPreviousUniverse, long m_nCurrentUniverse)
    {
        this.m_nPreviousUniverse = m_nPreviousUniverse;
        this.m_nCurrentUniverse = m_nCurrentUniverse;
    }

    public VREvent_Chaperone_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Chaperone_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Chaperone_t implements com.sun.jna.Structure.ByValue
    {
    }
}
