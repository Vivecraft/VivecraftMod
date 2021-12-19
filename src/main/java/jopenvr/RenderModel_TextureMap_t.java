package jopenvr;

import com.sun.jna.Pointer;
import java.util.Arrays;
import java.util.List;

public class RenderModel_TextureMap_t extends MispackedStructure
{
    public short unWidth;
    public short unHeight;
    public Pointer rubTextureMapData;

    public RenderModel_TextureMap_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("unWidth", "unHeight", "rubTextureMapData");
    }

    public RenderModel_TextureMap_t(short unWidth, short unHeight, Pointer rubTextureMapData)
    {
        this.unWidth = unWidth;
        this.unHeight = unHeight;
        this.rubTextureMapData = rubTextureMapData;
    }

    public RenderModel_TextureMap_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends RenderModel_TextureMap_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends RenderModel_TextureMap_t implements com.sun.jna.Structure.ByValue
    {
    }
}
