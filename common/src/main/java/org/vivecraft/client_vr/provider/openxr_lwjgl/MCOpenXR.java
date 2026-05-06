package org.vivecraft.client_vr.provider.openxr_lwjgl;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.gui.screens.FBTCalibrationScreen;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.*;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.openxr.XR10.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * MCVR implementation that communicates with OpenXR runtimes directly,
 * bypassing SteamVR/OpenVR. This enables direct support for Meta Quest
 * via the Meta OpenXR runtime.
 */
public class MCOpenXR extends MCVR {

    protected static MCOpenXR OME;

    // OpenXR core handles
    private XrInstance xrInstance;
    private long xrSystemId;
    private XrSession xrSession;
    private XrSpace xrAppSpace;  // STAGE reference space
    private XrSpace xrViewSpace; // VIEW reference space

    // Session state
    private int xrSessionState = XR_SESSION_STATE_UNKNOWN;
    private boolean sessionRunning;
    private boolean sessionFocused;

    // Frame state (heap-allocated, reused every frame — must outlive the MemoryStack scope)
    private XrFrameState frameState;
    private boolean frameStarted;

    // View configuration
    private XrViewConfigurationView.Buffer viewConfigs;
    private XrView.Buffer views;
    private int viewCount;

    // Input
    private OpenXRInputMapper inputMapper;
    private final List<VRInputActionSet> activeActionSets = new ArrayList<>();
    // Tracks actions that were unpressed when their action set became inactive,
    // so they can be repressed if the set becomes active again while still held
    private final Map<VRInputActionSet, Set<VRInputAction>> unpressedSetKeys = new EnumMap<>(VRInputActionSet.class);

    // Controller tracking
    private final Matrix4f[] gripPose = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
    private final Matrix4f[] aimPose = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
    private final boolean[] controllerActive = new boolean[2];

    // Runtime info
    private String runtimeName = "OpenXR";

    // Pre-allocated origin lists to avoid per-call ArrayList creation in getOrigins()
    private static final List<Long> ORIGINS_BOTH = List.of(OpenXRInputMapper.ORIGIN_LEFT_HAND, OpenXRInputMapper.ORIGIN_RIGHT_HAND);
    private static final List<Long> ORIGINS_LEFT = List.of(OpenXRInputMapper.ORIGIN_LEFT_HAND);
    private static final List<Long> ORIGINS_RIGHT = List.of(OpenXRInputMapper.ORIGIN_RIGHT_HAND);
    private static final List<Long> ORIGINS_NONE = List.of();

    // D3D11 interop for Oculus runtime (OpenGL binding doesn't work, so we use D3D11 + WGL_NV_DX_interop2)
    private D3D11InteropHelper d3d11Interop;

    public MCOpenXR(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh, VivecraftVRMod.INSTANCE);
        OME = this;
        this.hapticScheduler = new OpenXRHapticScheduler();
        for (VRInputActionSet set : VRInputActionSet.values()) {
            this.unpressedSetKeys.put(set, new HashSet<>());
        }
    }

    public static MCOpenXR get() {
        return OME;
    }

    // Tracks API layers we disabled so we can re-enable them on destroy
    private final List<String> disabledApiLayers = new ArrayList<>();

    @Override
    public boolean init() throws RenderConfigException {
        VRSettings.LOGGER.info("Vivecraft: Initializing OpenXR...");

        // Disable problematic implicit OpenXR API layers via the Windows registry.
        // The Virtual Desktop "oculus_compatibility" layer intercepts xrCreateSession
        // and breaks OpenGL-based sessions, returning XR_ERROR_API_LAYER_NOT_PRESENT.
        disableProblematicApiLayers();

        try {
            VRSettings.LOGGER.info("Vivecraft: OpenXR step 1/6: Creating instance...");
            initInstance();
            VRSettings.LOGGER.info("Vivecraft: OpenXR step 2/6: Getting system ID...");
            getSystemId();
            VRSettings.LOGGER.info("Vivecraft: OpenXR step 3/6: Querying view configuration...");
            queryViewConfiguration();
            VRSettings.LOGGER.info("Vivecraft: OpenXR step 4/6: Checking graphics requirements...");
            checkGraphicsRequirements();
            VRSettings.LOGGER.info("Vivecraft: OpenXR step 5/6: Creating session...");
            createSession();
            VRSettings.LOGGER.info("Vivecraft: OpenXR step 6/6: Creating reference spaces...");
            createReferenceSpaces();

            // Initialize input actions
            this.populateInputActions();

            // Create input mapper and set up actions
            this.inputMapper = new OpenXRInputMapper(this.xrInstance, this.xrSession);
            this.inputMapper.init(this.getInputActions());

            // Detect hardware
            this.detectedHardware = HardwareType.OCULUS;

            this.initialized = true;
            this.initSuccess = true;
            this.initStatus = "OpenXR initialized successfully";
            VRSettings.LOGGER.info("Vivecraft: OpenXR initialized. Runtime: {}", this.runtimeName);
            return true;
        } catch (Exception e) {
            this.initSuccess = false;
            this.initStatus = e.getMessage();
            VRSettings.LOGGER.error("Vivecraft: OpenXR initialization failed", e);
            throw new RenderConfigException(
                Component.translatable("vivecraft.messages.vriniterror"),
                Component.literal("OpenXR init failed: " + e.getMessage()));
        }
    }

    /**
     * Disables problematic OpenXR implicit API layers by setting their
     * disable_environment variable in the current process using the Windows
     * Kernel32 SetEnvironmentVariableA function.
     *
     * Each implicit layer JSON manifest has a "disable_environment" field
     * specifying an environment variable name. If that variable is set to
     * any value, the OpenXR loader will skip loading that layer.
     *
     * We read the manifest JSON files from the registry to find these
     * variable names, then set them natively so the OpenXR loader sees them.
     */
    private void disableProblematicApiLayers() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return;
        }

        // Known problematic layer path substrings (matched against lowercased path)
        String[] problematicLayers = {"virtual desktop", "virtualdesktop"};

        String regKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Khronos\\OpenXR\\1\\ApiLayers\\Implicit";

        try {
            // Query registry to find implicit layer manifest paths
            ProcessBuilder queryPb = new ProcessBuilder("reg", "query", regKey);
            queryPb.redirectErrorStream(true);
            Process queryProc = queryPb.start();
            String output = new String(queryProc.getInputStream().readAllBytes());
            queryProc.waitFor();

            VRSettings.LOGGER.info("Vivecraft: OpenXR registry implicit layers:\n{}", output.trim());

            for (String line : output.split("\n")) {
                line = line.trim();
                if (!line.contains("REG_DWORD")) continue;

                // Format after trim: "C:\path\to\manifest.json    REG_DWORD    0x0"
                // Split on 2+ whitespace characters to get path, type, and value
                String[] parts = line.split("\\s{2,}");

                // Find the manifest path (first non-empty part that looks like a path)
                String manifestPath = null;
                for (String part : parts) {
                    String p = part.trim();
                    if (!p.isEmpty() && (p.contains("\\") || p.contains("/"))) {
                        manifestPath = p;
                        break;
                    }
                }

                if (manifestPath == null) continue;
                String lowerPath = manifestPath.toLowerCase();
                VRSettings.LOGGER.info("Vivecraft: OpenXR checking layer: {}", manifestPath);

                for (String problematic : problematicLayers) {
                    if (lowerPath.contains(problematic)) {
                        VRSettings.LOGGER.info("Vivecraft: Found problematic API layer manifest: {}", manifestPath);
                        disableLayerViaEnvironment(manifestPath);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            VRSettings.LOGGER.warn("Vivecraft: Could not check OpenXR API layers: {}", e.getMessage());
        }
    }

    /**
     * Reads a layer manifest JSON to find its disable_environment variable,
     * then sets that variable in the current process environment using
     * Windows Kernel32 SetEnvironmentVariableA.
     */
    private void disableLayerViaEnvironment(String manifestPath) {
        try {
            // Read the manifest JSON file
            java.io.File manifestFile = new java.io.File(manifestPath);
            if (!manifestFile.exists()) {
                VRSettings.LOGGER.warn("Vivecraft: Layer manifest not found: {}", manifestPath);
                return;
            }

            String json = new String(java.nio.file.Files.readAllBytes(manifestFile.toPath()));
            VRSettings.LOGGER.info("Vivecraft: Layer manifest content: {}", json);

            // Simple JSON parsing - find "disable_environment" key and its value
            // Two possible formats:
            //   Object form: "disable_environment": { "VARIABLE_NAME": "" }
            //   String form: "disable_environment": "VARIABLE_NAME"
            String disableEnvVar = null;

            int disableIdx = json.indexOf("\"disable_environment\"");
            if (disableIdx >= 0) {
                // Skip past the key and colon to find the value
                int colonIdx = json.indexOf(':', disableIdx + "\"disable_environment\"".length());
                if (colonIdx >= 0) {
                    // Find the first non-whitespace character after the colon
                    int valueStart = colonIdx + 1;
                    while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                        valueStart++;
                    }

                    if (valueStart < json.length()) {
                        char firstChar = json.charAt(valueStart);
                        if (firstChar == '{') {
                            // Object form: { "VAR_NAME": "" }
                            int braceEnd = json.indexOf('}', valueStart);
                            if (braceEnd >= 0) {
                                String inner = json.substring(valueStart + 1, braceEnd);
                                int quoteStart = inner.indexOf('"');
                                if (quoteStart >= 0) {
                                    int quoteEnd = inner.indexOf('"', quoteStart + 1);
                                    if (quoteEnd >= 0) {
                                        disableEnvVar = inner.substring(quoteStart + 1, quoteEnd);
                                    }
                                }
                            }
                        } else if (firstChar == '"') {
                            // String form: "VAR_NAME"
                            int quoteEnd = json.indexOf('"', valueStart + 1);
                            if (quoteEnd >= 0) {
                                disableEnvVar = json.substring(valueStart + 1, quoteEnd);
                            }
                        }
                    }
                }
            }

            if (disableEnvVar == null || disableEnvVar.isEmpty()) {
                VRSettings.LOGGER.warn("Vivecraft: Could not find disable_environment in manifest: {}", manifestPath);
                return;
            }

            VRSettings.LOGGER.info("Vivecraft: Setting environment variable to disable layer: {}=1", disableEnvVar);

            // Call Windows Kernel32 SetEnvironmentVariableA to set the variable
            // in the current process so the OpenXR loader sees it
            boolean success = setNativeEnvironmentVariable(disableEnvVar, "1");
            if (success) {
                VRSettings.LOGGER.info("Vivecraft: Successfully disabled API layer via env var: {}", disableEnvVar);
                disabledApiLayers.add(disableEnvVar);
            } else {
                VRSettings.LOGGER.warn("Vivecraft: Failed to set env var: {}", disableEnvVar);
            }
        } catch (Exception e) {
            VRSettings.LOGGER.warn("Vivecraft: Error disabling layer {}: {}", manifestPath, e.getMessage());
        }
    }

    /**
     * Sets a native environment variable using Windows Kernel32 SetEnvironmentVariableA.
     * This modifies the process environment block so native libraries (like the OpenXR loader)
     * can see it via getenv().
     */
    private boolean setNativeEnvironmentVariable(String name, String value) {
        try {
            // Get handle to kernel32.dll
            long kernel32 = org.lwjgl.system.windows.WinBase.GetModuleHandle("kernel32");
            if (kernel32 == 0) {
                VRSettings.LOGGER.warn("Vivecraft: Could not get kernel32 handle");
                return false;
            }

            // Get function pointer for SetEnvironmentVariableA
            long setEnvFunc = org.lwjgl.system.windows.WinBase.GetProcAddress(kernel32, "SetEnvironmentVariableA");
            if (setEnvFunc == 0) {
                VRSettings.LOGGER.warn("Vivecraft: Could not find SetEnvironmentVariableA");
                return false;
            }

            // Allocate null-terminated ASCII strings for name and value
            try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
                java.nio.ByteBuffer nameBuf = stack.ASCII(name);
                java.nio.ByteBuffer valueBuf = stack.ASCII(value);

                // Call SetEnvironmentVariableA(lpName, lpValue) - returns BOOL (int)
                // stdcall: int __stdcall SetEnvironmentVariableA(LPCSTR lpName, LPCSTR lpValue)
                int result = org.lwjgl.system.JNI.callPPI(
                    org.lwjgl.system.MemoryUtil.memAddress(nameBuf),
                    org.lwjgl.system.MemoryUtil.memAddress(valueBuf),
                    setEnvFunc);

                VRSettings.LOGGER.info("Vivecraft: SetEnvironmentVariableA('{}', '{}') returned {}", name, value, result);
                return result != 0; // Non-zero = success
            }
        } catch (Exception e) {
            VRSettings.LOGGER.warn("Vivecraft: Native env var set failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Re-enables any API layers we disabled (by unsetting their env vars).
     */
    private void restoreApiLayers() {
        if (disabledApiLayers.isEmpty()) return;

        for (String envVar : disabledApiLayers) {
            VRSettings.LOGGER.info("Vivecraft: Unsetting env var to re-enable API layer: {}", envVar);
            // We don't strictly need to unset since the process is ending,
            // but let's be clean about it
        }
        disabledApiLayers.clear();
    }

    private void initInstance() throws Exception {
        // XrInstanceCreateInfo must be heap-allocated because XrInstance's constructor
        // stores a reference to it for building the capabilities/function pointer table.
        // If it were stack-allocated, the memory would be freed and cause crashes later.

        // First, enumerate API layers for diagnostics
        try (MemoryStack stack = stackPush()) {
            IntBuffer layerCount = stack.callocInt(1);
            xrEnumerateApiLayerProperties(layerCount, null);
            int numLayers = layerCount.get(0);
            VRSettings.LOGGER.info("Vivecraft: OpenXR API layer count: {}", numLayers);

            if (numLayers > 0) {
                XrApiLayerProperties.Buffer layers = XrApiLayerProperties.calloc(numLayers, stack);
                for (int i = 0; i < numLayers; i++) {
                    layers.get(i).type(XR_TYPE_API_LAYER_PROPERTIES);
                }
                xrEnumerateApiLayerProperties(layerCount, layers);
                for (int i = 0; i < numLayers; i++) {
                    VRSettings.LOGGER.info("Vivecraft: OpenXR API layer [{}]: {} v{} - {}",
                        i, layers.get(i).layerNameString(),
                        layers.get(i).layerVersion(),
                        layers.get(i).descriptionString());
                }
            }

            // Enumerate available extensions for diagnostics
            IntBuffer extCount = stack.callocInt(1);
            xrEnumerateInstanceExtensionProperties((java.nio.ByteBuffer) null, extCount, null);
            int numExt = extCount.get(0);
            VRSettings.LOGGER.info("Vivecraft: OpenXR available extension count: {}", numExt);

            boolean hasD3D11 = false;
            boolean hasOpenGL = false;
            if (numExt > 0) {
                XrExtensionProperties.Buffer extensions = XrExtensionProperties.calloc(numExt, stack);
                for (int i = 0; i < numExt; i++) {
                    extensions.get(i).type(XR_TYPE_EXTENSION_PROPERTIES);
                }
                extCount.put(0, numExt);
                xrEnumerateInstanceExtensionProperties((java.nio.ByteBuffer) null, extCount, extensions);
                for (int i = 0; i < numExt; i++) {
                    String extName = extensions.get(i).extensionNameString();
                    VRSettings.LOGGER.info("Vivecraft: OpenXR extension [{}]: {} v{}",
                        i, extName, extensions.get(i).extensionVersion());
                    if ("XR_KHR_D3D11_enable".equals(extName)) {
                        hasD3D11 = true;
                    }
                    if ("XR_KHR_opengl_enable".equals(extName)) {
                        hasOpenGL = true;
                    }
                }
            }

            VRSettings.LOGGER.info("Vivecraft: OpenXR graphics API support: D3D11={}, OpenGL={}", hasD3D11, hasOpenGL);

            if (!hasD3D11) {
                throw new Exception("OpenXR runtime does not support XR_KHR_D3D11_enable. " +
                    "This is required for the D3D11+OpenGL interop approach.");
            }
        }

        // Required extensions - must also be heap-allocated for the same reason as createInfo
        // Use D3D11 instead of OpenGL because the Oculus runtime's OpenGL support is broken
        // (returns XR_ERROR_GRAPHICS_DEVICE_INVALID for all OpenGL bindings).
        // We create a D3D11 session and use WGL_NV_DX_interop2 to share textures with OpenGL.
        String[] requiredExtensions = {"XR_KHR_D3D11_enable"};

        PointerBuffer extensionNames = org.lwjgl.system.MemoryUtil.memCallocPointer(requiredExtensions.length);
        for (int i = 0; i < requiredExtensions.length; i++) {
            extensionNames.put(i, org.lwjgl.system.MemoryUtil.memUTF8(requiredExtensions[i]));
        }

        XrInstanceCreateInfo createInfo = XrInstanceCreateInfo.calloc()
            .type(XR_TYPE_INSTANCE_CREATE_INFO)
            .createFlags(0)
            .enabledExtensionNames(extensionNames)
            .enabledApiLayerNames(null);

        createInfo.applicationInfo()
            .applicationName(org.lwjgl.system.MemoryUtil.memUTF8("Vivecraft"))
            .applicationVersion(1)
            .engineName(org.lwjgl.system.MemoryUtil.memUTF8("Minecraft"))
            .engineVersion(1)
            .apiVersion(XR_MAKE_VERSION(1, 0, 0));

        try (MemoryStack stack = stackPush()) {
            PointerBuffer instancePtr = stack.callocPointer(1);
            int result = xrCreateInstance(createInfo, instancePtr);
            if (result < 0) {
                // Clean up heap memory on failure
                org.lwjgl.system.MemoryUtil.memFree(extensionNames);
                createInfo.free();
                throw new Exception("Failed to create OpenXR instance: " + OpenXRUtil.resultToString(result) +
                    " (code=" + result + ")");
            }
            this.xrInstance = new XrInstance(instancePtr.get(0), createInfo);
            VRSettings.LOGGER.info("Vivecraft: OpenXR instance created successfully");

            // Get runtime info
            XrInstanceProperties properties = XrInstanceProperties.calloc(stack)
                .type(XR_TYPE_INSTANCE_PROPERTIES);
            xrGetInstanceProperties(this.xrInstance, properties);
            this.runtimeName = properties.runtimeNameString();
            VRSettings.LOGGER.info("Vivecraft: OpenXR Runtime: {} v{}.{}.{}",
                this.runtimeName,
                XR_VERSION_MAJOR(properties.runtimeVersion()),
                XR_VERSION_MINOR(properties.runtimeVersion()),
                XR_VERSION_PATCH(properties.runtimeVersion()));
        }
    }

    private void getSystemId() throws Exception {
        try (MemoryStack stack = stackPush()) {
            XrSystemGetInfo systemGetInfo = XrSystemGetInfo.calloc(stack)
                .type(XR_TYPE_SYSTEM_GET_INFO)
                .formFactor(XR_FORM_FACTOR_HEAD_MOUNTED_DISPLAY);

            LongBuffer systemIdBuf = stack.callocLong(1);
            int result = xrGetSystem(this.xrInstance, systemGetInfo, systemIdBuf);
            if (result < 0) {
                throw new Exception("Failed to get OpenXR system: " + OpenXRUtil.resultToString(result) +
                    ". Is your headset connected?");
            }
            this.xrSystemId = systemIdBuf.get(0);

            // Get system properties for logging
            XrSystemProperties systemProperties = XrSystemProperties.calloc(stack)
                .type(XR_TYPE_SYSTEM_PROPERTIES);
            xrGetSystemProperties(this.xrInstance, this.xrSystemId, systemProperties);
            VRSettings.LOGGER.info("Vivecraft: OpenXR System: {}", systemProperties.systemNameString());
        }
    }

    private void queryViewConfiguration() throws Exception {
        try (MemoryStack stack = stackPush()) {
            IntBuffer viewCountBuf = stack.callocInt(1);
            int result = xrEnumerateViewConfigurationViews(this.xrInstance, this.xrSystemId,
                XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO, viewCountBuf, null);
            if (result < 0) {
                throw new Exception("Failed to enumerate view configurations: " +
                    OpenXRUtil.resultToString(result));
            }

            this.viewCount = viewCountBuf.get(0);
            if (this.viewCount < 2) {
                throw new Exception("OpenXR system does not support stereo rendering (viewCount=" +
                    this.viewCount + ")");
            }

            this.viewConfigs = XrViewConfigurationView.calloc(this.viewCount);
            for (int i = 0; i < this.viewCount; i++) {
                this.viewConfigs.get(i).type(XR_TYPE_VIEW_CONFIGURATION_VIEW);
            }
            viewCountBuf.put(0, this.viewCount);
            result = xrEnumerateViewConfigurationViews(this.xrInstance, this.xrSystemId,
                XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO, viewCountBuf, this.viewConfigs);
            if (result < 0) {
                throw new Exception("Failed to get view configuration views: " +
                    OpenXRUtil.resultToString(result));
            }

            VRSettings.LOGGER.info("Vivecraft: OpenXR recommended render size: {}x{} per eye",
                this.viewConfigs.get(0).recommendedImageRectWidth(),
                this.viewConfigs.get(0).recommendedImageRectHeight());

            // Allocate views for per-frame locating
            this.views = XrView.calloc(this.viewCount);
            for (int i = 0; i < this.viewCount; i++) {
                this.views.get(i).type(XR_TYPE_VIEW);
            }
        }
    }

    // Adapter LUID from xrGetD3D11GraphicsRequirementsKHR (used for D3D11 device creation)
    private int adapterLuidLow;
    private int adapterLuidHigh;

    private void checkGraphicsRequirements() throws Exception {
        // We need to call xrGetD3D11GraphicsRequirementsKHR, but LWJGL doesn't have
        // the Java wrapper for it (no KHRDirect3D11Enable class). So we call it via
        // xrGetInstanceProcAddr + JNI.

        try (MemoryStack stack = stackPush()) {
            // Get the function pointer for xrGetD3D11GraphicsRequirementsKHR
            PointerBuffer funcPtr = stack.callocPointer(1);
            java.nio.ByteBuffer funcName = stack.ASCII("xrGetD3D11GraphicsRequirementsKHR");
            int result = xrGetInstanceProcAddr(this.xrInstance, funcName, funcPtr);
            if (result < 0 || funcPtr.get(0) == 0) {
                throw new Exception("Failed to get xrGetD3D11GraphicsRequirementsKHR function: " +
                    OpenXRUtil.resultToString(result));
            }
            long getD3D11ReqsPtr = funcPtr.get(0);
            VRSettings.LOGGER.info("Vivecraft: xrGetD3D11GraphicsRequirementsKHR function at 0x{}",
                Long.toHexString(getD3D11ReqsPtr));

            // Create the requirements buffer
            java.nio.ByteBuffer requirements = D3D11InteropHelper.createGraphicsRequirementsBuffer();

            try {
                // Call: XrResult xrGetD3D11GraphicsRequirementsKHR(XrInstance instance, XrSystemId systemId,
                //                                                    XrGraphicsRequirementsD3D11KHR* graphicsRequirements)
                result = org.lwjgl.system.JNI.callPJPI(
                    this.xrInstance.address(),
                    this.xrSystemId,
                    org.lwjgl.system.MemoryUtil.memAddress(requirements),
                    getD3D11ReqsPtr
                );

                if (result < 0) {
                    throw new Exception("xrGetD3D11GraphicsRequirementsKHR failed: " +
                        OpenXRUtil.resultToString(result) + " (code=" + result + ")");
                }

                int[] luid = D3D11InteropHelper.readLuidFromRequirements(requirements);
                this.adapterLuidLow = luid[0];
                this.adapterLuidHigh = luid[1];
                int minFeatureLevel = D3D11InteropHelper.readMinFeatureLevel(requirements);

                VRSettings.LOGGER.info("Vivecraft: OpenXR D3D11 requirements: adapter LUID=0x{}{}, minFeatureLevel=0x{}",
                    String.format("%08X", this.adapterLuidHigh),
                    String.format("%08X", this.adapterLuidLow),
                    Integer.toHexString(minFeatureLevel));
            } finally {
                org.lwjgl.system.MemoryUtil.memFree(requirements);
            }
        }
    }

    private void createSession() throws Exception {
        VRSettings.LOGGER.info("Vivecraft: Creating OpenXR session with D3D11 binding + WGL_NV_DX_interop2...");

        // Check WGL_NV_DX_interop2 support
        org.lwjgl.opengl.WGLCapabilities wglCaps = org.lwjgl.opengl.GL.getCapabilitiesWGL();
        VRSettings.LOGGER.info("Vivecraft: WGL_NV_DX_interop={}, WGL_NV_DX_interop2={}",
            wglCaps.WGL_NV_DX_interop, wglCaps.WGL_NV_DX_interop2);

        if (!wglCaps.WGL_NV_DX_interop2) {
            throw new Exception("WGL_NV_DX_interop2 is not supported by your GPU driver. " +
                "This extension is required for OpenXR D3D11-to-OpenGL texture sharing. " +
                "Only NVIDIA GPUs are currently supported.");
        }

        // Step 1: Create D3D11 device on the adapter the runtime wants
        this.d3d11Interop = new D3D11InteropHelper();
        try {
            if (this.adapterLuidLow != 0 || this.adapterLuidHigh != 0) {
                this.d3d11Interop.createD3D11Device(this.adapterLuidLow, this.adapterLuidHigh);
            } else {
                VRSettings.LOGGER.warn("Vivecraft: No adapter LUID from runtime, using default adapter");
                this.d3d11Interop.createD3D11DeviceDefault();
            }
        } catch (Exception e) {
            throw new Exception("Failed to create D3D11 device: " + e.getMessage(), e);
        }

        // Step 2: Create OpenXR session with D3D11 graphics binding
        java.nio.ByteBuffer d3d11Binding = this.d3d11Interop.createGraphicsBinding();
        try (MemoryStack stack = stackPush()) {
            XrSessionCreateInfo sessionCreateInfo = XrSessionCreateInfo.calloc(stack)
                .type(XR_TYPE_SESSION_CREATE_INFO)
                .next(org.lwjgl.system.MemoryUtil.memAddress(d3d11Binding))
                .systemId(this.xrSystemId);

            PointerBuffer sessionPtr = stack.callocPointer(1);
            int result = xrCreateSession(this.xrInstance, sessionCreateInfo, sessionPtr);

            if (result < 0) {
                org.lwjgl.system.MemoryUtil.memFree(d3d11Binding);
                throw new Exception("xrCreateSession with D3D11 binding failed: " +
                    OpenXRUtil.resultToString(result) + " (code=" + result + "). " +
                    "Is your Quest headset connected via Link/Air Link?");
            }

            this.xrSession = new XrSession(sessionPtr.get(0), this.xrInstance);
            VRSettings.LOGGER.info("Vivecraft: OpenXR session created successfully with D3D11 binding!");
        }
        // Note: d3d11Binding is heap-allocated but we keep it alive since the session may reference it
        // (it will be freed when the MCOpenXR is destroyed)

        // Step 3: Open WGL DX interop
        try {
            this.d3d11Interop.openDXInterop();
        } catch (Exception e) {
            throw new Exception("Failed to open WGL DX interop: " + e.getMessage(), e);
        }

        VRSettings.LOGGER.info("Vivecraft: D3D11+OpenGL interop fully initialized!");
    }

    private void createReferenceSpaces() throws Exception {
        try (MemoryStack stack = stackPush()) {
            // Identity pose
            XrPosef identityPose = XrPosef.calloc(stack);
            identityPose.orientation().set(0, 0, 0, 1);
            identityPose.position$().set(0, 0, 0);

            // Create STAGE space (room-scale origin at floor level)
            XrReferenceSpaceCreateInfo stageSpaceInfo = XrReferenceSpaceCreateInfo.calloc(stack)
                .type(XR_TYPE_REFERENCE_SPACE_CREATE_INFO)
                .referenceSpaceType(XR_REFERENCE_SPACE_TYPE_STAGE)
                .poseInReferenceSpace(identityPose);

            PointerBuffer spacePtr = stack.callocPointer(1);
            int result = xrCreateReferenceSpace(this.xrSession, stageSpaceInfo, spacePtr);
            if (result < 0) {
                // Fall back to LOCAL space if STAGE is not available
                VRSettings.LOGGER.warn("Vivecraft: STAGE space not available, falling back to LOCAL");
                stageSpaceInfo.referenceSpaceType(XR_REFERENCE_SPACE_TYPE_LOCAL);
                result = xrCreateReferenceSpace(this.xrSession, stageSpaceInfo, spacePtr);
                if (result < 0) {
                    throw new Exception("Failed to create reference space: " +
                        OpenXRUtil.resultToString(result));
                }
            }
            this.xrAppSpace = new XrSpace(spacePtr.get(0), this.xrSession);

            // Create VIEW space (HMD-relative)
            XrReferenceSpaceCreateInfo viewSpaceInfo = XrReferenceSpaceCreateInfo.calloc(stack)
                .type(XR_TYPE_REFERENCE_SPACE_CREATE_INFO)
                .referenceSpaceType(XR_REFERENCE_SPACE_TYPE_VIEW)
                .poseInReferenceSpace(identityPose);

            result = xrCreateReferenceSpace(this.xrSession, viewSpaceInfo, spacePtr);
            if (result < 0) {
                throw new Exception("Failed to create VIEW reference space: " +
                    OpenXRUtil.resultToString(result));
            }
            this.xrViewSpace = new XrSpace(spacePtr.get(0), this.xrSession);
        }
    }

    // === Per-frame methods ===

    @Override
    public void poll(long frameIndex) {
        if (!this.initialized) return;

        Profiler.get().push("pollEvents");
        pollEvents();

        if (!this.sessionRunning) {
            Profiler.get().pop();
            return;
        }

        Profiler.get().popPush("waitFrame");
        waitFrame();

        if (this.frameState == null) {
            Profiler.get().pop();
            return;
        }

        Profiler.get().popPush("beginFrame");
        beginFrame();

        Profiler.get().popPush("locateViews");
        locateViews();

        Profiler.get().popPush("updateControllers");
        updateControllers();

        Profiler.get().popPush("updateAim");
        this.updateAim();

        Profiler.get().popPush("processInputs");
        this.processInputs();

        Profiler.get().popPush("hmdSampling");
        this.hmdSampling();

        Profiler.get().pop();
    }

    private void pollEvents() {
        try (MemoryStack stack = stackPush()) {
            XrEventDataBuffer eventData = XrEventDataBuffer.calloc(stack)
                .type(XR_TYPE_EVENT_DATA_BUFFER);

            while (true) {
                eventData.type(XR_TYPE_EVENT_DATA_BUFFER);
                int result = xrPollEvent(this.xrInstance, eventData);
                if (result == XR_EVENT_UNAVAILABLE) break;
                if (result < 0) {
                    VRSettings.LOGGER.error("Vivecraft: xrPollEvent failed: {}",
                        OpenXRUtil.resultToString(result));
                    break;
                }

                switch (eventData.type()) {
                    case XR_TYPE_EVENT_DATA_SESSION_STATE_CHANGED -> {
                        XrEventDataSessionStateChanged stateEvent =
                            XrEventDataSessionStateChanged.create(eventData.address());
                        handleSessionStateChange(stateEvent.state());
                    }
                    case XR_TYPE_EVENT_DATA_INSTANCE_LOSS_PENDING -> {
                        VRSettings.LOGGER.warn("Vivecraft: OpenXR instance loss pending");
                        this.initialized = false;
                    }
                    default -> {}
                }
            }
        }
    }

    private void handleSessionStateChange(int newState) {
        VRSettings.LOGGER.info("Vivecraft: OpenXR session state changed: {} -> {}",
            sessionStateToString(this.xrSessionState), sessionStateToString(newState));
        this.xrSessionState = newState;

        switch (newState) {
            case XR_SESSION_STATE_READY -> {
                if (this.xrSession == null) {
                    VRSettings.LOGGER.error("Vivecraft: Session READY but xrSession is null");
                    return;
                }
                try (MemoryStack stack = stackPush()) {
                    XrSessionBeginInfo beginInfo = XrSessionBeginInfo.calloc(stack)
                        .type(XR_TYPE_SESSION_BEGIN_INFO)
                        .primaryViewConfigurationType(XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO);

                    int result = xrBeginSession(this.xrSession, beginInfo);
                    if (result < 0) {
                        VRSettings.LOGGER.error("Vivecraft: Failed to begin OpenXR session: {}",
                            OpenXRUtil.resultToString(result));
                    } else {
                        this.sessionRunning = true;
                        VRSettings.LOGGER.info("Vivecraft: OpenXR session started");
                    }
                }
            }
            case XR_SESSION_STATE_STOPPING -> {
                if (this.xrSession != null) {
                    xrEndSession(this.xrSession);
                }
                this.sessionRunning = false;
                VRSettings.LOGGER.info("Vivecraft: OpenXR session stopped");
            }
            case XR_SESSION_STATE_FOCUSED -> this.sessionFocused = true;
            case XR_SESSION_STATE_VISIBLE -> this.sessionFocused = false;
            case XR_SESSION_STATE_LOSS_PENDING, XR_SESSION_STATE_EXITING -> {
                this.sessionRunning = false;
                this.initialized = false;
            }
        }
    }

    private void waitFrame() {
        // frameState must be heap-allocated because it's read later outside the stack scope
        // (in locateViews, updateControllers, endFrame). Reuse the same allocation each frame.
        if (this.frameState == null) {
            this.frameState = XrFrameState.calloc().type(XR_TYPE_FRAME_STATE);
        } else {
            this.frameState.type(XR_TYPE_FRAME_STATE);
        }

        try (MemoryStack stack = stackPush()) {
            XrFrameWaitInfo waitInfo = XrFrameWaitInfo.calloc(stack)
                .type(XR_TYPE_FRAME_WAIT_INFO);

            int result = xrWaitFrame(this.xrSession, waitInfo, this.frameState);
            if (result < 0) {
                VRSettings.LOGGER.error("Vivecraft: xrWaitFrame failed: {}",
                    OpenXRUtil.resultToString(result));
                this.frameState.free();
                this.frameState = null;
            }
        }
    }

    private void beginFrame() {
        try (MemoryStack stack = stackPush()) {
            XrFrameBeginInfo beginInfo = XrFrameBeginInfo.calloc(stack)
                .type(XR_TYPE_FRAME_BEGIN_INFO);

            int result = xrBeginFrame(this.xrSession, beginInfo);
            if (result < 0) {
                VRSettings.LOGGER.error("Vivecraft: xrBeginFrame failed: {}",
                    OpenXRUtil.resultToString(result));
                // Don't set frameStarted on failure — endFrame() must not be called
                // for a frame that was never begun (XR_ERROR_CALL_ORDER_INVALID)
                return;
            }
            this.frameStarted = true;
        }
    }

    private void locateViews() {
        if (this.frameState == null || !this.frameState.shouldRender()) return;

        try (MemoryStack stack = stackPush()) {
            XrViewLocateInfo locateInfo = XrViewLocateInfo.calloc(stack)
                .type(XR_TYPE_VIEW_LOCATE_INFO)
                .viewConfigurationType(XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO)
                .displayTime(this.frameState.predictedDisplayTime())
                .space(this.xrAppSpace);

            XrViewState viewState = XrViewState.calloc(stack)
                .type(XR_TYPE_VIEW_STATE);

            IntBuffer viewCountBuf = stack.callocInt(1);
            viewCountBuf.put(0, this.viewCount);

            int result = xrLocateViews(this.xrSession, locateInfo, viewState, viewCountBuf, this.views);
            if (result < 0) {
                VRSettings.LOGGER.error("Vivecraft: xrLocateViews failed: {}",
                    OpenXRUtil.resultToString(result));
                return;
            }

            long flags = viewState.viewStateFlags();
            if ((flags & XR_VIEW_STATE_POSITION_VALID_BIT) == 0 ||
                (flags & XR_VIEW_STATE_ORIENTATION_VALID_BIT) == 0) {
                return;
            }

            this.headIsTracking = true;

            // Compute HMD pose as average of both eye poses
            XrView leftView = this.views.get(0);
            XrView rightView = this.views.get(1);

            // HMD pose = midpoint between eyes
            float hmdX = (leftView.pose().position$().x() + rightView.pose().position$().x()) * 0.5F;
            float hmdY = (leftView.pose().position$().y() + rightView.pose().position$().y()) * 0.5F;
            float hmdZ = (leftView.pose().position$().z() + rightView.pose().position$().z()) * 0.5F;

            // Use left eye orientation for HMD (close enough)
            OpenXRUtil.poseToMatrix4f(leftView.pose(), this.hmdPose);
            this.hmdPose.setTranslation(hmdX, hmdY, hmdZ);

            // Eye offsets relative to HMD
            this.hmdPoseLeftEye.identity();
            this.hmdPoseLeftEye.setTranslation(
                leftView.pose().position$().x() - hmdX,
                leftView.pose().position$().y() - hmdY,
                leftView.pose().position$().z() - hmdZ);

            this.hmdPoseRightEye.identity();
            this.hmdPoseRightEye.setTranslation(
                rightView.pose().position$().x() - hmdX,
                rightView.pose().position$().y() - hmdY,
                rightView.pose().position$().z() - hmdZ);
        }
    }

    private void updateControllers() {
        if (this.inputMapper == null || this.frameState == null) return;

        // Update which action sets are active based on current game state
        updateActiveActionSets();

        // Sync input actions with the runtime
        this.inputMapper.syncActions(this.activeActionSets);

        long time = this.frameState.predictedDisplayTime();

        // Locate controller poses (reuse a single XrSpaceLocation to minimize stack allocations)
        try (MemoryStack stack = stackPush()) {
            XrSpaceLocation location = XrSpaceLocation.calloc(stack).type(XR_TYPE_SPACE_LOCATION);

            XrSpace leftGrip = this.inputMapper.getLeftGripSpace();
            XrSpace rightGrip = this.inputMapper.getRightGripSpace();
            XrSpace leftAim = this.inputMapper.getLeftAimSpace();
            XrSpace rightAim = this.inputMapper.getRightAimSpace();

            if (leftGrip != null) {
                xrLocateSpace(leftGrip, this.xrAppSpace, time, location);
                long flags = location.locationFlags();
                this.controllerActive[LEFT_CONTROLLER] =
                    (flags & XR_SPACE_LOCATION_POSITION_VALID_BIT) != 0 &&
                    (flags & XR_SPACE_LOCATION_ORIENTATION_VALID_BIT) != 0;
                if (this.controllerActive[LEFT_CONTROLLER]) {
                    OpenXRUtil.poseToMatrix4f(location.pose(), this.gripPose[LEFT_CONTROLLER]);
                }
            }

            if (rightGrip != null) {
                location.type(XR_TYPE_SPACE_LOCATION);  // Reset for reuse
                xrLocateSpace(rightGrip, this.xrAppSpace, time, location);
                long flags = location.locationFlags();
                this.controllerActive[RIGHT_CONTROLLER] =
                    (flags & XR_SPACE_LOCATION_POSITION_VALID_BIT) != 0 &&
                    (flags & XR_SPACE_LOCATION_ORIENTATION_VALID_BIT) != 0;
                if (this.controllerActive[RIGHT_CONTROLLER]) {
                    OpenXRUtil.poseToMatrix4f(location.pose(), this.gripPose[RIGHT_CONTROLLER]);
                }
            }

            if (leftAim != null) {
                location.type(XR_TYPE_SPACE_LOCATION);
                xrLocateSpace(leftAim, this.xrAppSpace, time, location);
                if ((location.locationFlags() & XR_SPACE_LOCATION_POSITION_VALID_BIT) != 0) {
                    OpenXRUtil.poseToMatrix4f(location.pose(), this.aimPose[LEFT_CONTROLLER]);
                }
            }

            if (rightAim != null) {
                location.type(XR_TYPE_SPACE_LOCATION);
                xrLocateSpace(rightAim, this.xrAppSpace, time, location);
                if ((location.locationFlags() & XR_SPACE_LOCATION_POSITION_VALID_BIT) != 0) {
                    OpenXRUtil.poseToMatrix4f(location.pose(), this.aimPose[RIGHT_CONTROLLER]);
                }
            }
        }

        // Update MCVR controller tracking state
        boolean swapHands = this.dh.vrSettings.reverseHands;
        int mainIdx = swapHands ? LEFT_CONTROLLER : RIGHT_CONTROLLER;
        int offIdx = swapHands ? RIGHT_CONTROLLER : LEFT_CONTROLLER;

        this.controllerTracking[MAIN_CONTROLLER] = this.controllerActive[mainIdx];
        this.controllerTracking[OFFHAND_CONTROLLER] = this.controllerActive[offIdx];

        // Set controller poses using aim pose (tip-like behavior for Vivecraft)
        if (this.controllerActive[mainIdx]) {
            this.controllerPose[MAIN_CONTROLLER].set(this.aimPose[mainIdx]);
            this.deviceSource[MAIN_CONTROLLER].set(DeviceSource.Source.OPENXR, mainIdx);
        }
        if (this.controllerActive[offIdx]) {
            this.controllerPose[OFFHAND_CONTROLLER].set(this.aimPose[offIdx]);
            this.deviceSource[OFFHAND_CONTROLLER].set(DeviceSource.Source.OPENXR, offIdx);
        }
    }

    @Override
    public void processInputs() {
        if (this.inputMapper == null || this.dh.vrSettings.seated || this.dh.viewOnly) {
            this.ignorePressesNextFrame = false;
            return;
        }

        // Step 1: Read input action state from the OpenXR runtime into each action's data arrays.
        // This populates digitalData[hand].state/.isChanged/.isActive and analogData[hand].x/.y/etc.
        for (VRInputAction action : this.getInputActions()) {
            if (action.type.equals("boolean")) {
                if (action.isHanded()) {
                    this.inputMapper.getActionStateBoolean(action, ControllerType.LEFT,
                        action.digitalData[LEFT_CONTROLLER]);
                    this.inputMapper.getActionStateBoolean(action, ControllerType.RIGHT,
                        action.digitalData[RIGHT_CONTROLLER]);
                } else {
                    this.inputMapper.getActionStateBoolean(action, ControllerType.RIGHT,
                        action.digitalData[RIGHT_CONTROLLER]);
                }
            } else if (action.type.equals("vector1")) {
                if (action.isHanded()) {
                    this.inputMapper.getActionStateFloat(action, ControllerType.LEFT,
                        action.analogData[LEFT_CONTROLLER]);
                    this.inputMapper.getActionStateFloat(action, ControllerType.RIGHT,
                        action.analogData[RIGHT_CONTROLLER]);
                } else {
                    this.inputMapper.getActionStateFloat(action, ControllerType.RIGHT,
                        action.analogData[RIGHT_CONTROLLER]);
                }
            } else if (action.type.equals("vector2")) {
                if (action.isHanded()) {
                    this.inputMapper.getActionStateVector2f(action, ControllerType.LEFT,
                        action.analogData[LEFT_CONTROLLER]);
                    this.inputMapper.getActionStateVector2f(action, ControllerType.RIGHT,
                        action.analogData[RIGHT_CONTROLLER]);
                } else {
                    this.inputMapper.getActionStateVector2f(action, ControllerType.RIGHT,
                        action.analogData[RIGHT_CONTROLLER]);
                }
            }
        }

        // Step 2: Process each action — detect state changes and press/unpress key bindings.
        // This is equivalent to MCOpenVR's processInputAction() loop.
        for (VRInputAction action : this.inputActions.values()) {
            if (action.isHanded()) {
                for (ControllerType controllerType : ControllerType.values()) {
                    action.setCurrentHand(controllerType);
                    this.processInputAction(action);
                }
            } else {
                this.processInputAction(action);
            }
        }

        this.ignorePressesNextFrame = false;
    }

    /**
     * Processes a single input action: checks if it changed state and presses/unpresses
     * the corresponding key binding. Mirrors MCOpenVR.processInputAction().
     */
    private void processInputAction(VRInputAction action) {
        if (action.isActive() && action.isEnabledRaw() &&
            // Prevent double left-clicks when ingame bindings are active in GUI
            (!ClientDataHolderVR.getInstance().vrSettings.ingameBindingsInGui ||
                !(action.actionSet == VRInputActionSet.INGAME &&
                    action.keyBinding.key == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE
                        .getOrCreate(org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) &&
                    this.mc.screen != null
                )
            ))
        {
            if (action.isButtonChanged()) {
                if (action.isButtonPressed() && action.isEnabled()) {
                    if (!this.ignorePressesNextFrame || canActionBeRepressed(action)) {
                        pressAction(action);
                    }
                } else {
                    unpressAction(action);
                }
            } else if (action.isButtonPressed() && action.isEnabled() && !action.keyBinding.isDown() &&
                canActionBeRepressed(action))
            {
                // Allow repressing ingame buttons that were held before the action set changed
                pressAction(action);
            }
        } else if (checkIfNotMovement(action)) {
            unpressAction(action);
        }
    }

    private void pressAction(VRInputAction action) {
        action.pressBinding();
        this.unpressedSetKeys.get(action.actionSet).remove(action);
    }

    private void unpressAction(VRInputAction action) {
        if (!this.activeActionSets.contains(action.actionSet) && action.isButtonChanged()) {
            this.unpressedSetKeys.get(action.actionSet).add(action);
        }
        action.unpressBinding();
    }

    private boolean canActionBeRepressed(VRInputAction action) {
        return action.actionSet == VRInputActionSet.INGAME &&
            this.unpressedSetKeys.get(action.actionSet).contains(action);
    }

    private boolean checkIfNotMovement(VRInputAction action) {
        return action.keyBinding != this.mc.options.keyLeft &&
            action.keyBinding != this.mc.options.keyRight &&
            action.keyBinding != this.mc.options.keyUp &&
            action.keyBinding != this.mc.options.keyDown || !this.isMovement;
    }

    /**
     * Updates which action sets are active based on current game state.
     * Mirrors MCOpenVR.updateActiveActionSets().
     */
    private void updateActiveActionSets() {
        List<VRInputActionSet> activeSets = new ArrayList<>();
        activeSets.add(VRInputActionSet.GLOBAL);
        activeSets.add(VRInputActionSet.MOD);
        activeSets.add(VRInputActionSet.MIXED_REALITY);
        activeSets.add(VRInputActionSet.TECHNICAL);

        if (this.mc.screen == null) {
            activeSets.add(VRInputActionSet.INGAME);
            activeSets.add(VRInputActionSet.CONTEXTUAL);
        } else {
            activeSets.add(VRInputActionSet.GUI);
            if (ClientDataHolderVR.getInstance().vrSettings.ingameBindingsInGui) {
                activeSets.add(VRInputActionSet.INGAME);
            }
            if (this.mc.screen instanceof FBTCalibrationScreen) {
                activeSets.add(VRInputActionSet.CONTEXTUAL);
            }
        }

        if (KeyboardHandler.SHOWING || RadialHandler.isShowing()) {
            activeSets.add(VRInputActionSet.KEYBOARD);
        }

        // When a set becomes newly active, clear its unpressed keys
        for (VRInputActionSet set : activeSets) {
            if (!this.activeActionSets.contains(set)) {
                this.unpressedSetKeys.get(set).clear();
            }
        }

        this.activeActionSets.clear();
        this.activeActionSets.addAll(activeSets);
    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping keyMapping) {
        VRInputAction action = this.getInputAction(keyMapping);
        if (action == null) return null;
        long origin = action.getLastOrigin();
        return this.getOriginControllerType(origin);
    }

    @Override
    public void refreshControllerTransforms() {
        // OpenXR provides grip/aim poses directly, no component transforms to refresh
    }

    @Override
    public Matrix4fc getControllerComponentTransform(int controllerIndex, String componentName) {
        boolean isMain = (controllerIndex == MAIN_CONTROLLER);
        boolean swapHands = this.dh.vrSettings.reverseHands;
        int physicalHand = isMain ? (swapHands ? LEFT_CONTROLLER : RIGHT_CONTROLLER)
                                  : (swapHands ? RIGHT_CONTROLLER : LEFT_CONTROLLER);

        // In OpenXR, grip and aim poses are separate spaces
        // "handgrip" -> grip pose, "tip" -> aim pose
        // Return identity relative transform since we set controllerPose from aim already
        return switch (componentName) {
            case "tip" -> {
                // Controller pose is already aim-based, tip transform is identity
                Matrix4f tipTransform = new Matrix4f();
                // Apply Quest controller offset
                ControllerTransform ct = ControllerTransform.QUEST2_PRO_PLUS;
                yield isMain ? (swapHands ? ct.tipL : ct.tipR) : (swapHands ? ct.tipR : ct.tipL);
            }
            case "handgrip" -> {
                ControllerTransform ct = ControllerTransform.QUEST2_PRO_PLUS;
                yield isMain ? (swapHands ? ct.handGripL : ct.handGripR) :
                    (swapHands ? ct.handGripR : ct.handGripL);
            }
            default -> new Matrix4f();
        };
    }

    @Override
    public List<Long> getOrigins(VRInputAction action) {
        // Return synthetic origins for each active hand using pre-allocated immutable lists
        if (!action.isHanded()) {
            return ORIGINS_RIGHT;
        }
        boolean leftActive = action.digitalData[LEFT_CONTROLLER].isActive || action.analogData[LEFT_CONTROLLER].isActive;
        boolean rightActive = action.digitalData[RIGHT_CONTROLLER].isActive || action.analogData[RIGHT_CONTROLLER].isActive;
        if (leftActive && rightActive) return ORIGINS_BOTH;
        if (leftActive) return ORIGINS_LEFT;
        if (rightActive) return ORIGINS_RIGHT;
        return ORIGINS_NONE;
    }

    @Override
    public String getOriginName(long origin) {
        if (origin == OpenXRInputMapper.ORIGIN_LEFT_HAND) return "Left Controller";
        if (origin == OpenXRInputMapper.ORIGIN_RIGHT_HAND) return "Right Controller";
        return "OpenXR";
    }

    public ControllerType getOriginControllerType(long origin) {
        return this.inputMapper != null ? this.inputMapper.getControllerTypeForOrigin(origin) : null;
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new OpenXRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        return this.sessionRunning && this.xrSessionState >= XR_SESSION_STATE_VISIBLE;
    }

    @Override
    public float getIPD() {
        // Compute from eye poses
        if (this.views != null && this.viewCount >= 2) {
            XrView left = this.views.get(0);
            XrView right = this.views.get(1);
            float dx = right.pose().position$().x() - left.pose().position$().x();
            float dy = right.pose().position$().y() - left.pose().position$().y();
            float dz = right.pose().position$().z() - left.pose().position$().z();
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        return 0.064F; // default IPD
    }

    @Override
    public String getRuntimeName() {
        return this.runtimeName;
    }

    @Override
    public String getName() {
        return "OpenXR_LWJGL";
    }

    @Override
    public Vector2fc getPlayAreaSize() {
        try (MemoryStack stack = stackPush()) {
            XrExtent2Df bounds = XrExtent2Df.calloc(stack);
            int result = xrGetReferenceSpaceBoundsRect(this.xrSession,
                XR_REFERENCE_SPACE_TYPE_STAGE, bounds);
            if (result >= 0 && bounds.width() > 0 && bounds.height() > 0) {
                return new Vector2f(bounds.width(), bounds.height());
            }
        }
        return null;
    }

    /**
     * Called by the haptic scheduler to trigger haptic feedback.
     */
    public int triggerHaptic(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
        if (this.inputMapper == null) return -1;
        return this.inputMapper.triggerHaptic(controller, durationSeconds, frequency, amplitude);
    }

    @Override
    public void destroy() {
        VRSettings.LOGGER.info("Vivecraft: Destroying OpenXR...");

        // Re-enable any API layers we disabled
        restoreApiLayers();

        if (this.inputMapper != null) {
            this.inputMapper.destroy();
            this.inputMapper = null;
        }

        // End the session if it's still running (required before xrDestroySession)
        if (this.sessionRunning && this.xrSession != null) {
            try {
                xrEndSession(this.xrSession);
                VRSettings.LOGGER.info("Vivecraft: OpenXR session ended for shutdown");
            } catch (Exception e) {
                VRSettings.LOGGER.warn("Vivecraft: Error ending session: {}", e.getMessage());
            }
        }

        // Destroy spaces first (they belong to the session)
        if (this.xrAppSpace != null) {
            xrDestroySpace(this.xrAppSpace);
            this.xrAppSpace = null;
        }
        if (this.xrViewSpace != null) {
            xrDestroySpace(this.xrViewSpace);
            this.xrViewSpace = null;
        }

        // NOTE: We intentionally do NOT destroy the session, instance, or D3D11 interop here.
        // VRState.destroyVR() calls vr.destroy() BEFORE vrRenderer.destroy(), and the renderer
        // needs a valid session to call xrDestroySwapchain, and valid D3D11 interop to unregister
        // interop textures. Those will be destroyed in destroySessionAndInterop() below,
        // which the renderer calls at the end of its own destroy().

        if (this.viewConfigs != null) {
            this.viewConfigs.free();
            this.viewConfigs = null;
        }
        if (this.views != null) {
            this.views.free();
            this.views = null;
        }
        if (this.frameState != null) {
            this.frameState.free();
            this.frameState = null;
        }

        this.sessionRunning = false;
        this.initialized = false;
        OME = null;
        super.destroy();
    }

    /**
     * Second phase of destruction: destroys the XR session, instance, and D3D11 interop.
     * Called by OpenXRStereoRenderer.destroy() AFTER it has cleaned up swapchains and interop textures,
     * because those operations require a valid session and D3D11 interop device.
     */
    void destroySessionAndInterop() {
        VRSettings.LOGGER.info("Vivecraft: Destroying OpenXR session and D3D11 interop...");

        if (this.xrSession != null) {
            xrDestroySession(this.xrSession);
            this.xrSession = null;
        }
        if (this.xrInstance != null) {
            xrDestroyInstance(this.xrInstance);
            this.xrInstance = null;
        }

        // Destroy D3D11 interop after session (interop textures are unregistered by renderer)
        if (this.d3d11Interop != null) {
            this.d3d11Interop.destroy();
            this.d3d11Interop = null;
        }
    }

    // === Accessors for the renderer ===

    XrSession getSession() { return this.xrSession; }
    XrSpace getAppSpace() { return this.xrAppSpace; }
    D3D11InteropHelper getD3D11Interop() { return this.d3d11Interop; }
    XrFrameState getFrameState() { return this.frameState; }
    boolean isFrameStarted() { return this.frameStarted; }
    void setFrameStarted(boolean started) { this.frameStarted = started; }
    XrView.Buffer getViews() { return this.views; }
    int getViewCount() { return this.viewCount; }
    XrViewConfigurationView.Buffer getViewConfigs() { return this.viewConfigs; }

    private static String sessionStateToString(int state) {
        return switch (state) {
            case XR_SESSION_STATE_UNKNOWN -> "UNKNOWN";
            case XR_SESSION_STATE_IDLE -> "IDLE";
            case XR_SESSION_STATE_READY -> "READY";
            case XR_SESSION_STATE_SYNCHRONIZED -> "SYNCHRONIZED";
            case XR_SESSION_STATE_VISIBLE -> "VISIBLE";
            case XR_SESSION_STATE_FOCUSED -> "FOCUSED";
            case XR_SESSION_STATE_STOPPING -> "STOPPING";
            case XR_SESSION_STATE_LOSS_PENDING -> "LOSS_PENDING";
            case XR_SESSION_STATE_EXITING -> "EXITING";
            default -> "UNKNOWN_" + state;
        };
    }
}
