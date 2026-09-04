package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.platform.Window;
import com.sun.jna.Platform;
import net.minecraft.client.Minecraft;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GLX13;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.linux.X11;
import org.lwjgl.system.windows.User32;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.VulkanHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;

public interface DeviceCompat {

    default void initOpenXRLoader(MemoryStack stack) {
        VRSettings.LOGGER.info("Vivecraft: OpenXR running on Platform: {}, version: {}",
            System.getProperty("os.name"), System.getProperty("os.version"));
    }

    /**
     * @return the extension name of the used graphics extension used for this device
     */
    String getGraphicsExtension();

    /**
     * checks if the platform requirements are met.
     *
     * @param stack    MemoryStack to use for allocation
     * @param instance XR instance object
     * @param systemID XR system id
     * @throws RenderConfigException thrown when requirements are not met
     */
    void checkRequirements(MemoryStack stack, XrInstance instance, long systemID) throws RenderConfigException;

    /**
     * creates the swapchain image buffer for OpenGL images
     *
     * @param stack      MemoryStack to use for allocation
     * @param imageCount required capacity in the buffer
     * @return the created swapchain image buffer
     */
    XrSwapchainImageOpenGLKHR.Buffer createOpenglImageBuffers(MemoryStack stack, int imageCount);

    /**
     * creates the swapchain image buffer for Vulkan images
     *
     * @param stack      MemoryStack to use for allocation
     * @param imageCount required capacity in the buffer
     * @return the created swapchain image buffer
     */
    XrSwapchainImageVulkanKHR.Buffer createVulkanImageBuffers(MemoryStack stack, int imageCount);

    /**
     * creates any applicable struct that should be added to the {@code next} pointer in the XrSessionCreateInfo
     *
     * @param stack    MemoryStack to use for allocation
     * @param instance XR instance object
     * @param systemID XR system id
     * @return NULL or address of any created next struct
     */
    long getSessionCreateAddition(MemoryStack stack, XrInstance instance, long systemID);

    /**
     * creates any applicable struct that should be added to the {@code next} pointer in the XrInstanceCreateInfo
     *
     * @param stack MemoryStack to use for allocation
     * @return NULL or address of any created next struct
     */
    long getInstanceCreateAddition(MemoryStack stack);

    static DeviceCompat detectDevice() {
        return System.getProperty("os.version").contains("Android") ? new Mobile() : new Desktop();
    }

    class Desktop implements DeviceCompat {

        @Override
        public String getGraphicsExtension() {
            return switch (GraphicsHelper.INSTANCE.getType()) {
                case OPENGL -> KHROpenGLEnable.XR_KHR_OPENGL_ENABLE_EXTENSION_NAME;
                case VULKAN -> // TODO add support for KHRVulkanEnable2
                    KHRVulkanEnable.XR_KHR_VULKAN_ENABLE_EXTENSION_NAME;
            };
        }

        @Override
        public void checkRequirements(
            MemoryStack stack, XrInstance instance, long systemID) throws RenderConfigException
        {
            if (GraphicsHelper.INSTANCE instanceof VulkanHelper vulkanHelper) {
                // check if the required extensions are enabled
                XrGraphicsRequirementsVulkanKHR apiRequirements = XrGraphicsRequirementsVulkanKHR.calloc(stack)
                    .type$Default();
                int error = KHRVulkanEnable.xrGetVulkanGraphicsRequirementsKHR(instance, systemID, apiRequirements);
                MCOpenXR.logError(error, "xrGetVulkanGraphicsRequirementsKHR", "");

                Vector2ic currentVersion = vulkanHelper.getCurrentVulkanVersion();
                Vector2ic minVersion = new Vector2i(XR10.XR_VERSION_MAJOR(apiRequirements.minApiVersionSupported()),
                    XR10.XR_VERSION_MINOR(apiRequirements.minApiVersionSupported()));
                Vector2ic maxVersion = new Vector2i(XR10.XR_VERSION_MAJOR(apiRequirements.maxApiVersionSupported()),
                    XR10.XR_VERSION_MINOR(apiRequirements.maxApiVersionSupported()));

                // warn if the runtime is not tested on this vulkan version
                if (currentVersion.x() > maxVersion.x() ||
                    (currentVersion.x() == maxVersion.x() && currentVersion.y() > maxVersion.y()))
                {
                    VRSettings.LOGGER.warn(
                        "Vivecraft: Game is running Vulkan {}, but the OpenXR runtime is only tested up to {}. this could maybe cause a crash.",
                        currentVersion, maxVersion);
                }

                // check extensions
                IntBuffer capacity = stack.callocInt(1);
                error = KHRVulkanEnable.xrGetVulkanInstanceExtensionsKHR(instance, systemID, capacity, null);
                MCOpenXR.logError(error, "xrGetVulkanInstanceExtensionsKHR", "get capacity");

                ByteBuffer instanceExtensionsBuffer = stack.calloc(capacity.get(0));
                error = KHRVulkanEnable.xrGetVulkanInstanceExtensionsKHR(instance, systemID, capacity,
                    instanceExtensionsBuffer);
                MCOpenXR.logError(error, "xrGetVulkanInstanceExtensionsKHR", "get extensions");

                error = KHRVulkanEnable.xrGetVulkanDeviceExtensionsKHR(instance, systemID, capacity, null);
                MCOpenXR.logError(error, "xrGetVulkanDeviceExtensionsKHR", "get capacity");

                ByteBuffer deviceExtensionsBuffer = stack.calloc(capacity.get(0));
                error = KHRVulkanEnable.xrGetVulkanDeviceExtensionsKHR(instance, systemID, capacity,
                    deviceExtensionsBuffer);
                MCOpenXR.logError(error, "xrGetVulkanDeviceExtensionsKHR", "get extensions");

                String instanceExtensions = MemoryUtil.memUTF8(MemoryUtil.memAddress(instanceExtensionsBuffer));
                String deviceExtensions = MemoryUtil.memUTF8(MemoryUtil.memAddress(deviceExtensionsBuffer));

                PointerBuffer requiredDevicePointer = stack.callocPointer(1);
                error = KHRVulkanEnable.xrGetVulkanGraphicsDeviceKHR(instance, systemID, vulkanHelper.getInstance(),
                    requiredDevicePointer);
                MCOpenXR.logError(error, "xrGetVulkanGraphicsDeviceKHR", "");

                vulkanHelper.checkCompatibility(
                    Arrays.stream(instanceExtensions.split(" ")).toList(),
                    Arrays.stream(deviceExtensions.split(" ")).toList(),
                    minVersion,
                    requiredDevicePointer.get(0));
            }
        }

        @Override
        public XrSwapchainImageOpenGLKHR.Buffer createOpenglImageBuffers(MemoryStack stack, int imageCount) {
            XrSwapchainImageOpenGLKHR.Buffer swapchainImageBuffer = XrSwapchainImageOpenGLKHR.calloc(imageCount, stack);
            for (XrSwapchainImageOpenGLKHR image : swapchainImageBuffer) {
                image.type$Default();
            }

            return swapchainImageBuffer;
        }

        @Override
        public XrSwapchainImageVulkanKHR.Buffer createVulkanImageBuffers(MemoryStack stack, int imageCount) {
            XrSwapchainImageVulkanKHR.Buffer swapchainImageBuffer = XrSwapchainImageVulkanKHR.calloc(imageCount, stack);
            for (XrSwapchainImageVulkanKHR image : swapchainImageBuffer) {
                image.type$Default();
            }

            return swapchainImageBuffer;
        }

        @Override
        public long getInstanceCreateAddition(MemoryStack stack) {
            return MemoryUtil.NULL;
        }

        @Override
        public long getSessionCreateAddition(MemoryStack stack, XrInstance instance, long systemID) {
            return switch (GraphicsHelper.INSTANCE.getType()) {
                case OPENGL -> {
                    XrGraphicsRequirementsOpenGLKHR graphicsRequirements = XrGraphicsRequirementsOpenGLKHR.calloc(stack)
                        .type$Default();
                    KHROpenGLEnable.xrGetOpenGLGraphicsRequirementsKHR(instance, systemID, graphicsRequirements);
                    // Bind the OpenGL context to the OpenXR instance and create the session
                    Window window = Minecraft.getInstance().getWindow();
                    long windowHandle = window.handle();
                    if (Platform.getOSType() == Platform.WINDOWS) {
                        XrGraphicsBindingOpenGLWin32KHR binding = XrGraphicsBindingOpenGLWin32KHR.calloc(stack);
                        binding.type$Default();
                        binding.hDC(User32.GetDC(GLFWNativeWin32.glfwGetWin32Window(windowHandle)));
                        binding.hGLRC(GLFWNativeWGL.glfwGetWGLContext(windowHandle));
                        yield binding.address();
                    } else if (Platform.getOSType() == Platform.LINUX) {
                        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11) {
                            // X11
                            long xDisplay = GLFWNativeX11.glfwGetX11Display();
                            long glXWindowHandle = GLFWNativeGLX.glfwGetGLXWindow(windowHandle);

                            int fbXID = GLX13.glXQueryDrawable(xDisplay, glXWindowHandle, GLX13.GLX_FBCONFIG_ID);
                            PointerBuffer fbConfigBuf = Objects.requireNonNull(
                                GLX13.glXChooseFBConfig(xDisplay, X11.XDefaultScreen(xDisplay),
                                    stack.ints(GLX13.GLX_FBCONFIG_ID, fbXID, 0)), "No X11 Framebuffer config.");
                            long fbConfig = fbConfigBuf.get();

                            XrGraphicsBindingOpenGLXlibKHR binding = XrGraphicsBindingOpenGLXlibKHR.calloc(stack);
                            binding.type$Default();
                            binding.xDisplay(xDisplay);
                            binding.visualid(
                                (int) Objects.requireNonNull(GLX13.glXGetVisualFromFBConfig(xDisplay, fbConfig),
                                    "No X11 visual identifier.").visualid());
                            binding.glxFBConfig(fbConfig);
                            binding.glxDrawable(glXWindowHandle);
                            binding.glxContext(GLFWNativeGLX.glfwGetGLXContext(windowHandle));
                            yield binding.address();
                        } else {
                            throw new RuntimeException("OpenGL OpenXR on Wayland is not supported.");
                        }
                    } else {
                        throw new IllegalStateException("Macos not supported");
                    }
                }
                case VULKAN -> {
                    VulkanHelper vulkanHelper = (VulkanHelper) GraphicsHelper.INSTANCE;
                    XrGraphicsBindingVulkanKHR binding = XrGraphicsBindingVulkanKHR.calloc(stack);
                    binding.type$Default();
                    binding.instance(vulkanHelper.getInstance());
                    binding.physicalDevice(vulkanHelper.getPhysicalDevice());
                    binding.device(vulkanHelper.getDevice());
                    binding.queueFamilyIndex(vulkanHelper.getQueueFamilyIndex());
                    binding.queueIndex(vulkanHelper.getQueueIndex());
                    yield binding.address();
                }
            };
        }
    }

    class Mobile implements DeviceCompat {

        @Override
        public void initOpenXRLoader(MemoryStack stack) {
            DeviceCompat.super.initOpenXRLoader(stack);
//            VLoader.setupAndroid();
//            XrLoaderInitInfoAndroidKHR initInfo = XrLoaderInitInfoAndroidKHR.calloc(stack).set(
//                KHRLoaderInitAndroid.XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR,
//                NULL,
//                VLoader.getDalvikVM(),
//                VLoader.getDalvikActivity()
//            );
//
//            KHRLoaderInit.xrInitializeLoaderKHR(XrLoaderInitInfoBaseHeaderKHR.create(initInfo.address()));
        }

        @Override
        public String getGraphicsExtension() {
            //return KHROpenGLESEnable.XR_KHR_OPENGL_ES_ENABLE_EXTENSION_NAME;
            return null;
        }

        @Override
        public void checkRequirements(
            MemoryStack stack, XrInstance instance, long systemID) throws RenderConfigException
        {}

        @Override
        public XrSwapchainImageOpenGLKHR.Buffer createOpenglImageBuffers(MemoryStack stack, int imageCount) {
//            XrSwapchainImageOpenGLKHR.Buffer swapchainImageBuffer = XrSwapchainImageOpenGLKHR.calloc(imageCount, stack);
//            for (XrSwapchainImageOpenGLKHR image : swapchainImageBuffer) {
//                image.type$Default();
//            }
//
//            return swapchainImageBuffer;
            return null;
        }

        @Override
        public XrSwapchainImageVulkanKHR.Buffer createVulkanImageBuffers(MemoryStack stack, int imageCount) {
//            XrSwapchainImageVulkanKHR.Buffer swapchainImageBuffer = XrSwapchainImageVulkanKHR.calloc(imageCount, stack);
//            for (XrSwapchainImageVulkanKHR image : swapchainImageBuffer) {
//                image.type$Default();
//            }
//
//
            return null;
        }

        @Override
        public long getInstanceCreateAddition(MemoryStack stack) {
//            return XrInstanceCreateInfoAndroidKHR.calloc(stack).set(
//                KHRAndroidCreateInstance.XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR,
//                NULL,
//                VLoader.getDalvikVM(),
//                VLoader.getDalvikActivity()
//            ).address();
            return MemoryUtil.NULL;
        }

        @Override
        public long getSessionCreateAddition(MemoryStack stack, XrInstance instance, long systemID) {
//            XrGraphicsRequirementsOpenGLESKHR graphicsRequirements = XrGraphicsRequirementsOpenGLESKHR.calloc(stack)
//                  .type$Default();
//            KHROpenGLESEnable.xrGetOpenGLESGraphicsRequirementsKHR(instance, systemID, graphicsRequirements);
//            XrGraphicsBindingOpenGLESAndroidKHR graphicsBinding = XrGraphicsBindingOpenGLESAndroidKHR.calloc(stack);
//            graphicsBinding.set(
//                KHROpenGLESEnable.XR_TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR,
//                NULL,
//                VLoader.getEGLDisplay(),
//                VLoader.getEGLConfig(),
//                VLoader.getEGLContext()
//            );
//
//            return graphicsBinding;
            return MemoryUtil.NULL;
        }
    }
}
