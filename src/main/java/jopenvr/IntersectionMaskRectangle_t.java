package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class IntersectionMaskRectangle_t extends Structure
{
    public float m_flTopLeftX;
    public float m_flTopLeftY;
    public float m_flWidth;
    public float m_flHeight;

    public IntersectionMaskRectangle_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_flTopLeftX", "m_flTopLeftY", "m_flWidth", "m_flHeight");
    }

    public IntersectionMaskRectangle_t(float m_flTopLeftX, float m_flTopLeftY, float m_flWidth, float m_flHeight)
    {
        this.m_flTopLeftX = m_flTopLeftX;
        this.m_flTopLeftY = m_flTopLeftY;
        this.m_flWidth = m_flWidth;
        this.m_flHeight = m_flHeight;
    }

    public IntersectionMaskRectangle_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends IntersectionMaskRectangle_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends IntersectionMaskRectangle_t implements com.sun.jna.Structure.ByValue
    {
    }
}
