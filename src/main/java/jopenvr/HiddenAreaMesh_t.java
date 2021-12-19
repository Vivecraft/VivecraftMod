package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class HiddenAreaMesh_t extends Structure
{
    public HmdVector2_t.ByReference pVertexData;
    public int unTriangleCount;

    public HiddenAreaMesh_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("pVertexData", "unTriangleCount");
    }

    public HiddenAreaMesh_t(HmdVector2_t.ByReference pVertexData, int unTriangleCount)
    {
        this.pVertexData = pVertexData;
        this.unTriangleCount = unTriangleCount;
    }

    public HiddenAreaMesh_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends HiddenAreaMesh_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends HiddenAreaMesh_t implements com.sun.jna.Structure.ByValue
    {
    }
}
