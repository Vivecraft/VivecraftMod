package org.vivecraft.client_vr.render.helpers.graphics.vulkan;


import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.UUID;

/**
 * ths class contains static helper methods from {@link VulkanHelper}, since those cannot be called externally before the backend is initialized
 */
public class VulkanStaticHelper {


    public static Vector2ic parseVulkanVersion(int packedVersion) {
        return new Vector2i(VK10.VK_API_VERSION_MAJOR(packedVersion), VK10.VK_API_VERSION_MINOR(packedVersion));
    }

    public static int packVulkanVersion(int major, int minor) {
        return VK10.VK_MAKE_VERSION(major, minor, 0);
    }

    public static String getDeviceUUID(VkPhysicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties2 deviceProperties2 = VkPhysicalDeviceProperties2.calloc(stack)
                .sType$Default();
            VkPhysicalDeviceIDProperties deviceIDProperties = VkPhysicalDeviceIDProperties.calloc(stack)
                .sType$Default();
            deviceProperties2.pNext(deviceIDProperties);
            VK11.vkGetPhysicalDeviceProperties2(device, deviceProperties2);

            return new UUID(deviceIDProperties.deviceUUID().getLong(0),
                deviceIDProperties.deviceUUID().getLong(1)).toString();
        }
    }

    public static String getDeviceName(VkPhysicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.calloc(stack);
            VK10.vkGetPhysicalDeviceProperties(device, deviceProperties);

            return deviceProperties.deviceNameString();
        }
    }
}
