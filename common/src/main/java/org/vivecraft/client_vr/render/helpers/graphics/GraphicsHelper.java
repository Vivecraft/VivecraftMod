package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import org.vivecraft.client_vr.render.helpers.graphics.opengl.OpenGLHelper;
import org.vivecraft.client_vr.render.helpers.graphics.vulkan.VulkanHelper;

import javax.annotation.Nullable;
import java.util.Set;

public interface GraphicsHelper {

    GraphicsHelper INSTANCE = getHelper();

    private static GraphicsHelper getHelper() {
        if (RenderSystem.getDevice().backend instanceof GlDevice) {
            return new OpenGLHelper();
        } else if (RenderSystem.getDevice().backend instanceof VulkanDevice) {
            return new VulkanHelper();
        } else {
            throw new IllegalStateException(
                "Vivecraft: Unsupported backend: " + RenderSystem.getDevice().getDeviceInfo().backendName() +
                    " with class: " + RenderSystem.getDevice().backend.getClass().getName());
        }
    }

    /**
     * @return returns the underlying Api type
     */
    Type getType();

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

    /**
     * copies the RenderTarget color buffer to the given RawTexture
     *
     * @param sources RenderTargets to copy from
     * @param targets RawTextures to copy to, needs to be the same count as the size of {@code source}
     */
    void blitTextures(RenderTarget[] sources, RawTexture... targets);

    /**
     * @return Set of ImageFormats that should be fully supported by the current gpu
     */
    Set<ImageFormat> supportedImageFormats();

    /**
     * converts an ImageFormat to the API specific format
     *
     * @param format ImageFormat to convert
     * @return the API specific format, returns {@code -1} if the format is unsupported
     */
    int formatToAPI(ImageFormat format);

    /**
     * converts an API specific format to the corresponding ImageFormat
     *
     * @param format API specific format
     * @return corresponding ImageFormat, or {@code null} if the format is unsupported
     */
    @Nullable
    ImageFormat formatFromAPI(int format);

    enum Type {
        OPENGL,
        VULKAN
    }
}
