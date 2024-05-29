package org.vivecraft.client.extensions;

import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL30C;

public interface RenderTargetExtension {

    default void vivecraft$blitToScreen(int left, int width, int height, int top, boolean disableBlend, float xCropFactor, float yCropFactor, boolean keepAspect) {
        vivecraft$blitToScreen(null, left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
    }

    void vivecraft$blitToScreen(ShaderInstance instance, int left, int width, int height, int top, boolean disableBlend, float xCropFactor, float yCropFactor, boolean keepAspect);

    default void vivecraft$genMipMaps() {
        GL30C.glGenerateMipmap(GL30C.GL_TEXTURE_2D);
    }

    void vivecraft$setTextid(int texid);

    void vivecraft$setUseStencil(boolean useStencil);

    boolean vivecraft$getUseStencil();

    void vivecraft$isLinearFilter(boolean linearFilter);
}
