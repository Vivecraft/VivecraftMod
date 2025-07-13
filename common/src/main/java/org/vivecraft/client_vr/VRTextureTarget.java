package org.vivecraft.client_vr;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.util.Mth;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.GlDeviceExtension;
import org.vivecraft.client.extensions.RenderTargetExtension;

/**
 * extension of a regular RenderTarget that sets Vivecraft features on creation
 */
public class VRTextureTarget extends RenderTarget {

    public VRTextureTarget(
        String name, int width, int height, boolean useDepth, int texId, boolean linearFilter, boolean mipmaps,
        boolean useStencil)
    {
        super(name, useDepth);
        RenderSystem.assertOnRenderThread();
        ((RenderTargetExtension) this).vivecraft$setLinearFilter(linearFilter);
        ((RenderTargetExtension) this).vivecraft$setMipmaps(mipmaps);

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

                this.colorTexture.setAddressMode(AddressMode.CLAMP_TO_EDGE);
                this.setFilterMode(linearFilter ? FilterMode.LINEAR : FilterMode.NEAREST);
            } else {
                throw new IllegalStateException("Only Opengl is currently supported by Vivecraft");
            }
        } else {
            this.resize(width, height);
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
}
