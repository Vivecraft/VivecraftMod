package org.vivecraft.mod_compat_vr.blur.mixin;

import org.vivecraft.client_vr.VRState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.tterrag.blur.Blur")
public class BlurMixin {
    @Inject(at = @At("HEAD"), method = "getProgress", cancellable = true, remap = false)
    private static void vivecraft$noBlurInVR(CallbackInfoReturnable<Float> cir) {
        if (VRState.vrRunning)
            cir.setReturnValue(0.0F);
    }
}
