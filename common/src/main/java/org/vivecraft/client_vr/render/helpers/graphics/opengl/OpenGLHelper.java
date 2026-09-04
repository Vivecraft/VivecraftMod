package org.vivecraft.client_vr.render.helpers.graphics.opengl;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GLCapabilities;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.ImageFormat;
import org.vivecraft.client_vr.render.helpers.graphics.RawTexture;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;
import java.util.*;

public class OpenGLHelper implements GraphicsHelper {

    protected static boolean USE_GL_KHR_debug;
    protected static boolean USE_GL_EXT_debug_label;

    private Set<ImageFormat> supportedFormats = null;

    public OpenGLHelper() {
        GLCapabilities caps = GL.createCapabilities();
        USE_GL_KHR_debug = caps.GL_KHR_debug;
        USE_GL_EXT_debug_label = caps.GL_EXT_debug_label;
    }

    protected static GlDevice getGlDevice() {
        if (RenderSystem.getDevice().backend instanceof GlDevice glDevice) {
            return glDevice;
        } else {
            throw new IllegalArgumentException("Vivecraft: not an opengl device in opengl context");
        }
    }

    @Override
    public Type getType() {
        return Type.OPENGL;
    }

    @Override
    public long getTextureHandle(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            return glTexture.glId();
        }
        throw new IllegalArgumentException("Vivecraft: not an opengl texture in opengl context");
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
    public void genMipmaps(GpuTexture texture) {
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
                case GL30C.GL_INVALID_ENUM -> "invalid enum";
                case GL30C.GL_INVALID_VALUE -> "invalid value";
                case GL30C.GL_INVALID_OPERATION -> "invalid operation";
                case GL30C.GL_STACK_OVERFLOW -> "stack overflow";
                case GL30C.GL_STACK_UNDERFLOW -> "stack underflow";
                case GL30C.GL_OUT_OF_MEMORY -> "out of memory";
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
        return GL30C.glIsEnabled(GL30C.GL_STENCIL_TEST);
    }

    @Override
    public void setStencil(boolean state) {
        if (state) {
            GL30C.glEnable(GL30C.GL_STENCIL_TEST);
        } else {
            GL30C.glDisable(GL30C.GL_STENCIL_TEST);
        }
    }

    @Override
    public void flush() {
        GL30C.glFlush();
    }

    @Override
    public void blitTextures(RenderTarget[] sources, RawTexture... targets) {
        if (targets == null || sources.length != targets.length) {
            throw new IllegalArgumentException("Vivecraft: sources.length != targets.length");
        }
        for (int i = 0; i < sources.length; i++) {
            GlTexture source = (GlTexture) sources[i].getColorTexture();
            OpenGLRawTexture target = (OpenGLRawTexture) targets[i];
            getGlDevice().directStateAccess().blitFrameBuffers(
                getGlDevice().frameBufferCache().getFbo(getGlDevice().directStateAccess(), List.of(source), null),
                target.glFbo,
                0, 0, source.getWidth(0), source.getHeight(0),
                0, 0, target.width, target.height,
                GL30C.GL_COLOR_BUFFER_BIT, GL30C.GL_NEAREST);
        }
    }

    @Override
    public Set<ImageFormat> supportedImageFormats() {
        if (this.supportedFormats == null) {
            Set<ImageFormat> supported = new HashSet<>();
            for (ImageFormat format : ImageFormat.values()) {
                if (formatToAPI(format) != -1) {
                    supported.add(format);
                }
            }
            // make unmodifiable
            this.supportedFormats = Collections.unmodifiableSet(supported);
        }
        return this.supportedFormats;
    }

    @Override
    public int formatToAPI(ImageFormat format) {
        return switch (format) {
            case R8G8B8_SRGB -> GL30C.GL_SRGB8;
            case R8G8B8A8_UNORM -> GL30C.GL_RGBA8;
            case R8G8B8A8_SRGB -> GL30C.GL_SRGB8_ALPHA8;
            case R10G10B10A2_UINT -> GL30C.GL_RGB10_A2;
            case R16G16B16_UNORM -> GL30C.GL_RGB16;
            case R16G16B16_SFLOAT -> GL30C.GL_RGB16F;
            case R16G16B16A16_UNORM -> GL30C.GL_RGBA16;
            case R16G16B16A16_SFLOAT -> GL30C.GL_RGBA16F;
            case R32G32B32_SFLOAT -> GL30C.GL_RGB32F;
            case R32G32B32A32_SFLOAT -> GL30C.GL_RGBA32F;
            case B8G8R8A8_UNORM,
                 B8G8R8A8_SRGB,
                 B10G10R10A2_UNORM -> -1;
        };
    }

    @Override
    @Nullable
    public ImageFormat formatFromAPI(int format) {
        return switch (format) {
            case GL30C.GL_SRGB8 -> ImageFormat.R8G8B8_SRGB;
            case GL30C.GL_RGBA8 -> ImageFormat.R8G8B8A8_UNORM;
            case GL30C.GL_SRGB8_ALPHA8 -> ImageFormat.R8G8B8A8_SRGB;
            case GL30C.GL_RGB10_A2 -> ImageFormat.R10G10B10A2_UINT;
            case GL30C.GL_RGB16 -> ImageFormat.R16G16B16_UNORM;
            case GL30C.GL_RGB16F -> ImageFormat.R16G16B16_SFLOAT;
            case GL30C.GL_RGBA16 -> ImageFormat.R16G16B16A16_UNORM;
            case GL30C.GL_RGBA16F -> ImageFormat.R16G16B16A16_SFLOAT;
            case GL30C.GL_RGB32F -> ImageFormat.R32G32B32_SFLOAT;
            case GL30C.GL_RGBA32F -> ImageFormat.R32G32B32A32_SFLOAT;
            default -> null;
        };
    }

    public static int formatToPixelFormat(ImageFormat format) {
        return switch (format) {
            case R8G8B8A8_UNORM,
                 R8G8B8A8_SRGB,
                 R10G10B10A2_UINT,
                 R16G16B16A16_UNORM,
                 R16G16B16A16_SFLOAT,
                 R32G32B32A32_SFLOAT -> GL30C.GL_RGBA;
            case R8G8B8_SRGB,
                 R16G16B16_UNORM,
                 R16G16B16_SFLOAT,
                 R32G32B32_SFLOAT -> GL30C.GL_RGB;
            case B8G8R8A8_UNORM,
                 B8G8R8A8_SRGB,
                 B10G10R10A2_UNORM -> -1;
        };
    }
}
