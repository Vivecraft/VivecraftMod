package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;

public interface GraphicsHelper {

    GraphicsHelper INSTANCE = getHelper();

    private static GraphicsHelper getHelper() {
        if (RenderSystem.getDevice().backend instanceof GlDevice) {
            return new OpenGLHelper();
            //} else if (RenderSystem.getDevice().backend instanceof VulkanDevice) {
            //    return new VulkanHelper();
        } else {
            throw new IllegalStateException(
                "Vivecraft: Unsupported backend: " + RenderSystem.getDevice().backend.getBackendName() +
                    " with class: " + RenderSystem.getDevice().backend.getClass().getName());
        }
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
