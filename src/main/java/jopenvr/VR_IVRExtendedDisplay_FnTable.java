package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVRExtendedDisplay_FnTable extends Structure
{
    public VR_IVRExtendedDisplay_FnTable.GetWindowBounds_callback GetWindowBounds;
    public VR_IVRExtendedDisplay_FnTable.GetEyeOutputViewport_callback GetEyeOutputViewport;
    public VR_IVRExtendedDisplay_FnTable.GetDXGIOutputInfo_callback GetDXGIOutputInfo;

    public VR_IVRExtendedDisplay_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetWindowBounds", "GetEyeOutputViewport", "GetDXGIOutputInfo");
    }

    public VR_IVRExtendedDisplay_FnTable(VR_IVRExtendedDisplay_FnTable.GetWindowBounds_callback GetWindowBounds, VR_IVRExtendedDisplay_FnTable.GetEyeOutputViewport_callback GetEyeOutputViewport, VR_IVRExtendedDisplay_FnTable.GetDXGIOutputInfo_callback GetDXGIOutputInfo)
    {
        this.GetWindowBounds = GetWindowBounds;
        this.GetEyeOutputViewport = GetEyeOutputViewport;
        this.GetDXGIOutputInfo = GetDXGIOutputInfo;
    }

    public VR_IVRExtendedDisplay_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRExtendedDisplay_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRExtendedDisplay_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface GetDXGIOutputInfo_callback extends Callback
    {
        void apply(IntByReference var1, IntByReference var2);
    }

    public interface GetEyeOutputViewport_callback extends Callback
    {
        void apply(int var1, IntByReference var2, IntByReference var3, IntByReference var4, IntByReference var5);
    }

    public interface GetWindowBounds_callback extends Callback
    {
        void apply(IntByReference var1, IntByReference var2, IntByReference var3, IntByReference var4);
    }
}
