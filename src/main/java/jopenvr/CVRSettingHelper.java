package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

public class CVRSettingHelper extends Structure
{
    public IntByReference m_pSettings;

    public CVRSettingHelper()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("m_pSettings");
    }

    public CVRSettingHelper(IntByReference m_pSettings)
    {
        this.m_pSettings = m_pSettings;
    }

    public CVRSettingHelper(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends CVRSettingHelper implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends CVRSettingHelper implements com.sun.jna.Structure.ByValue
    {
    }
}
