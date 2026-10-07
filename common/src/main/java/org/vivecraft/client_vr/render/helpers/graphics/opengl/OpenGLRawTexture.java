package org.vivecraft.client_vr.render.helpers.graphics.opengl;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.lwjgl.opengl.*;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.render.helpers.graphics.ImageFormat;
import org.vivecraft.client_vr.render.helpers.graphics.RawTexture;

public class OpenGLRawTexture implements RawTexture {

    protected int width;
    protected int height;
    protected ImageFormat format;

    protected final boolean externalImage;
    protected final int glImage;
    protected final int glFbo;

    protected boolean destroyed;

    private OpenGLRawTexture(
        String name, int width, int height, ImageFormat format, int glImage, int layer, boolean external)
    {
        this.width = width;
        this.height = height;
        this.format = format;

        this.glImage = glImage;
        this.externalImage = external;

        this.glFbo = GlStateManager.glGenFramebuffers();

        int oldFbo = GlStateManager.getFrameBuffer(GL45C.GL_DRAW_FRAMEBUFFER);
        GlStateManager._glBindFramebuffer(GL45C.GL_DRAW_FRAMEBUFFER, this.glFbo);

        if (layer == -1) {
            GlStateManager._glFramebufferTexture2D(
                GL45C.GL_DRAW_FRAMEBUFFER,
                GL45C.GL_COLOR_ATTACHMENT0,
                GL45C.GL_TEXTURE_2D,
                this.glImage,
                0);
        } else {
            GL45C.glFramebufferTextureLayer(
                GL30C.GL_DRAW_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0,
                this.glImage,
                0,
                layer);
        }

        GlStateManager._glBindFramebuffer(GL45C.GL_DRAW_FRAMEBUFFER, oldFbo);

        if (OpenGLHelper.getGlDevice().debugLabels().exists()) {
            if (OpenGLHelper.USE_GL_KHR_debug) {
                KHRDebug.glObjectLabel(
                    GL45C.GL_TEXTURE, this.glImage,
                    name.substring(0, Math.min(name.length(), GL45C.glGetInteger(GL45C.GL_MAX_LABEL_LENGTH))));
            } else if (OpenGLHelper.USE_GL_EXT_debug_label) {
                EXTDebugLabel.glLabelObjectEXT(GL45C.GL_TEXTURE, this.glImage,
                    name.substring(0, Math.min(name.length(), 256)));
            }
        }
    }

    /**
     * creates a RawTexture that holds a reference to an externally created texture
     *
     * @param name    name of the texture
     * @param width   width of the external texture
     * @param height  height of the external texture
     * @param format  image format of the external texture
     * @param glImage handle to the external texture
     * @param layer   which layer of an array texture to represent, if the texture is not an array, use {@code -1}
     * @return a wrapper to the external texture
     */
    public static OpenGLRawTexture link(
        String name, int width, int height, ImageFormat format, int glImage, int layer)
    {
        return new OpenGLRawTexture(name, width, height, format, glImage, layer, true);
    }

    /**
     * creates a RawTexture of the give dimensions and format
     *
     * @param name   name of the texture
     * @param width  width of the texture to create
     * @param height height of the texture to create
     * @param format image format of the texture to create
     * @return the created texture
     */
    public static OpenGLRawTexture create(String name, int width, int height, ImageFormat format) {
        // query current bound texture
        int prevTexture = GlStateManager._getInteger(GL45C.GL_TEXTURE_BINDING_2D);

        // create new texture
        int glImage = GlStateManager._genTexture();
        GlStateManager._bindTexture(glImage);
        GlStateManager._texParameter(GL45C.GL_TEXTURE_2D, GL45C.GL_TEXTURE_MIN_FILTER, GL45C.GL_LINEAR);
        GlStateManager._texParameter(GL45C.GL_TEXTURE_2D, GL45C.GL_TEXTURE_MAG_FILTER, GL45C.GL_LINEAR);
        GlStateManager._texImage2D(GL45C.GL_TEXTURE_2D,
            0,
            GraphicsHelper.INSTANCE.formatToAPI(format),
            width, height,
            0,
            OpenGLHelper.formatToPixelFormat(format),
            GL45C.GL_UNSIGNED_BYTE,
            null);

        // restore old state
        GlStateManager._bindTexture(prevTexture);

        return new OpenGLRawTexture(name, width, height, format, glImage, -1, false);
    }

    @Override
    public long getHandle() {
        return this.glImage;
    }

    @Override
    public void destroy() {
        if (!this.destroyed) {
            if (!this.externalImage) {
                // destroy opengl texture
                GlStateManager._deleteTexture(this.glImage);
            }
            // destroy fbo
            GlStateManager._glDeleteFramebuffers(this.glFbo);

            this.destroyed = true;
        }
    }
}
