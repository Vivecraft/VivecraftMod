package org.vivecraft.client_vr.render.helpers.graphics;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashMap;
import java.util.Map;

public class OpenGLHelper implements GraphicsHelper {

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.OPENGL;
    }

    @Override
    public long getTextureHandle(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            return glTexture.glId();
        }
        throw new IllegalArgumentException("Vivecraft: not an opengl texture in opengl context");
    }

    /**
     * Generates mipmaps for the given GpuTexture
     *
     * @param texture GpuTexture to generate mipmaps for
     */
    public static void genMipmaps(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            int textureUnit = GlStateManager._getInteger(GL30C.GL_ACTIVE_TEXTURE);
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
