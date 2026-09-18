package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.*;

@Mixin(VulkanBackend.class)
public class VulkanBackendVRMixin {
    @ModifyArg(method = "createDevice(Lcom/mojang/renderpearl/api/device/GpuDebugOptions;)Lcom/mojang/renderpearl/api/device/GpuDevice;", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/init/FeatureSet;<init>(Ljava/lang/String;Ljava/util/Collection;)V"), index = 1)
    private Collection<FeatureSet> vivecraft$vrDeviceExtensions(
        Collection<FeatureSet> enabledFeatureSets, @Local VulkanPhysicalDevice physicalDevice,
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
                enabledFeatureSets.add(
                    new FeatureSet("VR Extensions", new HashSet<>(Arrays.asList(neededExtensions)), Set.of()));
            }
        }
        return enabledFeatureSets;
    }
}
