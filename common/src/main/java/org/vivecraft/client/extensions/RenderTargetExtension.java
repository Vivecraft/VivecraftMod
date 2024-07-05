package org.vivecraft.client.extensions;

import net.minecraft.client.renderer.ShaderInstance;

public interface RenderTargetExtension {

    default void vivecraft$blitToScreen(int left, int width, int height, int top, boolean disableBlend, float xCropFactor, float yCropFactor, boolean keepAspect) {
        vivecraft$blitToScreen(null, left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
    }

    void vivecraft$blitToScreen(ShaderInstance instance, int left, int width, int height, int top, boolean disableBlend, float xCropFactor, float yCropFactor, boolean keepAspect);

    void vivecraft$setTextId(int texid);

    void vivecraft$setStencil(boolean stencil);

    boolean vivecraft$hasStencil();

    void vivecraft$setLinearFilter(boolean linearFilter);

    void vivecraft$setMipmaps(boolean mipmaps);

    boolean vivecraft$hasMipmaps();
}
