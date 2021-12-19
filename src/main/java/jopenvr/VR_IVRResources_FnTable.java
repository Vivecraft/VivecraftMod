package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VR_IVRResources_FnTable extends Structure
{
    public VR_IVRResources_FnTable.LoadSharedResource_callback LoadSharedResource;
    public VR_IVRResources_FnTable.GetResourceFullPath_callback GetResourceFullPath;

    public VR_IVRResources_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("LoadSharedResource", "GetResourceFullPath");
    }

    public VR_IVRResources_FnTable(VR_IVRResources_FnTable.LoadSharedResource_callback LoadSharedResource, VR_IVRResources_FnTable.GetResourceFullPath_callback GetResourceFullPath)
    {
        this.LoadSharedResource = LoadSharedResource;
        this.GetResourceFullPath = GetResourceFullPath;
    }

    public VR_IVRResources_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRResources_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRResources_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface GetResourceFullPath_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, Pointer var3, int var4);
    }

    public interface LoadSharedResource_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, int var3);
    }
}
