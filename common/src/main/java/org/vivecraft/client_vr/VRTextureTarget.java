package org.vivecraft.client_vr;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;

public class VRTextureTarget extends RenderTarget {

    private final String name;

    public VRTextureTarget(String name, int width, int height, boolean usedepth, boolean onMac, int texid, boolean depthtex, boolean linearFilter, boolean useStencil) {
        super(usedepth);
        this.name = name;
        RenderSystem.assertOnGameThreadOrInit();
        ((RenderTargetExtension) this).vivecraft$setTextid(texid);
        ((RenderTargetExtension) this).vivecraft$isLinearFilter(linearFilter);
        ((RenderTargetExtension) this).vivecraft$setUseStencil(useStencil);
        this.resize(width, height, onMac);
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
