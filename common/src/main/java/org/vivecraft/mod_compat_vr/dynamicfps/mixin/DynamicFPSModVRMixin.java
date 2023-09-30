package org.vivecraft.mod_compat_vr.dynamicfps.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;

@Pseudo
@Mixin(targets = "dynamicfps.DynamicFPSMod")
public class DynamicFPSModVRMixin {
    @Inject(at = @At("HEAD"), method = "checkForRender()Z", remap = false, cancellable = true)
    private static void vivecraft$alwaysRenderVR(CallbackInfoReturnable<Boolean> cir) {
        if (VRState.vrRunning) {
            cir.setReturnValue(true);
        }
    }
}
