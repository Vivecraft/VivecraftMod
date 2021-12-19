package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class Compositor_OverlaySettings extends Structure
{
    public int size;
    public byte curved;
    public byte antialias;
    public float scale;
    public float distance;
    public float alpha;
    public float uOffset;
    public float vOffset;
    public float uScale;
    public float vScale;
    public float gridDivs;
    public float gridWidth;
    public float gridScale;
    public HmdMatrix44_t transform;

    public Compositor_OverlaySettings()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("size", "curved", "antialias", "scale", "distance", "alpha", "uOffset", "vOffset", "uScale", "vScale", "gridDivs", "gridWidth", "gridScale", "transform");
    }

    public Compositor_OverlaySettings(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends Compositor_OverlaySettings implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends Compositor_OverlaySettings implements com.sun.jna.Structure.ByValue
    {
    }
}
