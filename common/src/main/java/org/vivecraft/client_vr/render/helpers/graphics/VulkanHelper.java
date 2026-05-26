package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;

public class VulkanHelper implements GraphicsHelper {
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.VULKAN;
    }

    @Override
    public long getTextureHandle(GpuTexture texture) {
        if (texture instanceof VulkanGpuTexture vulkanTexture) {
            return vulkanTexture.vkImage();
        }
        throw new IllegalArgumentException("Vivecraft: not a vulkan texture in vulkan context");
    }

    @Override
    public String checkError(String errorSection) {
        // can't check errors like that on vulkan
        return "";
    }

    @Override
    public boolean isStencil() {
        return false;
    }

    @Override
    public void setStencil(boolean state) {

    }

    @Override
    public void flush() {

    }
}
