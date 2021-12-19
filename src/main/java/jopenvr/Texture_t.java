package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class Texture_t extends Structure
{
    public Pointer handle;
    public int eType;
    public int eColorSpace;

    public Texture_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("handle", "eType", "eColorSpace");
    }

    public Texture_t(Pointer handle, int eType, int eColorSpace)
    {
        this.handle = handle;
        this.eType = eType;
        this.eColorSpace = eColorSpace;
    }

    public Texture_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends Texture_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends Texture_t implements com.sun.jna.Structure.ByValue
    {
    }
}
