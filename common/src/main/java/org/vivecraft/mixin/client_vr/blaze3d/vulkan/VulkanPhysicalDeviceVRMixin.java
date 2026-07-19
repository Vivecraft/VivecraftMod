package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client_vr.extensions.vulkan.VulkanPhysicalDeviceExtension;

import java.util.Set;

@Mixin(VulkanPhysicalDevice.class)
public class VulkanPhysicalDeviceVRMixin implements VulkanPhysicalDeviceExtension {
    @Shadow
    @Final
    private VkExtensionProperties.Buffer vkDeviceExtensions;

    @Override
    @Unique
    public Set<String> vivecraft$getAvailableExtensions() {
        return ImmutableSet.copyOf(
            this.vkDeviceExtensions.stream().map(VkExtensionProperties::extensionNameString).toList());
    }
}
