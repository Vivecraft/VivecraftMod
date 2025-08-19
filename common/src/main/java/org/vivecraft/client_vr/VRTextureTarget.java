package org.vivecraft.client_vr;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.util.Mth;
import org.vivecraft.Xplat;
import org.vivecraft.client.extensions.GlDeviceExtension;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.render.helpers.opengl.OpenGLHelper;

/**
 * extension of a regular RenderTarget that sets Vivecraft features on creation
 */
public class VRTextureTarget extends RenderTarget {

    public boolean anisotropicFiltering;

    private VRTextureTarget(
        String name, int width, int height, boolean useDepth, int texId, boolean linearFilter, boolean mipmaps,
        boolean anisotropicFiltering, boolean useStencil)
    {
        super(name, useDepth);
        RenderSystem.assertOnRenderThread();
        ((RenderTargetExtension) this).vivecraft$setLinearFilter(linearFilter);
        ((RenderTargetExtension) this).vivecraft$setMipmaps(mipmaps);
        this.anisotropicFiltering = anisotropicFiltering;

        // need to set this first, because the forge/neoforge stencil enabled does a resize
        this.viewWidth = width;
        this.viewHeight = height;
        this.width = width;
        this.height = height;

        if (useStencil && !Xplat.enableRenderTargetStencil(this)) {
            // use our stencil only if the modloader doesn't support it
            ((RenderTargetExtension) this).vivecraft$setStencil(true);
        }
        if (texId >= 0) {
            // hardcoded opengl here
            if (RenderSystem.getDevice() instanceof GlDevice glDevice) {
                this.colorTexture = ((GlDeviceExtension) glDevice).vivecraft$createFixedIdTexture(() -> this.label,
                    TextureFormat.RGBA8, width, height, mipmaps ? Math.max(Mth.log2(width), Mth.log2(height)) : 1,
                    texId);
    }

                this.colorTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
                this.setFilterMode(linearFilter ? FilterMode.LINEAR : FilterMode.NEAREST);
            } else {
                throw new IllegalStateException("Only Opengl is currently supported by Vivecraft");
            }
        } else {
            this.resize(width, height);
        }
    @Override
    public void createBuffers(int width, int height) {
        super.createBuffers(width, height);

        if (((RenderTargetExtension) this).vivecraft$hasMipmaps()) {
            if (this.anisotropicFiltering) {
                OpenGLHelper.enableAnisotropicFiltering(this);
            }
            // generate mipmaps so they are initialized
            OpenGLHelper.genMipmaps(this);
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
                this.viewWidth, this.viewHeight,
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
        private int texId = -1;

        private boolean linearFilter;

        private boolean mipmaps;
        private boolean anisotropicFiltering;

        private boolean stencil;

        private Builder(String name) {
            this.name = name;
        }

        public Builder withSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder withTexId(int texId) {
            this.texId = texId;
            return this;
        }

        public Builder withDepth() {
            this.useDepth = true;
            return this;
        }

        public Builder withLinearFilter() {
            this.linearFilter = true;
            return this;
        }

        public Builder withMipmaps(boolean useMipmaps) {
            this.mipmaps = useMipmaps;
            return this;
        }

        public Builder withAnisotropicFiltering(boolean useAF) {
            this.anisotropicFiltering = useAF;
            return this;
        }

        public Builder withStencil(boolean useStencil) {
            this.stencil = useStencil;
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
                this.texId,
                this.linearFilter,
                this.mipmaps,
                this.anisotropicFiltering,
                this.stencil);
        }
    }
}
