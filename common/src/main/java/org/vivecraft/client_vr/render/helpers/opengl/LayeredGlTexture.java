package org.vivecraft.client_vr.render.helpers.opengl;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

public class LayeredGlTexture extends GlTexture {

    private final int layer;
    private final Int2IntMap fboCache = new Int2IntOpenHashMap();

    public LayeredGlTexture(
        int usage, String label, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels, int id,
        int layer)
    {
        super(usage, label, format, width, height, depthOrLayers, mipLevels, id);
        this.layer = layer;
    }

    @Override
    public int getFbo(DirectStateAccess directStateAccess, @Nullable GpuTexture depthTexture) {
        int depthId = depthTexture == null ? 0 : ((GlTexture) depthTexture).glId();
        return this.fboCache.computeIfAbsent(depthId, (tex) -> {
            int frameBuffer = GlStateManager.glGenFramebuffers();
            int oldFbo = GlStateManager.getFrameBuffer(GL30C.GL_DRAW_FRAMEBUFFER);
            GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, frameBuffer);

            GL30.glFramebufferTextureLayer(GL30C.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.id, 0,
                this.layer);
            GL30.glFramebufferTexture2D(GL30C.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D,
                depthId, 0);

            GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, oldFbo);

            return frameBuffer;
        });
    }
}
