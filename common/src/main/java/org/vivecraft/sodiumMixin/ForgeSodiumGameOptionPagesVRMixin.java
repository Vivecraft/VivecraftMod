package org.vivecraft.sodiumMixin;

import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.misc.GraphicsMode;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;

@Pseudo
@Mixin(SodiumGameOptionPages.class)
public class ForgeSodiumGameOptionPagesVRMixin {

    @Inject(at = @At("HEAD"), method = "lambda$quality$23", remap = false)
    private static void initframe(Options opts, GraphicsMode value, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }
}
