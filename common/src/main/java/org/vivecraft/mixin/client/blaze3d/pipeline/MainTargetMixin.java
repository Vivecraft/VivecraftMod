package org.vivecraft.mixin.client.blaze3d.pipeline;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.pipeline.MainTarget;

@Mixin(MainTarget.class)
public abstract class MainTargetMixin {


    @Shadow
    @Final
    @Mutable
    static MainTarget.Dimension DEFAULT_DIMENSIONS = new MainTarget.Dimension(1280, 720);
}
