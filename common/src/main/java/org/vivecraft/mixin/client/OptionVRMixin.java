package org.vivecraft.mixin.client;

import org.spongepowered.asm.mixin.injection.Group;
import org.vivecraft.ClientDataHolder;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Option.class)
public abstract class OptionVRMixin {

    @Group(name = "reinitFrameBuffers", min = 1, max = 1)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V", remap = true), method = "method_32552", remap = false, expect = 0)
    private static void reinitFabric(Options options, Option option, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }

    @Group(name = "reinitFrameBuffers", min = 1, max = 1)
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V",remap = true), method = "m_193661_", remap = false, expect = 0)
    private static void reinitForge(Options options, Option option, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }

}
