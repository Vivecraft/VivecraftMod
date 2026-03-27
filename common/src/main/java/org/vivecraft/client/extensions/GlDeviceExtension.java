package org.vivecraft.client.extensions;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;

import javax.annotation.Nullable;

public interface GlDeviceExtension {
    /**
     * additional method to create a texture with a predefined id
     */
    GpuTexture vivecraft$createFixedIdTexture(
        @Nullable String label, int usageFlags, TextureFormat textureFormat, int width,
        int height, int depthLayers, int mipmapLevels, int texId);
}
