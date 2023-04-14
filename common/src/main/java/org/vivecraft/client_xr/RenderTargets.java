package org.vivecraft.client_xr;

import com.mojang.blaze3d.pipeline.MainTarget;

public class RenderTargets {

    public static RenderTargets INSTANCE;

    public final MainTarget vanillaRenderTarget;

    public static WorldRenderPass wrp;

    public RenderTargets(MainTarget vanillaRenderTarget) {
        this.vanillaRenderTarget = vanillaRenderTarget;
    }
}
