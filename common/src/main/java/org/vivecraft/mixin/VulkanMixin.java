package org.vivecraft.mixin;

import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.lwjgl.vulkan.VkInstance;

@Mixin(Vulkan.class)
public interface VulkanMixin {
    @Accessor
    static VkInstance getInstance() {
        return null;
    }

    @Accessor
    static VkPhysicalDevice getPhysicalDevice() {
        return null;
    }

    @Accessor
    static VkDevice getDevice() {
        return null;
    }
}