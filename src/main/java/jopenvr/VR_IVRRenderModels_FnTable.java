package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVRRenderModels_FnTable extends Structure
{
    public VR_IVRRenderModels_FnTable.LoadRenderModel_Async_callback LoadRenderModel_Async;
    public VR_IVRRenderModels_FnTable.FreeRenderModel_callback FreeRenderModel;
    public VR_IVRRenderModels_FnTable.LoadTexture_Async_callback LoadTexture_Async;
    public VR_IVRRenderModels_FnTable.FreeTexture_callback FreeTexture;
    public VR_IVRRenderModels_FnTable.LoadTextureD3D11_Async_callback LoadTextureD3D11_Async;
    public VR_IVRRenderModels_FnTable.LoadIntoTextureD3D11_Async_callback LoadIntoTextureD3D11_Async;
    public VR_IVRRenderModels_FnTable.FreeTextureD3D11_callback FreeTextureD3D11;
    public VR_IVRRenderModels_FnTable.GetRenderModelName_callback GetRenderModelName;
    public VR_IVRRenderModels_FnTable.GetRenderModelCount_callback GetRenderModelCount;
    public VR_IVRRenderModels_FnTable.GetComponentCount_callback GetComponentCount;
    public VR_IVRRenderModels_FnTable.GetComponentName_callback GetComponentName;
    public VR_IVRRenderModels_FnTable.GetComponentButtonMask_callback GetComponentButtonMask;
    public VR_IVRRenderModels_FnTable.GetComponentRenderModelName_callback GetComponentRenderModelName;
    public VR_IVRRenderModels_FnTable.GetComponentStateForDevicePath_callback GetComponentStateForDevicePath;
    public VR_IVRRenderModels_FnTable.GetComponentState_callback GetComponentState;
    public VR_IVRRenderModels_FnTable.RenderModelHasComponent_callback RenderModelHasComponent;
    public VR_IVRRenderModels_FnTable.GetRenderModelThumbnailURL_callback GetRenderModelThumbnailURL;
    public VR_IVRRenderModels_FnTable.GetRenderModelOriginalPath_callback GetRenderModelOriginalPath;
    public VR_IVRRenderModels_FnTable.GetRenderModelErrorNameFromEnum_callback GetRenderModelErrorNameFromEnum;

    public VR_IVRRenderModels_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("LoadRenderModel_Async", "FreeRenderModel", "LoadTexture_Async", "FreeTexture", "LoadTextureD3D11_Async", "LoadIntoTextureD3D11_Async", "FreeTextureD3D11", "GetRenderModelName", "GetRenderModelCount", "GetComponentCount", "GetComponentName", "GetComponentButtonMask", "GetComponentRenderModelName", "GetComponentStateForDevicePath", "GetComponentState", "RenderModelHasComponent", "GetRenderModelThumbnailURL", "GetRenderModelOriginalPath", "GetRenderModelErrorNameFromEnum");
    }

    public VR_IVRRenderModels_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRRenderModels_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRRenderModels_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface FreeRenderModel_callback extends Callback
    {
        void apply(RenderModel_t var1);
    }

    public interface FreeTextureD3D11_callback extends Callback
    {
        void apply(Pointer var1);
    }

    public interface FreeTexture_callback extends Callback
    {
        void apply(RenderModel_TextureMap_t var1);
    }

    public interface GetComponentButtonMask_callback extends Callback
    {
        long apply(Pointer var1, Pointer var2);
    }

    public interface GetComponentCount_callback extends Callback
    {
        int apply(Pointer var1);
    }

    public interface GetComponentName_callback extends Callback
    {
        int apply(Pointer var1, int var2, Pointer var3, int var4);
    }

    public interface GetComponentRenderModelName_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, Pointer var3, int var4);
    }

    public interface GetComponentStateForDevicePath_callback extends Callback
    {
        byte apply(Pointer var1, Pointer var2, long var3, RenderModel_ControllerMode_State_t var5, RenderModel_ComponentState_t var6);
    }

    public interface GetComponentState_callback extends Callback
    {
        byte apply(Pointer var1, Pointer var2, VRControllerState_t var3, RenderModel_ControllerMode_State_t var4, RenderModel_ComponentState_t var5);
    }

    public interface GetRenderModelCount_callback extends Callback
    {
        int apply();
    }

    public interface GetRenderModelErrorNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetRenderModelName_callback extends Callback
    {
        int apply(int var1, Pointer var2, int var3);
    }

    public interface GetRenderModelOriginalPath_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, int var3, IntByReference var4);
    }

    public interface GetRenderModelThumbnailURL_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, int var3, IntByReference var4);
    }

    public interface LoadIntoTextureD3D11_Async_callback extends Callback
    {
        int apply(int var1, Pointer var2);
    }

    public interface LoadRenderModel_Async_callback extends Callback
    {
        int apply(Pointer var1, PointerByReference var2);
    }

    public interface LoadTextureD3D11_Async_callback extends Callback
    {
        int apply(int var1, Pointer var2, PointerByReference var3);
    }

    public interface LoadTexture_Async_callback extends Callback
    {
        int apply(int var1, PointerByReference var2);
    }

    public interface RenderModelHasComponent_callback extends Callback
    {
        byte apply(Pointer var1, Pointer var2);
    }
}
