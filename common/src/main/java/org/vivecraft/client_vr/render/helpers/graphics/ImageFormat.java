package org.vivecraft.client_vr.render.helpers.graphics;

public enum ImageFormat {
    R8G8B8_SRGB(true),
    R8G8B8A8_UNORM,
    R8G8B8A8_SRGB(true),
    B8G8R8A8_UNORM,
    B8G8R8A8_SRGB(true),
    R10G10B10A2_UINT,
    B10G10R10A2_UNORM,
    R16G16B16_UNORM,
    R16G16B16_SFLOAT,
    R16G16B16A16_UNORM,
    R16G16B16A16_SFLOAT,
    R32G32B32_SFLOAT,
    R32G32B32A32_SFLOAT;

    public final boolean srgb;

    ImageFormat() {
        this.srgb = false;
    }

    ImageFormat(boolean srgb) {
        this.srgb = srgb;
    }
}
