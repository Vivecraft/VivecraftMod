package org.vivecraft.client_vr.extensions;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.world.entity.Entity;

public interface LevelRendererExtension {
    Entity vivecraft$getRenderedEntity();

    RenderTarget vivecraft$getAlphaSortVROccludedFramebuffer();

    RenderTarget vivecraft$getAlphaSortVRUnoccludedFramebuffer();

    RenderTarget vivecraft$getAlphaSortVRHandsFramebuffer();

    void vivecraft$restoreVanillaPostChains();
}
