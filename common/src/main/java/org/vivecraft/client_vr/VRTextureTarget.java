package org.vivecraft.client_vr;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.vivecraft.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;

import javax.annotation.Nullable;

/**
 * extension of a regular RenderTarget that sets Vivecraft features on creation
 */
public class VRTextureTarget extends RenderTarget {

    @Nullable
    private final Vector4fc clearColor;

    public final GpuFormat gpuFormat;

    private VRTextureTarget(
        String name, int width, int height, boolean useDepth, boolean mipmaps, boolean useStencil,
        @Nullable Vector4fc clearColor, GpuFormat format)
    {
        super(name, useDepth, format);
        this.gpuFormat = format;
        RenderSystem.assertOnRenderThread();
        ((RenderTargetExtension) this).vivecraft$setMipmaps(mipmaps);
        this.clearColor = clearColor;

        // need to set this first, because the forge/neoforge stencil enabled does a resize
        this.width = width;
        this.height = height;

        if (useStencil && !Xplat.INSTANCE.enableRenderTargetStencil(this)) {
            // use our stencil only if the modloader doesn't support it
            ((RenderTargetExtension) this).vivecraft$setStencil(true);
        }
        this.resize(width, height);
    }

    @Override
    public void createBuffers(int width, int height) {
        super.createBuffers(width, height);

        if (this.clearColor != null) {
            if (this.useDepth) {
                RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                    this.colorTexture, this.clearColor,
                    this.depthTexture, 0.0);
            } else {
                RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.colorTexture, this.clearColor);
            }
        }

        if (((RenderTargetExtension) this).vivecraft$hasMipmaps()) {
            // generate mipmaps so they are initialized
            GraphicsHelper.INSTANCE.genMipmaps(this.colorTexture);
        }
    }

    @Override
    public String toString() {
        return """
            
            Vivecraft RenderTarget: %s
            Size: %s x %s
            Tex ID: %s"""
            .formatted(
                this.label,
                this.width, this.height,
                this.colorTexture.getLabel());
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;

        private int width;
        private int height;

        private boolean useDepth;

        private boolean mipmaps;

        private boolean stencil;

        private Vector4f clearColor;

        private GpuFormat format = GpuFormat.RGBA8_UNORM;

        private Builder(String name) {
            this.name = name;
        }

        public Builder withSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withDepth() {
            this.useDepth = true;
            return this;
        }

        public Builder withMipmaps(boolean useMipmaps) {
            this.mipmaps = useMipmaps;
            return this;
        }

        public Builder withStencil(boolean useStencil) {
            this.stencil = useStencil;
            return this;
        }

        public Builder withClearColor(float red, float green, float blue, float alpha) {
            this.clearColor = new Vector4f(red, green, blue, alpha);
            return this;
        }

        public Builder withFormat(GpuFormat format) {
            this.format = format;
            return this;
        }

        public VRTextureTarget build() {
            if (this.width <= 0 || this.height <= 0) {
                throw new IllegalArgumentException("Width and height must be greater than 0");
            }
            return new VRTextureTarget(
                this.name,
                this.width, this.height,
                this.useDepth,
                this.mipmaps,
                this.stencil,
                this.clearColor,
                this.format);
        }
    }
}
