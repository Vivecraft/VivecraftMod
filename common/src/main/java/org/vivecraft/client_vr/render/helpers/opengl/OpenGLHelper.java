package org.vivecraft.client_vr.render.helpers.opengl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.lwjgl.opengl.GL30C;

public class OpenGLHelper {

    public static void bindTexture(int slot, GpuTextureView texture) {
        if (texture instanceof GlTextureView glTextureView) {
            GlStateManager._activeTexture(GL30C.GL_TEXTURE0 + slot);
            GlStateManager._bindTexture(glTextureView.texture().glId());
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

    public static void blitFramebuffer(
        GpuTexture source, GpuTexture target, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0,
        int dstX1, int dstY1, int mask, int filter)
    {
        if (RenderSystem.getDevice() instanceof GlDevice glDevice && source instanceof GlTexture glSource &&
            target instanceof GlTexture glTarget)
        {
            glDevice.directStateAccess().blitFrameBuffers(glSource.getFbo(glDevice.directStateAccess(), null),
                glTarget.getFbo(glDevice.directStateAccess(), null),
                srcX0, srcY0, srcX1, srcY1,
                dstX0, dstY0, dstX1, dstY1,
                mask, filter);
        } else {
            throw new IllegalStateException("Vivecraft: only opengl textures are supported");
        }
    }

}
