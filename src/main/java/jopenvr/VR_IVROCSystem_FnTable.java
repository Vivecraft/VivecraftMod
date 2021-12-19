package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VR_IVROCSystem_FnTable extends Structure
{
    public static final String Version = "FnTable:IVROCSystem_001";
    public VR_IVROCSystem_FnTable.GetExtendedButtonStatus_callback GetExtendedButtonStatus;

    public VR_IVROCSystem_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetExtendedButtonStatus");
    }

    public VR_IVROCSystem_FnTable(VR_IVROCSystem_FnTable.GetExtendedButtonStatus_callback GetExtendedButtonStatus)
    {
        this.GetExtendedButtonStatus = GetExtendedButtonStatus;
    }

    public VR_IVROCSystem_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVROCSystem_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVROCSystem_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface GetExtendedButtonStatus_callback extends Callback
    {
        long apply();
    }
}
