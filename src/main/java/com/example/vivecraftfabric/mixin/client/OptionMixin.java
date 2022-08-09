package com.example.vivecraftfabric.mixin.client;

import com.example.vivecraftfabric.DataHolder;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Mixin(Option.class)
public abstract class OptionMixin {

    @Final
    @Shadow
    private static Component GRAPHICS_TOOLTIP_FAST;

    @Final
    @Shadow
    private static Component GRAPHICS_TOOLTIP_FANCY;

    @Final
    @Shadow
    private static Component GRAPHICS_TOOLTIP_FABULOUS;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V"), method = "method_32552", remap = false)
    private static void reinit(Options options, Option option, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        DataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }

}
