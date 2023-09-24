package org.vivecraft.mod_compat_vr.dynamicfps.mixin;

import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class DynamicFPSScreenVRMixin {

    @Inject(at = @At("HEAD"), method = "dynamicfps$rendersBackground", remap = false, cancellable = true, expect = 0)
    public void  noOptimizeVR(CallbackInfoReturnable<Boolean> cir) {
        // please don't stop rendering the world when a screen is open
        if (vrRunning) {
            cir.setReturnValue(false);
        }
    }
}
