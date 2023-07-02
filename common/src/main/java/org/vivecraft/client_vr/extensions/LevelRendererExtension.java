package org.vivecraft.client_vr.extensions;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.world.entity.Entity;

public interface LevelRendererExtension {
    Entity getRenderedEntity();

    RenderTarget getAlphaSortVROccludedFramebuffer();

    RenderTarget getAlphaSortVRUnoccludedFramebuffer();

    RenderTarget getAlphaSortVRHandsFramebuffer();

    void restoreVanillaPostChains();
}
