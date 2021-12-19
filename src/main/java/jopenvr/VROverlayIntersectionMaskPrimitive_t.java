package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VROverlayIntersectionMaskPrimitive_t extends Structure
{
    public int m_nPrimitiveType;
    public VROverlayIntersectionMaskPrimitive_Data_t m_Primitive;

    public VROverlayIntersectionMaskPrimitive_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_nPrimitiveType", "m_Primitive");
    }

    public VROverlayIntersectionMaskPrimitive_t(int m_nPrimitiveType, VROverlayIntersectionMaskPrimitive_Data_t m_Primitive)
    {
        this.m_nPrimitiveType = m_nPrimitiveType;
        this.m_Primitive = m_Primitive;
    }

    public VROverlayIntersectionMaskPrimitive_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VROverlayIntersectionMaskPrimitive_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VROverlayIntersectionMaskPrimitive_t implements com.sun.jna.Structure.ByValue
    {
    }
}
