package org.vivecraft.client.extensions;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.textures.GpuTexture;

import javax.annotation.Nullable;

public interface GlDeviceExtension {
    /**
     * additional method to create a texture with a predefined id
     */
    GpuTexture vivecraft$createFixedIdTexture(
        @Nullable String label, int usageFlags, GpuFormat textureFormat, int width,
        int height, int depthLayers, int mipmapLevels, int texId);
}
