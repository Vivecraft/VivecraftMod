package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class IntersectionMaskCircle_t extends Structure
{
    public float m_flCenterX;
    public float m_flCenterY;
    public float m_flRadius;

    public IntersectionMaskCircle_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_flCenterX", "m_flCenterY", "m_flRadius");
    }

    public IntersectionMaskCircle_t(float m_flCenterX, float m_flCenterY, float m_flRadius)
    {
        this.m_flCenterX = m_flCenterX;
        this.m_flCenterY = m_flCenterY;
        this.m_flRadius = m_flRadius;
    }

    public IntersectionMaskCircle_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends IntersectionMaskCircle_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends IntersectionMaskCircle_t implements com.sun.jna.Structure.ByValue
    {
    }
}
