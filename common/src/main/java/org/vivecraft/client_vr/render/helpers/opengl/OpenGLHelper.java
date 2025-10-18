package org.vivecraft.client_vr.render.helpers.opengl;

import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL46C;
import org.vivecraft.client_vr.render.helpers.GraphicsAPI;
import org.vivecraft.client_vr.settings.VRSettings;

public class OpenGLHelper extends GraphicsAPI {

    private static boolean CHECKED_ANISOTROPY = false;
    private static boolean ANISOTROPY_SUPPORTED = false;
    private static int ANISOTROPY_LEVEL = -1;
    private static final int ANISOTROPY_PARAMETER = GL46C.GL_TEXTURE_MAX_ANISOTROPY;
    private static final int MAX_ANISOTROPY_PARAMETER = GL46C.GL_MAX_TEXTURE_MAX_ANISOTROPY;

    public OpenGLHelper() {
        GraphicsAPI.INSTANCE = this;
    }

    public static void bindTexture(int slot, GpuTextureView texture) {
        if (texture instanceof GlTextureView glTextureView) {
            GlStateManager._activeTexture(GL30C.GL_TEXTURE0 + slot);
            GlStateManager._bindTexture(glTextureView.texture().glId());
        } else {
            throw new IllegalStateException("Vivecraft: only opengl textures are supported");
        }
    }

    @Override
    public String checkError(String context) {
        int error = GlStateManager._getError();
        if (error != 0) {
            String errorString = switch (error) {
                case GL11C.GL_INVALID_ENUM -> "invalid enum";
                case GL11C.GL_INVALID_VALUE -> "invalid value";
                case GL11C.GL_INVALID_OPERATION -> "invalid operation";
                case GL11C.GL_STACK_OVERFLOW -> "stack overflow";
                case GL11C.GL_STACK_UNDERFLOW -> "stack underflow";
                case GL11C.GL_OUT_OF_MEMORY -> "out of memory";
                case GL30C.GL_INVALID_FRAMEBUFFER_OPERATION -> "framebuffer is not complete";
                default -> "unknown error";
            };
            VRSettings.LOGGER.error("Vivecraft: ########## GL ERROR ##########");
            VRSettings.LOGGER.error("Vivecraft: @ {}", context);
            VRSettings.LOGGER.error("Vivecraft: {}: {}", error, errorString);
            return errorString;
        } else {
            return "";
        }
    }

    @Override
    public Type type() {
        return Type.OPENGL;
    }

    @Override
    public boolean isStencilEnabled() {
        return GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
    }

    @Override
    public void enableStencil() {
        GL11C.glEnable(GL11C.GL_STENCIL_TEST);
    }

    @Override
    public void disableStencil() {
        GL11C.glDisable(GL11C.GL_STENCIL_TEST);
    }

    @Override
    public void flushPreSubmit() {}

    @Override
    public void flushPostSubmit() {
        GL11C.glFlush();
    }


    @Override
    public void changeTexturePurpose(GpuTexture texture, TexturePurpose purpose) {
        // don't need to change this for opengl
    }

    @Override
    public long getImageHandle(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            return glTexture.glId();
        } else {
            throw new IllegalStateException("Vivecraft: only opengl textures are supported");
        }
    }

    @Override
    public int getFormat(TextureFormat format) {
        return GlConst.toGlInternalId(format);
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
