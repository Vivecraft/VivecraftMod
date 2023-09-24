package org.vivecraft.client.extensions;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;

import net.minecraft.client.renderer.ShaderInstance;

public interface RenderTargetExtension
{

    String getName();

    void clearWithColor(float r, float g, float b, float a, boolean isMac);

    default void blitToScreen(
        int left, int width, int height, int top, boolean disableBlend, float xCropFactor,
        float yCropFactor, boolean keepAspect
    )
    {
        blitToScreen(null, left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
    }


    void blitToScreen(
        ShaderInstance instance, int left, int width, int height, int top, boolean disableBlend, float xCropFactor,
        float yCropFactor, boolean keepAspect
    );

    default void genMipMaps()
    {
        GL30C.glGenerateMipmap(GL11C.GL_TEXTURE_2D);
    }

    void setTextid(int texid);

    void setUseStencil(boolean useStencil);

    boolean getUseStencil();

    void isLinearFilter(boolean linearFilter);

    void blitFovReduction(ShaderInstance instance, int width, int height);
}
