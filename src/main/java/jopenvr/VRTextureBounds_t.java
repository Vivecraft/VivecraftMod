package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRTextureBounds_t extends Structure
{
    public float uMin;
    public float vMin;
    public float uMax;
    public float vMax;

    public VRTextureBounds_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("uMin", "vMin", "uMax", "vMax");
    }

    public VRTextureBounds_t(float uMin, float vMin, float uMax, float vMax)
    {
        this.uMin = uMin;
        this.vMin = vMin;
        this.uMax = uMax;
        this.vMax = vMax;
    }

    public VRTextureBounds_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRTextureBounds_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRTextureBounds_t implements com.sun.jna.Structure.ByValue
    {
    }
}
