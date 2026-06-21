package org.vivecraft.mixin.client.blaze3d.opengl;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.opengl.*;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.vivecraft.client.extensions.GlDeviceExtension;

import javax.annotation.Nullable;

@Mixin(GlDevice.class)
public class GlDeviceMixin implements GlDeviceExtension {
    @Shadow
    @Final
    private GlDebugLabel debugLabels;

    /**
     * copy of {@link GlDevice#createTexture(String, int, TextureFormat, int, int, int, int)} but with a fixes texture id
     */
    @Override
    public GpuTexture vivecraft$createFixedIdTexture(
        @Nullable String label, int usageFlags, TextureFormat textureFormat, int width,
        int height, int depthLayers, int mipmapLevels, int texId)
    {
        if (mipmapLevels < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        } else if (depthLayers < 1) {
            throw new IllegalArgumentException("depthOrLayers must be at least 1");
        } else {
            if ((usageFlags & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) != 0) {
                throw new UnsupportedOperationException("CubeMap textures are not supported");
            } else if (depthLayers > 1) {
                throw new UnsupportedOperationException("Array or 3D textures are not supported");
            }

            GlStateManager.clearGlErrors();
            if (label == null) {
                label = String.valueOf(texId);
            }

            GlStateManager._bindTexture(texId);
            GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAX_LEVEL, mipmapLevels - 1);
            GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_LOD, 0);
            GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAX_LOD, mipmapLevels - 1);
            if (textureFormat.hasDepthAspect()) {
                GlStateManager._texParameter(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_NONE);
            }

            for (int m = 0; m < mipmapLevels; m++) {
                GlStateManager._texImage2D(GL30C.GL_TEXTURE_2D, m, GlConst.toGlInternalId(textureFormat), width >> m,
                    height >> m, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
            }

            int error = GlStateManager._getError();
            if (error == GL30C.GL_OUT_OF_MEMORY) {
                throw new GpuOutOfMemoryException(
                    "Could not allocate texture of " + width + "x" + height + " for " + label);
            } else if (error != 0) {
                throw new IllegalStateException("OpenGL error " + error);
            } else {
                GlTexture glTexture = new GlTexture(usageFlags, label, textureFormat, width, height, depthLayers,
                    mipmapLevels, texId);
                this.debugLabels.applyLabel(glTexture);
                return glTexture;
            }
        }
    }
}
