package org.vivecraft.mixin.client_vr.blaze3d.vulkan;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vulkan.VulkanInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.vulkan.VulkanInstanceExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Mixin(VulkanInstance.class)
public class VulkanInstanceVRMixin implements VulkanInstanceExtension {
    @Shadow
    @Final
    private Set<String> enabledExtensions;

    @Unique
    private Set<String> vivecraft$availableExtensions;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vulkan/VulkanDebug;create(IZLjava/util/Set;Ljava/util/Set;)Lcom/mojang/blaze3d/vulkan/VulkanDebug;"))
    private void vivecraft$vrInstanceExtensions(CallbackInfo ci, @Local Set<String> availableExtensions) {
        if (!ClientDataHolderVR.getInstance().vrSettings.requiredVulkanInstanceExtensions.isEmpty()) {
            // check that all extensions are supported before enabling anything
            String[] neededExtensions = ClientDataHolderVR.getInstance().vrSettings.requiredVulkanInstanceExtensions.split(
                " ");
            List<String> missingExtensions = new ArrayList<>();
            for (String extension : neededExtensions) {
                if (!availableExtensions.contains(extension)) {
                    missingExtensions.add(extension);
                }
            }
            if (missingExtensions.isEmpty()) {
                // all available, enable them
                this.enabledExtensions.addAll(Arrays.asList(neededExtensions));
            }
        }
        this.vivecraft$availableExtensions = ImmutableSet.copyOf(availableExtensions);
    }

    @Override
    public Set<String> vivecraft$getAvailableExtensions() {
        return this.vivecraft$availableExtensions;
    }
}
