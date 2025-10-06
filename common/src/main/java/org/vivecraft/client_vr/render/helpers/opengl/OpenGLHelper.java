package org.vivecraft.client_vr.render.helpers.opengl;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL46C;

public class OpenGLHelper {

    private static boolean CHECKED_ANISOTROPY = false;
    private static boolean ANISOTROPY_SUPPORTED = false;
    private static int ANISOTROPY_LEVEL = -1;
    private static final int ANISOTROPY_PARAMETER = GL46C.GL_TEXTURE_MAX_ANISOTROPY;
    private static final int MAX_ANISOTROPY_PARAMETER = GL46C.GL_MAX_TEXTURE_MAX_ANISOTROPY;

    public static void bindTexture(int slot, GpuTexture texture) {
        if (texture instanceof GlTexture glTextureView) {
            GlStateManager._activeTexture(GL30C.GL_TEXTURE0 + slot);
            GlStateManager._bindTexture(glTextureView.glId());
        } else {
            throw new IllegalStateException("Vivecraft: only opengl textures are supported");
        }
    }

    /**
     * Generates mipmaps for the given GpuTexture
     *
     * @param texture GpuTexture to generate mipmaps for
     */
    public static void genMipmaps(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            int textureUnit = GlStateManager._getActiveTexture();
            int boundTexture = GlStateManager._getInteger(GL30C.GL_TEXTURE_BINDING_2D);

            GlStateManager._activeTexture(GL30C.GL_TEXTURE0);
            GlStateManager._bindTexture(glTexture.glId());

            GL30C.glGenerateMipmap(GL30C.GL_TEXTURE_2D);

            GlStateManager._activeTexture(textureUnit);
            GlStateManager._bindTexture(boundTexture);
        } else {
            throw new IllegalStateException("Vivecraft: only opengl textures are supported");
        }
    }

    /**
     * enabled anisotropic filtering for the given GpuTexture
     *
     * @param texture GpuTexture to enable anisotropic filtering for
     */
    public static void enableAnisotropicFiltering(GpuTexture texture) {
        if (supportsAnisotropicFiltering()) {
            if (texture instanceof GlTexture glTexture) {
                int textureUnit = GlStateManager._getActiveTexture();
                int boundTexture = GlStateManager._getInteger(GL30C.GL_TEXTURE_BINDING_2D);

                GlStateManager._activeTexture(GL30C.GL_TEXTURE0);
                GlStateManager._bindTexture(glTexture.glId());

                GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, ANISOTROPY_PARAMETER, ANISOTROPY_LEVEL);

                GlStateManager._activeTexture(textureUnit);
                GlStateManager._bindTexture(boundTexture);
            } else {
                throw new IllegalStateException("Vivecraft: only opengl textures are supported");
            }
        }
    }

    public static boolean supportsAnisotropicFiltering() {
        if (!CHECKED_ANISOTROPY) {
            if (RenderSystem.getDevice() instanceof GlDevice &&
                GLFW.glfwExtensionSupported("GL_ARB_texture_filter_anisotropic") ||
                GLFW.glfwExtensionSupported("GL_EXT_texture_filter_anisotropic"))
            {
                ANISOTROPY_SUPPORTED = true;
                // both the EXT and the ARB use the same parameters
                ANISOTROPY_LEVEL = Math.min(16, GlStateManager._getInteger(MAX_ANISOTROPY_PARAMETER));
            }
            CHECKED_ANISOTROPY = true;
        }
        return ANISOTROPY_SUPPORTED;
    }
}
