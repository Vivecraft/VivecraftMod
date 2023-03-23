package jopenvr;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

abstract class MispackedStructure extends Structure
{
    protected MispackedStructure()
    {
    }

    protected MispackedStructure(Pointer peer)
    {
        super(peer);
    }

    protected int getNativeAlignment(Class type, Object value, boolean isFirstElement)
    {
        if (!Platform.isLinux() && !Platform.isMac())
        {
            return super.getNativeAlignment(type, value, isFirstElement);
        }
        else
        {
            int i = super.getNativeAlignment(type, value, isFirstElement);
            return i > 4 ? 4 : i;
        }
    }
}
