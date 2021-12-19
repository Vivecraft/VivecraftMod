package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class RenderModel_Vertex_t extends Structure
{
    public HmdVector3_t vPosition;
    public HmdVector3_t vNormal;
    public float[] rfTextureCoord = new float[2];

    public RenderModel_Vertex_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("vPosition", "vNormal", "rfTextureCoord");
    }

    public RenderModel_Vertex_t(HmdVector3_t vPosition, HmdVector3_t vNormal, float[] rfTextureCoord)
    {
        this.vPosition = vPosition;
        this.vNormal = vNormal;

        if (rfTextureCoord.length != this.rfTextureCoord.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.rfTextureCoord = rfTextureCoord;
        }
    }

    public RenderModel_Vertex_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends RenderModel_Vertex_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends RenderModel_Vertex_t implements com.sun.jna.Structure.ByValue
    {
    }
}
