package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.OVRMultiview;

public class MultiViewRenderTarget extends RenderTarget {
    private final int views;

    public MultiViewRenderTarget(boolean useDepth, int width, int height, int colorTexture, int views) {
        super(useDepth);

        this.views = views;
        this.colorTextureId = colorTexture;
        this.resize(width, height);
    }

    @Override
    public void createBuffers(int width, int height) {
        RenderSystem.assertOnRenderThreadOrInit();
        int i = RenderSystem.maxSupportedTextureSize();
        if (width > 0 && width <= i && height > 0 && height <= i) {
            this.viewWidth = width;
            this.viewHeight = height;
            this.width = width;
            this.height = height;
            this.frameBufferId = GlStateManager.glGenFramebuffers();
            boolean resetColor = false;
            if(colorTextureId == -1) {
                colorTextureId = TextureUtil.generateTextureId();
                resetColor = true;
            }
            if (this.useDepth) {
                this.depthBufferId = TextureUtil.generateTextureId();
                GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.depthBufferId);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10241, 9728);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10240, 9728);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 34892, 0);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10242, 33071);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10243, 33071);
                GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL30.GL_DEPTH_COMPONENT16, width, height, this.views);
            }

            //this.setFilterMode(9728, true);
            GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.colorTextureId);
            if(resetColor) {
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10242, 33071);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10243, 33071);
                GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL30.GL_RGBA8, width, height, this.views);
            }
            GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
            OVRMultiview.glFramebufferTextureMultiviewOVR(36160, GL30.GL_COLOR_ATTACHMENT0,
                this.colorTextureId, 0, 0, views);
            if (this.useDepth) {
                OVRMultiview.glFramebufferTextureMultiviewOVR(36160, 36096, this.depthBufferId,
                    0, 0, views);
            }

            this.checkStatus();
            this.clear();
            this.unbindRead();
        } else {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + i + ")");
        }
    }

    @Override
    public void bindRead() {
        RenderSystem.assertOnRenderThread();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.colorTextureId);
    }

    @Override
    public void unbindRead() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._bindTexture(0);
    }
}
