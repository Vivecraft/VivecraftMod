package com.example.examplemod.mixin.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.MainTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MainTarget.class)
public abstract class MainTargetMixin {

    @Final
    @Shadow
    static int DEFAULT_WIDTH = 1280;

    @Final
    @Shadow
    static int DEFAULT_HEIGHT = 720;

    @Shadow
    static final MainTarget.Dimension DEFAULT_DIMENSIONS = new MainTarget.Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
}
