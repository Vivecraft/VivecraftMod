package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;

public interface GraphicsHelper {

    GraphicsHelper INSTANCE = getHelper();

    private static GraphicsHelper getHelper() {
        if (RenderSystem.getDevice() instanceof FrontendGpuDevice gpuDevice) {
            if (gpuDevice.backend instanceof GlDevice) {
                return new OpenGLHelper();
            } else if (gpuDevice.backend instanceof VulkanDevice) {
                return new VulkanHelper();
            }
        }
        throw new IllegalStateException(
            "Vivecraft: Unsupported backend: " + RenderSystem.getDevice().getDeviceInfo().backendName() +
                " with class: " + RenderSystem.getDevice().getClass().getName());
    }

    /**
     * Generates api texture handle for the given GpuTexture
     *
     * @param texture GpuTexture to get the texture handle for
     */
    long getTextureHandle(GpuTexture texture);

    /**
     * Generates mipmaps for the given GpuTexture
     *
     * @param texture GpuTexture to generate mipmaps for
     */
    void genMipmaps(GpuTexture texture);

    String checkError(String errorSection);

    boolean isStencil();

    void setStencil(boolean state);

    void flush();

    /**
     * @return if the eye buffer needs to be flipped vertically
     */
    default boolean flipEyeVertically() {
        return false;
    }
}
