package org.vivecraft.forge.mixin;

import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeIngameGui.class)
public abstract class ForgeIngameGuiVRMixin {

    @Inject(at = @At("HEAD"), method = "lambda$static$0", remap = false, cancellable = true)
    private static void noVignette(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "lambda$static$1", remap = false, cancellable = true)
    private static void noSpyglass(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "lambda$static$2", remap = false, cancellable = true)
    private static void noHelmet(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "lambda$static$3", remap = false, cancellable = true)
    private static void noFreeze(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "lambda$static$4", remap = false, cancellable = true)
    private static void noPortal(CallbackInfo ci) {
        ci.cancel();
    }
}
