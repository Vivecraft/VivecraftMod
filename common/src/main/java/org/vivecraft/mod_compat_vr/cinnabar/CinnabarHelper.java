package org.vivecraft.mod_compat_vr.cinnabar;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import org.lwjgl.vulkan.*;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.render.helpers.vulkan.VulkanHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CinnabarHelper {

    private static boolean INITIALIZED;

    // 1.21.6
    private static Field CinnabarCommandEncoder_beginFrameTransferCommandBuffer;
    private static Method CinnabarCommandEncoder_flushCommandBuffers;
    private static Field CinnabarGpuTexture_imageHandle;
    private static Field CinnabarDevice_vkInstance;
    private static Field CinnabarDevice_vkDevice;
    private static Field CinnabarDevice_vkPhysicalDevice;
    private static Field CinnabarDevice_graphicsQueue;
    private static Field CinnabarDevice_graphicsQueueFamily;

    // 1.21.9+
    private static Field Hg3DCommandEncoder_earlyCommandBuffer;
    private static Method Hg3DCommandEncoder_flush;
    private static Field MercuryCommandBuffer_commandBuffer;
    private static Field Hg3DGpuTexture_image;
    private static Field MercuryImage_imageHandle;
    private static Field MercuryDevice_vkInstance;
    private static Field MercuryDevice_vkDevice;
    private static Field MercuryDevice_vkPhysicalDevice;
    private static Field MercuryDevice_graphicsQueue;
    private static Field MercuryQueue_vkQueue;
    private static Field MercuryQueue_queueFamily;


    public static boolean isLoaded() {
        return Xloader.isModLoaded("cinnabar");
    }

    private static void init() {
        if (INITIALIZED) {
            return;
        }
        try {
            Class<?> CinnabarCommandEncoder =
                Class.forName("graphics.cinnabar.core.b3d.command.CinnabarCommandEncoder");
            CinnabarCommandEncoder_beginFrameTransferCommandBuffer = CinnabarCommandEncoder.getDeclaredField(
                "beginFrameTransferCommandBuffer");
            CinnabarCommandEncoder_beginFrameTransferCommandBuffer.setAccessible(true);

            CinnabarCommandEncoder_flushCommandBuffers = CinnabarCommandEncoder.getMethod("flushCommandBuffers");

            CinnabarGpuTexture_imageHandle =
                Class.forName("graphics.cinnabar.core.b3d.texture.CinnabarGpuTexture")
                    .getDeclaredField("imageHandle");
            CinnabarGpuTexture_imageHandle.setAccessible(true);

            Class<?> CinnabarDevice = Class.forName("graphics.cinnabar.core.b3d.CinnabarDevice");
            CinnabarDevice_vkInstance = CinnabarDevice.getDeclaredField("vkInstance");
            CinnabarDevice_vkDevice = CinnabarDevice.getDeclaredField("vkDevice");
            CinnabarDevice_vkPhysicalDevice = CinnabarDevice.getDeclaredField("vkPhysicalDevice");
            CinnabarDevice_graphicsQueue = CinnabarDevice.getDeclaredField("graphicsQueue");
            CinnabarDevice_graphicsQueueFamily = CinnabarDevice.getDeclaredField("graphicsQueueFamily");
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            try {
                // try new classes
                Class<?> Hg3DCommandEncoder = Class.forName("graphics.cinnabar.core.hg3d.Hg3DCommandEncoder");
                Hg3DCommandEncoder_earlyCommandBuffer = Hg3DCommandEncoder.getDeclaredField("earlyCommandBuffer");
                Hg3DCommandEncoder_earlyCommandBuffer.setAccessible(true);
                Hg3DCommandEncoder_flush = Hg3DCommandEncoder.getMethod("flush");
                MercuryCommandBuffer_commandBuffer =
                    Class.forName("graphics.cinnabar.core.mercury.MercuryCommandBuffer")
                        .getDeclaredField("commandBuffer");
                MercuryCommandBuffer_commandBuffer.setAccessible(true);

                Hg3DGpuTexture_image =
                    Class.forName("graphics.cinnabar.core.hg3d.Hg3DGpuTexture")
                        .getDeclaredField("image");
                Hg3DGpuTexture_image.setAccessible(true);
                MercuryImage_imageHandle =
                    Class.forName("graphics.cinnabar.core.mercury.MercuryImage")
                        .getDeclaredField("imageHandle");
                MercuryImage_imageHandle.setAccessible(true);

                Class<?> MercuryDevice = Class.forName("graphics.cinnabar.core.mercury.MercuryDevice");
                MercuryDevice_vkInstance = MercuryDevice.getDeclaredField("vkInstance");
                MercuryDevice_vkDevice = MercuryDevice.getDeclaredField("vkDevice");
                MercuryDevice_vkPhysicalDevice = MercuryDevice.getDeclaredField("vkPhysicalDevice");
                MercuryDevice_graphicsQueue = MercuryDevice.getDeclaredField("graphicsQueue");
                Class<?> MercuryQueue = Class.forName("graphics.cinnabar.core.mercury.MercuryQueue");
                MercuryQueue_vkQueue = MercuryQueue.getDeclaredField("vkQueue");
                MercuryQueue_vkQueue.setAccessible(true);
                MercuryQueue_queueFamily = MercuryQueue.getDeclaredField("queueFamily");
                MercuryQueue_queueFamily.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e2) {
                throw new RuntimeException("Cinnabar is loaded, but has incompatible changes to work in VR.", e2);
            }
        }
        INITIALIZED = true;
    }

    public static VkCommandBuffer getCommandBuffer() {
        init();
        try {
            if (CinnabarCommandEncoder_beginFrameTransferCommandBuffer != null) {
                return (VkCommandBuffer) CinnabarCommandEncoder_beginFrameTransferCommandBuffer.get(
                    RenderSystem.getDevice().createCommandEncoder());
            } else {
                return (VkCommandBuffer) MercuryCommandBuffer_commandBuffer.get(
                    Hg3DCommandEncoder_earlyCommandBuffer.get(RenderSystem.getDevice().createCommandEncoder()));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cinnabar is loaded, but couldn't get VkCommandBuffer.", e);
        }
    }

    public static void flushCommandBuffers() {
        init();
        try {
            if (CinnabarCommandEncoder_flushCommandBuffers != null) {
                CinnabarCommandEncoder_flushCommandBuffers.invoke(RenderSystem.getDevice().createCommandEncoder());
            } else {
                Hg3DCommandEncoder_flush.invoke(RenderSystem.getDevice().createCommandEncoder());
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Cinnabar is loaded, but couldn't get VkCommandBuffer.", e);
        }
    }

    public static long getImageHandle(GpuTexture texture) {
        init();
        try {
            if (CinnabarGpuTexture_imageHandle != null) {
                return (long) CinnabarGpuTexture_imageHandle.get(texture);
            } else {
                return (long) MercuryImage_imageHandle.get(Hg3DGpuTexture_image.get(texture));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cinnabar is loaded, but couldn't get VkCommandBuffer.", e);
        }
    }

    public static VulkanHelper.VulkanDeviceData getDeviceData() {
        init();
        GpuDevice device = RenderSystem.getDevice();
        try {
            if (CinnabarDevice_vkInstance != null) {
                return new VulkanHelper.VulkanDeviceData(
                    ((VkDevice) CinnabarDevice_vkDevice.get(device)).address(),
                    ((VkPhysicalDevice) CinnabarDevice_vkPhysicalDevice.get(device)).address(),
                    ((VkInstance) CinnabarDevice_vkInstance.get(device)).address(),
                    ((VkQueue) CinnabarDevice_graphicsQueue.get(device)).address(),
                    (int) CinnabarDevice_graphicsQueueFamily.get(device));
            } else {
                Object mercuryQueue = MercuryDevice_graphicsQueue.get(device);
                return new VulkanHelper.VulkanDeviceData(
                    ((VkDevice) MercuryDevice_vkDevice.get(device)).address(),
                    ((VkPhysicalDevice) MercuryDevice_vkPhysicalDevice.get(device)).address(),
                    ((VkInstance) MercuryDevice_vkInstance.get(device)).address(),
                    ((VkQueue) MercuryQueue_vkQueue.get(mercuryQueue)).address(),
                    (int) MercuryQueue_queueFamily.get(mercuryQueue));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cinnabar is loaded, but couldn't get VkCommandBuffer.", e);
        }
    }
}
