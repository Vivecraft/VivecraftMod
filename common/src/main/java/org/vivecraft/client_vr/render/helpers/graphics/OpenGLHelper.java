package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL46C;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashMap;
import java.util.Map;

public class OpenGLHelper implements GraphicsHelper {

    private boolean checkedAnisotropy = false;
    private boolean anisotropySupported = false;
    private int anisotropyLevel = -1;
    private static final int ANISOTROPY_PARAMETER = GL46C.GL_TEXTURE_MAX_ANISOTROPY;
    private static final int MAX_ANISOTROPY_PARAMETER = GL46C.GL_MAX_TEXTURE_MAX_ANISOTROPY;

    @Override
    public long getTextureHandle(RenderTarget texture) {
        return texture.getColorTextureId();
    }

    @Override
    public void genMipmaps(RenderTarget texture) {
        texture.bindRead();
        GL30C.glGenerateMipmap(GL30C.GL_TEXTURE_2D);
        texture.unbindRead();
    }

    /**
     * enabled anisotropic filtering for the given RenderTarget
     *
     * @param texture RenderTarget to enable anisotropic filtering for
     */
    @Override
    public void enableAnisotropicFiltering(RenderTarget texture) {
        if (supportsAnisotropicFiltering()) {
            texture.bindRead();
            RenderSystem.texParameter(GL30C.GL_TEXTURE_2D, ANISOTROPY_PARAMETER, this.anisotropyLevel);
            texture.unbindRead();
        }
    }

    private boolean supportsAnisotropicFiltering() {
        if (!this.checkedAnisotropy) {
            if (GLFW.glfwExtensionSupported("GL_ARB_texture_filter_anisotropic") ||
                GLFW.glfwExtensionSupported("GL_EXT_texture_filter_anisotropic"))
            {
                this.anisotropySupported = true;
                // both the EXT and the ARB use the same parameters
                this.anisotropyLevel = Math.min(16, GlStateManager._getInteger(MAX_ANISOTROPY_PARAMETER));
            }
            this.checkedAnisotropy = true;
        }
        return this.anisotropySupported;
    }

    private final Map<String, Pair<Integer, Integer>> glErrors = new HashMap<>();

    /**
     * checks if there were any opengl errors since this was last called
     *
     * @param errorSection name of the section that is checked, this gets logged if there are any errors
     * @return error string if there was one
     */
    @Override
    public String checkError(String errorSection) {
        int error = GlStateManager._getError();
        int count = 0;
        Pair<Integer, Integer> oldError = this.glErrors.get(errorSection);
        if (error != 0 && oldError != null && oldError.getLeft() == error) {
            count = oldError.getRight() + 1;
        }
        this.glErrors.put(errorSection, Pair.of(error, count));
        if (error != 0 && count < 5) {
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
            VRSettings.LOGGER.error("Vivecraft: @ {}", errorSection);
            VRSettings.LOGGER.error("Vivecraft: {}: {}", error, errorString);
            return errorString;
        } else if (count == 5) {
            VRSettings.LOGGER.error("Vivecraft: repeated gl errors for {}, not logging anymore", errorSection);
        }
        return "";
    }

    @Override
    public boolean isStencil() {
        return GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
    }

    @Override
    public void setStencil(boolean state) {
        if (state) {
            GL11C.glEnable(GL11C.GL_STENCIL_TEST);
        } else {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }

    @Override
    public void flush() {
        GL11C.glFlush();
    }
}
