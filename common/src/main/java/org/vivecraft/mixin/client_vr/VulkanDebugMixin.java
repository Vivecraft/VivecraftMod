package org.vivecraft.mixin.client_vr;

import com.mojang.blaze3d.vulkan.VulkanDebug;
import org.lwjgl.vulkan.VkDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.function.Supplier;

@Mixin(value = {VulkanDebug.Enabled.class, VulkanDebug.Disabled.class})
public class VulkanDebugMixin {
    @Inject(method = "setObjectName(Lorg/lwjgl/vulkan/VkDevice;IJLjava/lang/String;)V", at = @At("HEAD"))
    private void vivecraft$logName1(VkDevice device, int objectType, long objectHandle, String label, CallbackInfo ci) {
        VRSettings.LOGGER.error("Buffer: {} is named : {}", Long.toHexString(objectHandle), label);
    }

    @Inject(method = "setObjectName(Lorg/lwjgl/vulkan/VkDevice;IJLjava/util/function/Supplier;)V", at = @At("HEAD"))
    private void vivecraft$logName2(
        VkDevice device, int objectType, long objectHandle, Supplier<String> label, CallbackInfo ci)
    {
        VRSettings.LOGGER.error("Buffer: {} is named : {}", Long.toHexString(objectHandle), label.get());
    }
}
