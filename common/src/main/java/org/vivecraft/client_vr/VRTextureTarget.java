package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.OVRMultiview;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;

import java.nio.IntBuffer;

/**
 * extension of a regular RenderTarget that sets Vivecraft features on creation
 */
public class VRTextureTarget extends RenderTarget {

    private final String name;
    private boolean isMultiview = false;
    public final int index;

    public VRTextureTarget(
        String name, int width, int height, boolean useDepth, int texId, boolean linearFilter, boolean mipmaps,
        boolean useStencil)
    {
        super(useDepth);
        this.name = name;
        this.index = 0;
        RenderSystem.assertOnRenderThreadOrInit();
        ((RenderTargetExtension) this).vivecraft$setTexId(texId);
        ((RenderTargetExtension) this).vivecraft$setLinearFilter(linearFilter);
        ((RenderTargetExtension) this).vivecraft$setMipmaps(mipmaps);

        // need to set this first, because the forge/neoforge stencil enabled does a resize
        this.viewWidth = width;
        this.viewHeight = height;

        if (useStencil && !Xplat.enableRenderTargetStencil(this)) {
            // use our stencil only if the modloader doesn't support it
            ((RenderTargetExtension) this).vivecraft$setStencil(true);
        }
        this.resize(width, height);

        this.setClearColor(0, 0, 0, 0);
    }

    public VRTextureTarget(String name, int width, int height, int colorId, int index, boolean isMultiview) {
        super(false);
        this.name = name;
        this.isMultiview = isMultiview;
        this.index = index;
        RenderSystem.assertOnRenderThreadOrInit();
        this.colorTextureId = colorId;
        this.resize(width, height);
        if (!isMultiview) {
            // free the old one when setting a new one
            if (this.colorTextureId != -1) {
                TextureUtil.releaseTextureId(this.colorTextureId);
            }

            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBufferId);
            // unset the old GL_COLOR_ATTACHMENT0

            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D,
                0,
                0);
            GL30.glFramebufferTextureLayer(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorId, 0, index);
        }

        // unbind the framebuffer
        this.unbindRead();
        this.unbindWrite();

        this.setClearColor(0, 0, 0, 0);
    }

    @Override
    public void createBuffers(int width, int height) {
        if (!isMultiview) {
            super.createBuffers(width, height);
            return;
        }

        RenderSystem.assertOnRenderThreadOrInit();
        int i = RenderSystem.maxSupportedTextureSize();
        if (width > 0 && width <= i && height > 0 && height <= i) {
            this.viewWidth = width;
            this.viewHeight = height;
            this.width = width;
            this.height = height;
            this.frameBufferId = GlStateManager.glGenFramebuffers();
            if (this.useDepth) {
                this.depthBufferId = TextureUtil.generateTextureId();
                GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.colorTextureId);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10241, 9728);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10240, 9728);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 34892, 0);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10242, 33071);
                GlStateManager._texParameter(GL30.GL_TEXTURE_2D_ARRAY, 10243, 33071);
                GL42.glTexStorage3D(GL30.GL_TEXTURE_2D_ARRAY, 1, GL30.GL_DEPTH_COMPONENT24, width, height, 2);
            }

            //this.setFilterMode(9728, true);
            GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, this.colorTextureId);
            GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
            OVRMultiview.glFramebufferTextureMultiviewOVR(36160, 36064,
                this.colorTextureId, 0, 0, 2);
            if (this.useDepth) {
                OVRMultiview.glFramebufferTextureMultiviewOVR(36160, 36096, this.depthBufferId,
                    0, 0, 2);
            }

            this.checkStatus();
            this.clear();
            this.unbindRead();
        } else {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + i + ")");
        }
    }

    @Override
    public String toString() {
        return """
            
            Vivecraft RenderTarget: %s
            Size: %s x %s
            FB ID: %s
            Tex ID: %s"""
            .formatted(
                this.name,
                this.viewWidth, this.viewHeight,
                this.frameBufferId,
                this.colorTextureId);
    }
}
