package org.vivecraft.client.extensions;

import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL30;

public interface RenderTargetExtension {

    default void vivecraft$blitToScreen(int i, int viewWidth, int viewHeight, int j, boolean b, float f, float g, boolean c) {
        vivecraft$blitToScreen(null, i, viewWidth, viewHeight, j, b, f, g, c);
    }

    void vivecraft$blitToScreen(ShaderInstance instance, int i, int viewWidth, int viewHeight, int j, boolean b, float f, float g, boolean c);

    default void vivecraft$genMipMaps() {
        GL30.glGenerateMipmap(3553);
    }

    void vivecraft$setTextid(int texid);

    void vivecraft$setColorid(int colorid);

    void vivecraft$setUseStencil(boolean useStencil);

    boolean vivecraft$getUseStencil();

    void vivecraft$isLinearFilter(boolean linearFilter);

    void vivecraft$blitFovReduction(ShaderInstance instance, int width, int height);
}
