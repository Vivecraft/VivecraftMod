package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.extensions.vulkan.VulkanDeviceExtension;
import org.vivecraft.client_vr.extensions.vulkan.VulkanPhysicalDeviceExtension;

import java.util.Set;

@Mixin(VulkanDevice.class)
public class VulkanDeviceVRMixin implements VulkanDeviceExtension {

    @Unique
    private Set<String> vivecraft$availableDeviceExtensions;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;close()V"))
    private void vivecraft$storeAvailableExtensions(
        CallbackInfo ci, @Local(argsOnly = true) VulkanPhysicalDevice physicalDevice)
    {
        this.vivecraft$availableDeviceExtensions = ((VulkanPhysicalDeviceExtension) physicalDevice).vivecraft$getAvailableExtensions();
    }

    @Override
    public Set<String> vivecraft$getAvailableDeviceExtensions() {
        return this.vivecraft$availableDeviceExtensions;
    }
}
