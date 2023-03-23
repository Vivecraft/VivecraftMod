package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRTrackedCamera_FnTable extends Structure
{
    public GetCameraErrorNameFromEnum_callback GetCameraErrorNameFromEnum;
    public HasCamera_callback HasCamera;
    public GetCameraFrameSize_callback GetCameraFrameSize;
    public GetCameraIntrinsics_callback GetCameraIntrinsics;
    public GetCameraProjection_callback GetCameraProjection;
    public AcquireVideoStreamingService_callback AcquireVideoStreamingService;
    public ReleaseVideoStreamingService_callback ReleaseVideoStreamingService;
    public GetVideoStreamFrameBuffer_callback GetVideoStreamFrameBuffer;
    public GetVideoStreamTextureSize_callback GetVideoStreamTextureSize;
    public GetVideoStreamTextureD3D11_callback GetVideoStreamTextureD3D11;
    public GetVideoStreamTextureGL_callback GetVideoStreamTextureGL;
    public ReleaseVideoStreamTextureGL_callback ReleaseVideoStreamTextureGL;

    public VR_IVRTrackedCamera_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetCameraErrorNameFromEnum", "HasCamera", "GetCameraFrameSize", "GetCameraIntrinsics", "GetCameraProjection", "AcquireVideoStreamingService", "ReleaseVideoStreamingService", "GetVideoStreamFrameBuffer", "GetVideoStreamTextureSize", "GetVideoStreamTextureD3D11", "GetVideoStreamTextureGL", "ReleaseVideoStreamTextureGL");
    }

    public VR_IVRTrackedCamera_FnTable(Pointer peer)
    {
        super(peer);
    }

    public interface AcquireVideoStreamingService_callback extends Callback
    {
        int apply(int var1, LongByReference var2);
    }

    public static class ByReference extends VR_IVRTrackedCamera_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRTrackedCamera_FnTable implements Structure.ByValue
    {
    }

    public interface GetCameraErrorNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetCameraFrameSize_callback extends Callback
    {
        int apply(int var1, int var2, IntByReference var3, IntByReference var4, IntByReference var5);
    }

    public interface GetCameraIntrinsics_callback extends Callback
    {
        int apply(int var1, int var2, int var3, HmdVector2_t var4, HmdVector2_t var5);
    }

    public interface GetCameraProjection_callback extends Callback
    {
        int apply(int var1, int var2, int var3, float var4, float var5, HmdMatrix44_t var6);
    }

    public interface GetVideoStreamFrameBuffer_callback extends Callback
    {
        int apply(long var1, int var3, Pointer var4, int var5, CameraVideoStreamFrameHeader_t var6, int var7);
    }

    public interface GetVideoStreamTextureD3D11_callback extends Callback
    {
        int apply(long var1, int var3, Pointer var4, PointerByReference var5, CameraVideoStreamFrameHeader_t var6, int var7);
    }

    public interface GetVideoStreamTextureGL_callback extends Callback
    {
        int apply(long var1, int var3, IntByReference var4, CameraVideoStreamFrameHeader_t var5, int var6);
    }

    public interface GetVideoStreamTextureSize_callback extends Callback
    {
        int apply(int var1, int var2, VRTextureBounds_t var3, IntByReference var4, IntByReference var5);
    }

    public interface HasCamera_callback extends Callback
    {
        int apply(int var1, Pointer var2);
    }

    public interface ReleaseVideoStreamTextureGL_callback extends Callback
    {
        int apply(long var1, int var3);
    }

    public interface ReleaseVideoStreamingService_callback extends Callback
    {
        int apply(long var1);
    }
}
