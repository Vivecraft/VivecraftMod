package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanBackend;

public interface GraphicsHelper {

    enum DeviceType {
        OPENGL,
        VULKAN
    }

    GraphicsHelper INSTANCE = getHelper();

    private static GraphicsHelper getHelper() {
        if (RenderSystem.getDevice().backend instanceof GlDevice) {
            return new OpenGLHelper();
        } else if (RenderSystem.getDevice().backend instanceof VulkanBackend) {
            return new VulkanHelper();
        } else {
            throw new IllegalStateException(
                "Unsupported backend: " + RenderSystem.getDevice().getDeviceInfo().backendName() + "with class: " +
                    RenderSystem.getDevice().backend.getClass().getName());
        }
    }

    DeviceType getDeviceType();

    long getTextureHandle(GpuTexture texture);

    String checkError(String errorSection);

    boolean isStencil();

    void setStencil(boolean state);

    void flush();
}
