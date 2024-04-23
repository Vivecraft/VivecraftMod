package org.vivecraft.mixin.client_vr.blaze3d.platform;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.InputSimulator;

@Mixin(InputConstants.class)
public class InputConstantsVRMixin {

    @Inject(at = @At("TAIL"), method = "isKeyDown", cancellable = true)
    private static void vivecraft$keyDown(long window, int key, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || (VRState.vrRunning && InputSimulator.isKeyDown(key)));
    }
}
