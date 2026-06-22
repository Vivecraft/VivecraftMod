package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface GraphicsHelper {

    GraphicsHelper INSTANCE = getHelper();

    private static GraphicsHelper getHelper() {
        return new OpenGLHelper();
        //} else if (RenderSystem.getDevice().backend instanceof VulkanDevice) {
        //    return new VulkanHelper();
    }

    /**
     * Generates api texture handle for the given RenderTarget
     *
     * @param texture RenderTarget to get the texture handle for
     */
    long getTextureHandle(RenderTarget texture);

    /**
     * Generates mipmaps for the given RenderTarget
     *
     * @param texture RenderTarget to generate mipmaps for
     */
    void genMipmaps(RenderTarget texture);

    /**
     * enabled anisotropic filtering for the given RenderTarget
     *
     * @param texture RenderTarget to enabled anisotropic filtering for
     */
    void enableAnisotropicFiltering(RenderTarget texture);

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
