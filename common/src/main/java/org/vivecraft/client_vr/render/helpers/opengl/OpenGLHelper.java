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
}
