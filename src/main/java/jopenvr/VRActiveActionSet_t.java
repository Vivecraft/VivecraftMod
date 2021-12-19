package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRActiveActionSet_t extends Structure
{
    public long ulActionSet;
    public long ulRestrictedToDevice;
    public long ulSecondaryActionSet;
    public int unPadding;
    public int nPriority;

    public VRActiveActionSet_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("ulActionSet", "ulRestrictedToDevice", "ulSecondaryActionSet", "unPadding", "nPriority");
    }

    public VRActiveActionSet_t(long ulActionSet, long ulRestrictedToDevice, long ulSecondaryActionSet, int unPadding, int nPriority)
    {
        this.ulActionSet = ulActionSet;
        this.ulRestrictedToDevice = ulRestrictedToDevice;
        this.ulSecondaryActionSet = ulSecondaryActionSet;
        this.unPadding = unPadding;
        this.nPriority = nPriority;
    }

    public VRActiveActionSet_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRActiveActionSet_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRActiveActionSet_t implements com.sun.jna.Structure.ByValue
    {
    }
}
