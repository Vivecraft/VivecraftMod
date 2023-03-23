package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Union;

public class VROverlayIntersectionMaskPrimitive_Data_t extends Union
{
    public IntersectionMaskRectangle_t m_Rectangle;
    public IntersectionMaskCircle_t m_Circle;

    public VROverlayIntersectionMaskPrimitive_Data_t()
    {
    }

    public VROverlayIntersectionMaskPrimitive_Data_t(IntersectionMaskRectangle_t m_Rectangle)
    {
        this.m_Rectangle = m_Rectangle;
        this.setType(IntersectionMaskRectangle_t.class);
    }

    public VROverlayIntersectionMaskPrimitive_Data_t(IntersectionMaskCircle_t m_Circle)
    {
        this.m_Circle = m_Circle;
        this.setType(IntersectionMaskCircle_t.class);
    }

    public VROverlayIntersectionMaskPrimitive_Data_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VROverlayIntersectionMaskPrimitive_Data_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VROverlayIntersectionMaskPrimitive_Data_t implements com.sun.jna.Structure.ByValue
    {
    }
}
