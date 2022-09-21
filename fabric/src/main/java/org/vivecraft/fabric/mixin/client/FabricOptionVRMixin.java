package org.vivecraft.fabric.mixin.client;

import org.spongepowered.asm.mixin.Shadow;
import org.vivecraft.ClientDataHolder;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;


@Mixin(Options.class)
public abstract class FabricOptionVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V"), method = "method_42464", remap = false)
    private static void reinit(OptionInstance optionInstance, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }

}
