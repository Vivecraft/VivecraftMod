package org.vivecraft.api.jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class VR_IVRDriverManager_FnTable extends Structure
{
    public GetDriverCount_callback GetDriverCount;
    public GetDriverName_callback GetDriverName;
    public GetDriverHandle_callback GetDriverHandle;

    public VR_IVRDriverManager_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetDriverCount", "GetDriverName", "GetDriverHandle");
    }

    public VR_IVRDriverManager_FnTable(GetDriverCount_callback GetDriverCount, GetDriverName_callback GetDriverName, GetDriverHandle_callback GetDriverHandle)
    {
        this.GetDriverCount = GetDriverCount;
        this.GetDriverName = GetDriverName;
        this.GetDriverHandle = GetDriverHandle;
    }

    public VR_IVRDriverManager_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRDriverManager_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRDriverManager_FnTable implements Structure.ByValue
    {
    }

    public interface GetDriverCount_callback extends Callback
    {
        int apply();
    }

    public interface GetDriverHandle_callback extends Callback
    {
        long apply(Pointer var1);
    }

    public interface GetDriverName_callback extends Callback
    {
        int apply(int var1, Pointer var2, int var3);
    }
}
