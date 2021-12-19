package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VROverlayIntersectionResults_t extends Structure
{
    public HmdVector3_t vPoint;
    public HmdVector3_t vNormal;
    public HmdVector2_t vUVs;
    public float fDistance;

    public VROverlayIntersectionResults_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("vPoint", "vNormal", "vUVs", "fDistance");
    }

    public VROverlayIntersectionResults_t(HmdVector3_t vPoint, HmdVector3_t vNormal, HmdVector2_t vUVs, float fDistance)
    {
        this.vPoint = vPoint;
        this.vNormal = vNormal;
        this.vUVs = vUVs;
        this.fDistance = fDistance;
    }

    public VROverlayIntersectionResults_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VROverlayIntersectionResults_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VROverlayIntersectionResults_t implements com.sun.jna.Structure.ByValue
    {
    }
}
