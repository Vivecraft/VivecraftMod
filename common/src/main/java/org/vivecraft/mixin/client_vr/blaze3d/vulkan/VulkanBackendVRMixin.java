package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.vulkan.VulkanBackend;
import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.VulkanStaticHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Mixin(VulkanBackend.class)
public class VulkanBackendVRMixin {
    @Inject(method = "createDevice(JLcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/shaders/GpuDebugOptions;Ljava/lang/Runnable;)Lcom/mojang/blaze3d/systems/GpuDevice;", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanBackend;createDevice(Ljava/util/Collection;Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;Ljava/util/Set;)Lorg/lwjgl/vulkan/VkDevice;"))
    private void vivecraft$vrDeviceExtensions(
        CallbackInfoReturnable<GpuDevice> cir, @Local VulkanPhysicalDevice physicalDevice,
        @Local(ordinal = 0) Set<String> deviceExtensions)
    {
        if (!ClientDataHolderVR.getInstance().vrSettings.requiredVulkanDeviceExtensions.isEmpty()) {
            // check that all extensions are supported before enabling anything
            String[] neededExtensions = ClientDataHolderVR.getInstance().vrSettings.requiredVulkanDeviceExtensions.split(
                " ");
            List<String> missingExtensions = new ArrayList<>();
            for (String extension : neededExtensions) {
                if (!physicalDevice.hasDeviceExtension(extension)) {
                    missingExtensions.add(extension);
                }
            }
            if (missingExtensions.isEmpty()) {
                // all available, enable them
                deviceExtensions.addAll(Arrays.asList(neededExtensions));
            }
        }
    }

    @WrapOperation(method = "findPhysicalDevice", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanBackend;isDeviceSuitable(Lorg/lwjgl/vulkan/VkPhysicalDevice;)Z"))
    private static boolean vivecraft$preferVRGpuCapture(
        VkPhysicalDevice vkPhysicalDevice, Operation<Boolean> original,
        @Share("vrVkDevice") LocalRef<VkPhysicalDevice> vrVkDevice)
    {
        boolean suitable = original.call(vkPhysicalDevice);

        String requiredDevice = ClientDataHolderVR.getInstance().vrSettings.requiredVulkanDeviceUUID;
        if (suitable && !requiredDevice.isEmpty()) {
            if (VulkanStaticHelper.getDeviceUUID(vkPhysicalDevice).equals(requiredDevice)) {
                vrVkDevice.set(vkPhysicalDevice);
            }
        }
        return suitable;
    }

    @ModifyArg(method = "findPhysicalDevice", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanPhysicalDevice;<init>(Lorg/lwjgl/vulkan/VkPhysicalDevice;)V"))
    private static VkPhysicalDevice vivecraft$preferVRGpuSet(
        VkPhysicalDevice vkPhysicalDevice, @Share("vrVkDevice") LocalRef<VkPhysicalDevice> vrVkDevice)
    {
        return vrVkDevice.get() != null ? vrVkDevice.get() : vkPhysicalDevice;
    }
}
