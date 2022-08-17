package org.vivecraft.mixin.client;

import org.vivecraft.DataHolder;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


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
