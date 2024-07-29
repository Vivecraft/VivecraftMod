package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;

/**
 * extension of a regular RenderTarget that sets Vivecraft features on creation
 */
public class VRTextureTarget extends RenderTarget {

    private final String name;

    public VRTextureTarget(String name, int width, int height, boolean useDepth, int texId, boolean linearFilter, boolean mipmaps, boolean useStencil) {
        super(useDepth);
        this.name = name;
        RenderSystem.assertOnGameThreadOrInit();
        ((RenderTargetExtension) this).vivecraft$setTextId(texId);
        ((RenderTargetExtension) this).vivecraft$setLinearFilter(linearFilter);
        ((RenderTargetExtension) this).vivecraft$setMipmaps(mipmaps);
        ((RenderTargetExtension) this).vivecraft$setStencil(useStencil);
        this.resize(width, height, Minecraft.ON_OSX);
        if (useStencil) {
            Xplat.enableRenderTargetStencil(this);
        }
        this.setClearColor(0, 0, 0, 0);
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
