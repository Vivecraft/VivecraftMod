package org.vivecraft.client_vr.render.helpers.opengl;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL46C;

public class OpenGLHelper {

    private static boolean CHECKED_ANISOTROPY = false;
    private static boolean ANISOTROPY_SUPPORTED = false;
    private static int ANISOTROPY_LEVEL = -1;
    private static final int ANISOTROPY_PARAMETER = GL46C.GL_TEXTURE_MAX_ANISOTROPY;
    private static final int MAX_ANISOTROPY_PARAMETER = GL46C.GL_MAX_TEXTURE_MAX_ANISOTROPY;

    /**
     * Generates mipmaps for the given RenderTarget
     *
     * @param renderTarget RenderTarget to generate mipmaps for
     */
    public static void genMipmaps(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            int textureUnit = GlStateManager._getInteger(GL30C.GL_ACTIVE_TEXTURE);
            int boundTexture = GlStateManager._getInteger(GL30C.GL_TEXTURE_BINDING_2D);
            GL30C.glActiveTexture(GL30C.GL_TEXTURE0);
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, glTexture.glId());

            GL30C.glGenerateMipmap(GL30C.GL_TEXTURE_2D);

            GL30C.glActiveTexture(textureUnit);
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, boundTexture);
        } else {
            throw new IllegalStateException("Vivecraft: only opengl textures are supported");
        }
    }

    /**
     * enabled anisotropic filtering for the given RenderTarget
     *
     * @param renderTarget RenderTarget to enable Anisotropic Filtering for
     */
    public static void enableAnisotropicFiltering(RenderTarget renderTarget) {
        if (supportsAnisotropicFiltering()) {
            renderTarget.bindRead();
            RenderSystem.texParameter(GL30C.GL_TEXTURE_2D, ANISOTROPY_PARAMETER, ANISOTROPY_LEVEL);
            renderTarget.unbindRead();
        }
    }

    public static boolean supportsAnisotropicFiltering() {
        if (!CHECKED_ANISOTROPY) {
            if (GLFW.glfwExtensionSupported("GL_ARB_texture_filter_anisotropic") ||
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
