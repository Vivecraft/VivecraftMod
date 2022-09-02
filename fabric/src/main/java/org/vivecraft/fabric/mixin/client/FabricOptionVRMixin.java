package org.vivecraft.fabric.mixin.client;

import org.vivecraft.ClientDataHolder;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Option.class)
public abstract class FabricOptionVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V", remap = true), method = "method_32552", remap = false)
    private static void reinit(Options options, Option option, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }

}
