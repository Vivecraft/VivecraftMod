package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class AppOverrideKeys_t extends Structure
{
    public Pointer pchKey;
    public Pointer pchValue;

    public AppOverrideKeys_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("pchKey", "pchValue");
    }

    public AppOverrideKeys_t(Pointer pchKey, Pointer pchValue)
    {
        this.pchKey = pchKey;
        this.pchValue = pchValue;
    }

    public AppOverrideKeys_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends AppOverrideKeys_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends AppOverrideKeys_t implements com.sun.jna.Structure.ByValue
    {
    }
}
