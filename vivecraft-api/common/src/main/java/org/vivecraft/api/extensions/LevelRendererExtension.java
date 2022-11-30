package org.vivecraft.api.extensions;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.world.entity.Entity;

public interface LevelRendererExtension {
    Entity getRenderedEntity();

    RenderTarget getAlphaSortVROccludedFramebuffer();

    RenderTarget getAlphaSortVRUnoccludedFramebuffer();

    RenderTarget getAlphaSortVRHandsFramebuffer();
}
