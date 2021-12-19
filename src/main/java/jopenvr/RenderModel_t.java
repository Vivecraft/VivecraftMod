package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.ShortByReference;
import java.util.Arrays;
import java.util.List;

public class RenderModel_t extends MispackedStructure
{
    public RenderModel_Vertex_t.ByReference rVertexData;
    public int unVertexCount;
    public ShortByReference rIndexData;
    public int unTriangleCount;
    public int diffuseTextureId;

    public RenderModel_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("rVertexData", "unVertexCount", "rIndexData", "unTriangleCount", "diffuseTextureId");
    }

    public RenderModel_t(RenderModel_Vertex_t.ByReference rVertexData, int unVertexCount, ShortByReference rIndexData, int unTriangleCount, int diffuseTextureId)
    {
        this.rVertexData = rVertexData;
        this.unVertexCount = unVertexCount;
        this.rIndexData = rIndexData;
        this.unTriangleCount = unTriangleCount;
        this.diffuseTextureId = diffuseTextureId;
    }

    public RenderModel_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends RenderModel_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends RenderModel_t implements com.sun.jna.Structure.ByValue
    {
    }
}
