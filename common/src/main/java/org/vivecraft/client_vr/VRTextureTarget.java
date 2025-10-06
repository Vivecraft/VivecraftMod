package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.vivecraft.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.render.helpers.opengl.OpenGLHelper;

/**
 * extension of a regular RenderTarget that sets Vivecraft features on creation
 */
public class VRTextureTarget extends RenderTarget {

    private final String name;

    public boolean anisotropicFiltering;

    private VRTextureTarget(
        String name, int width, int height, boolean useDepth, int texId, boolean linearFilter, boolean mipmaps,
        boolean anisotropicFiltering, boolean useStencil, Vector4fc clearColor)
    {
        super(useDepth);

        this.setClearColor(clearColor.x(), clearColor.y(), clearColor.z(), clearColor.w());

        this.name = name;
        RenderSystem.assertOnGameThreadOrInit();
        ((RenderTargetExtension) this).vivecraft$setTexId(texId);
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
        this.resize(width, height, Minecraft.ON_OSX);
    }

    @Override
    public void createBuffers(int width, int height, boolean clearError) {
        super.createBuffers(width, height, clearError);

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
            FB ID: %s
            Tex ID: %s"""
            .formatted(
                this.name,
                this.viewWidth, this.viewHeight,
                this.frameBufferId,
                this.colorTextureId);
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

        private final Vector4f clearColor = new Vector4f(0);

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

        public Builder withClearColor(float red, float green, float blue, float alpha) {
            this.clearColor.set(red, green, blue, alpha);
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
                this.stencil,
                this.clearColor);
        }
    }
}
