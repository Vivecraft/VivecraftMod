package org.vivecraft.client_vr.provider.openxr_lwjgl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.WGLNVDXInterop;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.windows.WinBase;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Helper class for D3D11 interop with OpenGL via WGL_NV_DX_interop2.
 *
 * The Oculus OpenXR runtime doesn't properly support XR_KHR_opengl_enable on desktop
 * (returns XR_ERROR_GRAPHICS_DEVICE_INVALID for all OpenGL bindings). As a workaround,
 * we create the OpenXR session using XR_KHR_D3D11_enable, then use WGL_NV_DX_interop2
 * to share the D3D11 swapchain textures with OpenGL for rendering.
 *
 * Flow:
 * 1. D3D11CreateDevice() -> ID3D11Device*
 * 2. xrCreateSession with XrGraphicsBindingD3D11KHR
 * 3. xrCreateSwapchain -> D3D11 texture2D swapchain images
 * 4. wglDXOpenDeviceNV(d3d11Device) -> interop handle
 * 5. For each swapchain image: wglDXRegisterObjectNV -> GL texture
 * 6. Per-frame: lock, render to GL texture, unlock, submit to OpenXR
 */
public class D3D11InteropHelper {

    // D3D11 constants
    private static final int D3D_DRIVER_TYPE_UNKNOWN = 0;
    private static final int D3D_DRIVER_TYPE_HARDWARE = 1;
    private static final int D3D_FEATURE_LEVEL_11_0 = 0xb000;
    private static final int D3D_FEATURE_LEVEL_11_1 = 0xb100;
    private static final int D3D11_SDK_VERSION = 7;

    // D3D11_BIND_FLAG
    private static final int D3D11_BIND_SHADER_RESOURCE = 0x8;
    private static final int D3D11_BIND_RENDER_TARGET = 0x20;

    // D3D11_USAGE
    private static final int D3D11_USAGE_DEFAULT = 0;

    // D3D11_RESOURCE_MISC_FLAG
    private static final int D3D11_RESOURCE_MISC_SHARED = 0x2;
    private static final int D3D11_RESOURCE_MISC_SHARED_NTHANDLE = 0x800;

    // ID3D11Device vtable indices (IUnknown: 0-2, then ID3D11Device methods)
    // ID3D11Device::CreateTexture2D is vtable index 5
    private static final int ID3D11DEVICE_CREATE_TEXTURE2D_VTABLE_INDEX = 5;

    // ID3D11DeviceContext vtable indices
    // ID3D11DeviceContext::CopyResource is vtable index 47
    private static final int ID3D11DEVICECONTEXT_COPY_RESOURCE_VTABLE_INDEX = 47;

    // Extension number 28: base = 1000000000 + (28-1)*1000 = 1000027000
    // XR_TYPE_GRAPHICS_BINDING_D3D11_KHR = 1000027000
    static final int XR_TYPE_GRAPHICS_BINDING_D3D11_KHR = 1000027000;
    // XR_TYPE_SWAPCHAIN_IMAGE_D3D11_KHR = 1000027001
    static final int XR_TYPE_SWAPCHAIN_IMAGE_D3D11_KHR = 1000027001;
    // XR_TYPE_GRAPHICS_REQUIREMENTS_D3D11_KHR = 1000027002
    static final int XR_TYPE_GRAPHICS_REQUIREMENTS_D3D11_KHR = 1000027002;

    // D3D11 COM method vtable offsets (IUnknown base: QueryInterface=0, AddRef=1, Release=2)
    private static final int IUNKNOWN_RELEASE_VTABLE_INDEX = 2;

    // Handles
    private long d3d11Device;        // ID3D11Device*
    private long d3d11Context;       // ID3D11DeviceContext*
    private long dxgiAdapter;        // IDXGIAdapter* (from the runtime's LUID)
    private long dxInteropHandle;    // WGL interop device handle from wglDXOpenDeviceNV
    private long d3d11DllHandle;     // d3d11.dll module handle
    private long dxgiDllHandle;      // dxgi.dll module handle

    // Function pointers
    private long d3d11CreateDevicePtr;
    private long createDXGIFactoryPtr;

    /**
     * Creates a D3D11 device on the adapter specified by the given LUID.
     * The LUID comes from xrGetD3D11GraphicsRequirementsKHR.
     *
     * @param adapterLuidLow  low 32 bits of the adapter LUID
     * @param adapterLuidHigh high 32 bits of the adapter LUID
     */
    public void createD3D11Device(int adapterLuidLow, int adapterLuidHigh) throws Exception {
        VRSettings.LOGGER.info("Vivecraft: Creating D3D11 device for adapter LUID: 0x{}{}",
            String.format("%08X", adapterLuidHigh), String.format("%08X", adapterLuidLow));

        // Load d3d11.dll
        this.d3d11DllHandle = WinBase.LoadLibrary("d3d11");
        if (this.d3d11DllHandle == 0) {
            throw new Exception("Failed to load d3d11.dll");
        }

        this.d3d11CreateDevicePtr = WinBase.GetProcAddress(this.d3d11DllHandle, "D3D11CreateDevice");
        if (this.d3d11CreateDevicePtr == 0) {
            throw new Exception("Failed to find D3D11CreateDevice in d3d11.dll");
        }

        // Load dxgi.dll for adapter enumeration
        this.dxgiDllHandle = WinBase.LoadLibrary("dxgi");
        if (this.dxgiDllHandle == 0) {
            throw new Exception("Failed to load dxgi.dll");
        }

        this.createDXGIFactoryPtr = WinBase.GetProcAddress(this.dxgiDllHandle, "CreateDXGIFactory1");
        if (this.createDXGIFactoryPtr == 0) {
            throw new Exception("Failed to find CreateDXGIFactory1 in dxgi.dll");
        }

        try (MemoryStack stack = stackPush()) {
            // Find the DXGI adapter matching the requested LUID
            long adapter = findAdapterByLuid(stack, adapterLuidLow, adapterLuidHigh);

            // D3D11CreateDevice parameters:
            // HRESULT D3D11CreateDevice(
            //   IDXGIAdapter *pAdapter,          // adapter (or null)
            //   D3D_DRIVER_TYPE DriverType,       // UNKNOWN when adapter specified, HARDWARE when null
            //   HMODULE Software,                 // null
            //   UINT Flags,                       // 0
            //   D3D_FEATURE_LEVEL *pFeatureLevels,// feature level array
            //   UINT FeatureLevels,               // count
            //   UINT SDKVersion,                  // D3D11_SDK_VERSION = 7
            //   ID3D11Device **ppDevice,           // out
            //   D3D_FEATURE_LEVEL *pFeatureLevel, // out (can be null)
            //   ID3D11DeviceContext **ppImmediateContext // out
            // )
            IntBuffer featureLevels = stack.ints(D3D_FEATURE_LEVEL_11_1, D3D_FEATURE_LEVEL_11_0);
            PointerBuffer ppDevice = stack.callocPointer(1);
            IntBuffer pFeatureLevel = stack.callocInt(1);
            PointerBuffer ppContext = stack.callocPointer(1);

            int driverType = adapter != 0 ? D3D_DRIVER_TYPE_UNKNOWN : D3D_DRIVER_TYPE_HARDWARE;

            // HRESULT D3D11CreateDevice(ptr, enum, ptr, uint, ptr, uint, uint, ptr, ptr, ptr)
            // We widen enum/uint params to long to match LWJGL JNI's callPPPPPPPPPI overload.
            // On x64 Windows this is safe as all params pass in 64-bit registers/stack slots.
            int hr = JNI.callPPPPPPPPPI(
                adapter,                                           // pAdapter (P)
                (long) driverType,                                 // DriverType (P, widened)
                0L,                                                // Software (P, null)
                0L,                                                // Flags (P, widened)
                MemoryUtil.memAddress(featureLevels),               // pFeatureLevels (P)
                (long) featureLevels.remaining(),                  // FeatureLevels count (P, widened)
                D3D11_SDK_VERSION,                                 // SDKVersion (int)
                MemoryUtil.memAddress(ppDevice),                    // ppDevice (P)
                MemoryUtil.memAddress(pFeatureLevel),               // pFeatureLevel (P)
                MemoryUtil.memAddress(ppContext),                    // ppImmediateContext (P)
                this.d3d11CreateDevicePtr
            );

            if (hr < 0) {
                throw new Exception("D3D11CreateDevice failed: HRESULT 0x" + Integer.toHexString(hr));
            }

            this.d3d11Device = ppDevice.get(0);
            this.d3d11Context = ppContext.get(0);
            this.dxgiAdapter = adapter;

            int featureLevel = pFeatureLevel.get(0);
            VRSettings.LOGGER.info("Vivecraft: D3D11 device created. Feature level: 0x{}, device ptr: 0x{}",
                Integer.toHexString(featureLevel), Long.toHexString(this.d3d11Device));
        }
    }

    /**
     * Creates a D3D11 device using the default adapter (no LUID matching).
     * Fallback when we can't get the LUID from the runtime.
     */
    public void createD3D11DeviceDefault() throws Exception {
        VRSettings.LOGGER.info("Vivecraft: Creating D3D11 device with default adapter");

        // Load d3d11.dll
        this.d3d11DllHandle = WinBase.LoadLibrary("d3d11");
        if (this.d3d11DllHandle == 0) {
            throw new Exception("Failed to load d3d11.dll");
        }

        this.d3d11CreateDevicePtr = WinBase.GetProcAddress(this.d3d11DllHandle, "D3D11CreateDevice");
        if (this.d3d11CreateDevicePtr == 0) {
            throw new Exception("Failed to find D3D11CreateDevice in d3d11.dll");
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer featureLevels = stack.ints(D3D_FEATURE_LEVEL_11_1, D3D_FEATURE_LEVEL_11_0);
            PointerBuffer ppDevice = stack.callocPointer(1);
            IntBuffer pFeatureLevel = stack.callocInt(1);
            PointerBuffer ppContext = stack.callocPointer(1);

            int hr = JNI.callPPPPPPPPPI(
                0L,                                                // pAdapter (P, null = default)
                (long) D3D_DRIVER_TYPE_HARDWARE,                   // DriverType (P, widened)
                0L,                                                // Software (P)
                0L,                                                // Flags (P, widened)
                MemoryUtil.memAddress(featureLevels),               // pFeatureLevels (P)
                (long) featureLevels.remaining(),                  // FeatureLevels count (P, widened)
                D3D11_SDK_VERSION,                                 // SDKVersion (int)
                MemoryUtil.memAddress(ppDevice),                    // ppDevice (P)
                MemoryUtil.memAddress(pFeatureLevel),               // pFeatureLevel (P)
                MemoryUtil.memAddress(ppContext),                    // ppImmediateContext (P)
                this.d3d11CreateDevicePtr
            );

            if (hr < 0) {
                throw new Exception("D3D11CreateDevice failed: HRESULT 0x" + Integer.toHexString(hr));
            }

            this.d3d11Device = ppDevice.get(0);
            this.d3d11Context = ppContext.get(0);

            int featureLevel = pFeatureLevel.get(0);
            VRSettings.LOGGER.info("Vivecraft: D3D11 device created (default adapter). Feature level: 0x{}, device ptr: 0x{}",
                Integer.toHexString(featureLevel), Long.toHexString(this.d3d11Device));
        }
    }

    /**
     * Finds a DXGI adapter matching the given LUID.
     */
    private long findAdapterByLuid(MemoryStack stack, int luidLow, int luidHigh) throws Exception {
        if (this.createDXGIFactoryPtr == 0) {
            VRSettings.LOGGER.warn("Vivecraft: CreateDXGIFactory1 not available, using null adapter");
            return 0;
        }

        // IID_IDXGIFactory1 = {770aae78-f26f-4dba-a829-253c83d1b387}
        ByteBuffer iidFactory = stack.calloc(16);
        iidFactory.putInt(0, 0x770aae78);
        iidFactory.putShort(4, (short) 0xf26f);
        iidFactory.putShort(6, (short) 0x4dba);
        iidFactory.put(8, (byte) 0xa8);
        iidFactory.put(9, (byte) 0x29);
        iidFactory.put(10, (byte) 0x25);
        iidFactory.put(11, (byte) 0x3c);
        iidFactory.put(12, (byte) 0x83);
        iidFactory.put(13, (byte) 0xd1);
        iidFactory.put(14, (byte) 0xb3);
        iidFactory.put(15, (byte) 0x87);

        PointerBuffer ppFactory = stack.callocPointer(1);

        // HRESULT CreateDXGIFactory1(REFIID riid, void **ppFactory)
        int hr = JNI.callPPI(
            MemoryUtil.memAddress(iidFactory),
            MemoryUtil.memAddress(ppFactory),
            this.createDXGIFactoryPtr
        );

        if (hr < 0) {
            VRSettings.LOGGER.warn("Vivecraft: CreateDXGIFactory1 failed: HRESULT 0x{}", Integer.toHexString(hr));
            return 0;
        }

        long factory = ppFactory.get(0);
        if (factory == 0) return 0;

        try {
            // Enumerate adapters: IDXGIFactory1::EnumAdapters1 is vtable index 12
            // (IUnknown: 3 + IDXGIObject: 4 + IDXGIFactory: 4 + IDXGIFactory1: 1 more = index 12)
            // Actually: IUnknown(3) + IDXGIObject(2) + IDXGIFactory(5) + IDXGIFactory1(1)
            // IDXGIFactory1::EnumAdapters1 is at vtable index 12
            long vtable = MemoryUtil.memGetAddress(factory);

            // IDXGIFactory::EnumAdapters is at index 7, IDXGIFactory1::EnumAdapters1 is at index 12
            long enumAdapters1Ptr = MemoryUtil.memGetAddress(vtable + 12L * Long.BYTES);

            for (int i = 0; i < 16; i++) {
                PointerBuffer ppAdapter = stack.callocPointer(1);
                // HRESULT EnumAdapters1(UINT Adapter, IDXGIAdapter1 **ppAdapter)
                hr = JNI.callPPI(factory, i, MemoryUtil.memAddress(ppAdapter), enumAdapters1Ptr);
                if (hr < 0) break; // DXGI_ERROR_NOT_FOUND

                long adapter = ppAdapter.get(0);
                if (adapter == 0) continue;

                // Get adapter desc: IDXGIAdapter1::GetDesc1 is at vtable index 10
                // IUnknown(3) + IDXGIObject(2) + IDXGIAdapter(3) + IDXGIAdapter1(2)
                // GetDesc1 is index 10
                long adapterVtable = MemoryUtil.memGetAddress(adapter);
                long getDesc1Ptr = MemoryUtil.memGetAddress(adapterVtable + 10L * Long.BYTES);

                // DXGI_ADAPTER_DESC1 is 312 bytes
                ByteBuffer desc = stack.calloc(312);
                hr = JNI.callPPI(adapter, MemoryUtil.memAddress(desc), getDesc1Ptr);

                if (hr >= 0) {
                    // DXGI_ADAPTER_DESC1 layout:
                    // WCHAR Description[128] = 256 bytes (offset 0)
                    // UINT VendorId (offset 256)
                    // UINT DeviceId (offset 260)
                    // UINT SubSysId (offset 264)
                    // UINT Revision (offset 268)
                    // SIZE_T DedicatedVideoMemory (offset 272, 8 bytes on 64-bit)
                    // SIZE_T DedicatedSystemMemory (offset 280)
                    // SIZE_T SharedSystemMemory (offset 288)
                    // LUID AdapterLuid (offset 296, 8 bytes: 4 low + 4 high)
                    int adapterLuidLow = desc.getInt(296);
                    int adapterLuidHigh = desc.getInt(300);

                    // Read description (WCHAR = UTF-16LE)
                    StringBuilder descStr = new StringBuilder();
                    for (int c = 0; c < 128; c++) {
                        char ch = desc.getChar(c * 2);
                        if (ch == 0) break;
                        descStr.append(ch);
                    }

                    VRSettings.LOGGER.info("Vivecraft: DXGI Adapter [{}]: {} (LUID: 0x{}{})",
                        i, descStr, String.format("%08X", adapterLuidHigh), String.format("%08X", adapterLuidLow));

                    if (adapterLuidLow == luidLow && adapterLuidHigh == luidHigh) {
                        VRSettings.LOGGER.info("Vivecraft: Found matching adapter!");
                        return adapter;
                    }
                }

                // Release this adapter since it's not the one we want
                comRelease(adapter);
            }
        } finally {
            // Release factory
            comRelease(factory);
        }

        VRSettings.LOGGER.warn("Vivecraft: No DXGI adapter matched LUID 0x{}{}, using null adapter",
            String.format("%08X", luidHigh), String.format("%08X", luidLow));
        return 0;
    }

    /**
     * Opens the WGL_NV_DX_interop device for the D3D11 device.
     */
    public void openDXInterop() throws Exception {
        if (this.d3d11Device == 0) {
            throw new Exception("D3D11 device not created");
        }

        this.dxInteropHandle = WGLNVDXInterop.wglDXOpenDeviceNV(this.d3d11Device);
        if (this.dxInteropHandle == 0) {
            throw new Exception("wglDXOpenDeviceNV failed. Is WGL_NV_DX_interop2 supported?");
        }

        VRSettings.LOGGER.info("Vivecraft: WGL DX interop opened. Handle: 0x{}",
            Long.toHexString(this.dxInteropHandle));
    }

    /**
     * Registers a D3D11 texture as an OpenGL texture via WGL_NV_DX_interop2.
     *
     * @param d3d11Texture the D3D11 texture pointer (ID3D11Texture2D*)
     * @param glTexture    the OpenGL texture name (pre-created with glGenTextures)
     * @return the interop object handle (for lock/unlock)
     */
    public long registerTexture(long d3d11Texture, int glTexture) {
        long handle = WGLNVDXInterop.wglDXRegisterObjectNV(
            this.dxInteropHandle,
            d3d11Texture,
            glTexture,
            GL_TEXTURE_2D,
            WGLNVDXInterop.WGL_ACCESS_WRITE_DISCARD_NV
        );
        if (handle == 0) {
            VRSettings.LOGGER.error("Vivecraft: wglDXRegisterObjectNV failed for D3D11 texture 0x{} -> GL {}",
                Long.toHexString(d3d11Texture), glTexture);
        }
        return handle;
    }

    /**
     * Locks interop objects for OpenGL access.
     */
    public boolean lockObjects(long... handles) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer hObjects = stack.callocPointer(handles.length);
            for (long h : handles) {
                hObjects.put(h);
            }
            hObjects.flip();
            return WGLNVDXInterop.wglDXLockObjectsNV(this.dxInteropHandle, hObjects);
        }
    }

    /**
     * Unlocks interop objects after OpenGL rendering.
     */
    public boolean unlockObjects(long... handles) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer hObjects = stack.callocPointer(handles.length);
            for (long h : handles) {
                hObjects.put(h);
            }
            hObjects.flip();
            return WGLNVDXInterop.wglDXUnlockObjectsNV(this.dxInteropHandle, hObjects);
        }
    }

    /**
     * Unregisters a D3D11 texture from OpenGL interop.
     */
    public void unregisterTexture(long interopHandle) {
        if (interopHandle != 0 && this.dxInteropHandle != 0) {
            WGLNVDXInterop.wglDXUnregisterObjectNV(this.dxInteropHandle, interopHandle);
        }
    }

    /**
     * Creates a D3D11 Texture2D that we own (not OpenXR runtime-owned).
     * These intermediate textures are created with SHARED flag so that
     * WGL_NV_DX_interop2 can register them, unlike the runtime-owned swapchain textures.
     *
     * D3D11_TEXTURE2D_DESC layout (44 bytes, padded to 48):
     *   UINT Width;                   // offset 0
     *   UINT Height;                  // offset 4
     *   UINT MipLevels;               // offset 8
     *   UINT ArraySize;               // offset 12
     *   DXGI_FORMAT Format;           // offset 16
     *   DXGI_SAMPLE_DESC SampleDesc;  // offset 20 (Count=4bytes, Quality=4bytes)
     *   D3D11_USAGE Usage;            // offset 28
     *   UINT BindFlags;               // offset 32
     *   UINT CPUAccessFlags;          // offset 36
     *   UINT MiscFlags;               // offset 40
     * Total: 44 bytes
     *
     * @param width      texture width
     * @param height     texture height
     * @param dxgiFormat DXGI_FORMAT enum value (e.g., 28 for R8G8B8A8_UNORM)
     * @return pointer to the created ID3D11Texture2D, or 0 on failure
     */
    public long createTexture2D(int width, int height, int dxgiFormat) {
        if (this.d3d11Device == 0) {
            VRSettings.LOGGER.error("Vivecraft: Cannot create texture, D3D11 device is null");
            return 0;
        }

        try (MemoryStack stack = stackPush()) {
            // D3D11_TEXTURE2D_DESC is 44 bytes
            ByteBuffer desc = stack.calloc(44);
            desc.putInt(0, width);                     // Width
            desc.putInt(4, height);                    // Height
            desc.putInt(8, 1);                         // MipLevels
            desc.putInt(12, 1);                        // ArraySize
            desc.putInt(16, dxgiFormat);               // Format
            desc.putInt(20, 1);                        // SampleDesc.Count
            desc.putInt(24, 0);                        // SampleDesc.Quality
            desc.putInt(28, D3D11_USAGE_DEFAULT);      // Usage
            desc.putInt(32, D3D11_BIND_SHADER_RESOURCE | D3D11_BIND_RENDER_TARGET); // BindFlags
            desc.putInt(36, 0);                        // CPUAccessFlags
            desc.putInt(40, D3D11_RESOURCE_MISC_SHARED); // MiscFlags - CRITICAL for interop!

            PointerBuffer ppTexture = stack.callocPointer(1);

            // ID3D11Device::CreateTexture2D(this, pDesc, pInitialData, ppTexture2D)
            // Vtable: IUnknown(3) + ID3D11Device starts at 3
            // CreateBuffer=3, CreateTexture1D=4, CreateTexture2D=5
            long vtable = MemoryUtil.memGetAddress(this.d3d11Device);
            long createTexture2DPtr = MemoryUtil.memGetAddress(vtable +
                (long) ID3D11DEVICE_CREATE_TEXTURE2D_VTABLE_INDEX * Long.BYTES);

            // HRESULT CreateTexture2D(ID3D11Device* this, D3D11_TEXTURE2D_DESC* pDesc,
            //                         D3D11_SUBRESOURCE_DATA* pInitialData, ID3D11Texture2D** ppTexture2D)
            int hr = JNI.callPPPPI(
                this.d3d11Device,
                MemoryUtil.memAddress(desc),
                0L,  // pInitialData = null
                MemoryUtil.memAddress(ppTexture),
                createTexture2DPtr
            );

            if (hr < 0) {
                VRSettings.LOGGER.error("Vivecraft: CreateTexture2D failed: HRESULT 0x{}",
                    Integer.toHexString(hr));
                return 0;
            }

            long texture = ppTexture.get(0);
            VRSettings.LOGGER.info("Vivecraft: Created intermediate D3D11 texture: 0x{} ({}x{}, format={})",
                Long.toHexString(texture), width, height, dxgiFormat);
            return texture;
        }
    }

    /**
     * Copies the contents of one D3D11 resource to another using ID3D11DeviceContext::CopyResource.
     * This is used to copy from our interop-registered intermediate texture to the runtime-owned
     * swapchain texture (or vice versa).
     *
     * @param dstResource destination D3D11 resource pointer (ID3D11Resource*)
     * @param srcResource source D3D11 resource pointer (ID3D11Resource*)
     */
    public void copyResource(long dstResource, long srcResource) {
        if (this.d3d11Context == 0) {
            VRSettings.LOGGER.error("Vivecraft: Cannot copy resource, D3D11 context is null");
            return;
        }

        // ID3D11DeviceContext::CopyResource(this, pDstResource, pSrcResource)
        long vtable = MemoryUtil.memGetAddress(this.d3d11Context);
        long copyResourcePtr = MemoryUtil.memGetAddress(vtable +
            (long) ID3D11DEVICECONTEXT_COPY_RESOURCE_VTABLE_INDEX * Long.BYTES);

        // void CopyResource(ID3D11DeviceContext* this, ID3D11Resource* pDst, ID3D11Resource* pSrc)
        JNI.callPPPV(
            this.d3d11Context,
            dstResource,
            srcResource,
            copyResourcePtr
        );
    }

    /**
     * Releases a COM object (e.g., an intermediate texture we created).
     */
    public static void releaseTexture(long texture) {
        if (texture != 0) {
            comRelease(texture);
        }
    }

    /**
     * Gets the D3D11 device pointer.
     */
    public long getD3D11Device() {
        return this.d3d11Device;
    }

    /**
     * Builds an XrGraphicsBindingD3D11KHR struct as a raw ByteBuffer.
     * Layout (on 64-bit):
     *   XrStructureType type;     // offset 0, 4 bytes
     *   [4 bytes padding]
     *   const void* next;         // offset 8, 8 bytes
     *   ID3D11Device* device;     // offset 16, 8 bytes
     * Total: 24 bytes
     */
    public ByteBuffer createGraphicsBinding() {
        ByteBuffer binding = MemoryUtil.memCalloc(24);
        binding.putInt(0, XR_TYPE_GRAPHICS_BINDING_D3D11_KHR);
        // next = null (already zeroed)
        MemoryUtil.memPutAddress(MemoryUtil.memAddress(binding) + 8, 0L);  // next
        MemoryUtil.memPutAddress(MemoryUtil.memAddress(binding) + 16, this.d3d11Device); // device
        return binding;
    }

    /**
     * Builds an XrGraphicsRequirementsD3D11KHR struct as a raw ByteBuffer for output.
     * Layout (on 64-bit):
     *   XrStructureType type;           // offset 0, 4 bytes
     *   [4 bytes padding]
     *   const void* next;               // offset 8, 8 bytes
     *   LUID adapterLuid;               // offset 16, 8 bytes (4 low + 4 high)
     *   D3D_FEATURE_LEVEL minFeatureLevel; // offset 24, 4 bytes
     *   [4 bytes padding to align]
     * Total: 32 bytes (aligned)
     */
    public static ByteBuffer createGraphicsRequirementsBuffer() {
        ByteBuffer req = MemoryUtil.memCalloc(32);
        req.putInt(0, XR_TYPE_GRAPHICS_REQUIREMENTS_D3D11_KHR);
        return req;
    }

    /**
     * Reads the adapter LUID from an XrGraphicsRequirementsD3D11KHR buffer.
     * @return [luidLow, luidHigh]
     */
    public static int[] readLuidFromRequirements(ByteBuffer requirements) {
        int luidLow = requirements.getInt(16);
        int luidHigh = requirements.getInt(20);
        return new int[]{luidLow, luidHigh};
    }

    /**
     * Reads the minimum feature level from requirements.
     */
    public static int readMinFeatureLevel(ByteBuffer requirements) {
        return requirements.getInt(24);
    }

    /**
     * Calls IUnknown::Release() on a COM object.
     */
    private static void comRelease(long comObject) {
        if (comObject == 0) return;
        long vtable = MemoryUtil.memGetAddress(comObject);
        long releasePtr = MemoryUtil.memGetAddress(vtable + IUNKNOWN_RELEASE_VTABLE_INDEX * (long) Long.BYTES);
        // ULONG Release(IUnknown* this)
        JNI.callPI(comObject, releasePtr);
    }

    /**
     * Cleans up all D3D11 and interop resources.
     */
    public void destroy() {
        if (this.dxInteropHandle != 0) {
            WGLNVDXInterop.wglDXCloseDeviceNV(this.dxInteropHandle);
            this.dxInteropHandle = 0;
        }
        if (this.d3d11Context != 0) {
            comRelease(this.d3d11Context);
            this.d3d11Context = 0;
        }
        if (this.dxgiAdapter != 0) {
            comRelease(this.dxgiAdapter);
            this.dxgiAdapter = 0;
        }
        if (this.d3d11Device != 0) {
            comRelease(this.d3d11Device);
            this.d3d11Device = 0;
        }
        // Note: we don't FreeLibrary d3d11.dll/dxgi.dll since they may still be in use
    }
}
