package org.vivecraft.client_vr;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import org.vivecraft.client_vr.render.helpers.opengl.LayeredGlTexture;
import org.vivecraft.client_vr.render.helpers.opengl.LayeredGlTextureView;

/**
 * RenderTarget that has a layer of a color texture and no depth
 */
public class VRLayeredRenderTarget extends RenderTarget {

    private final int layer;

    public VRLayeredRenderTarget(String name, int width, int height, int texId, int layer) {
        super(name, false);
        RenderSystem.assertOnRenderThread();

        // need to set this first, because the forge/neoforge stencil enabled does a resize
        this.width = width;
        this.height = height;
        this.layer = layer;

        // hardcoded opengl here
        if (RenderSystem.getDevice().backend instanceof GlDevice) {
            this.colorTexture = new LayeredGlTexture(
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_TEXTURE_BINDING |
                    GpuTexture.USAGE_RENDER_ATTACHMENT, this.label + " / Color", TextureFormat.RGBA8, width, height, 1,
                1, texId, layer);
            this.colorTextureView = new LayeredGlTextureView((LayeredGlTexture) this.colorTexture, 0,
                this.colorTexture.getMipLevels());
        } else {
            throw new IllegalStateException("Only Opengl is currently supported by Vivecraft");
        }
    }

    @Override
    public String toString() {
        return """
            
            Vivecraft LayeredRenderTarget: %s
            Size: %s x %s
            Tex ID: %s
            Layer: %s"""
            .formatted(
                this.label,
                this.width, this.height,
                this.colorTexture.getLabel(),
                this.layer);
    }
}
