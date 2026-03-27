package org.vivecraft.mixin.client_vr;

import net.minecraft.client.FramerateLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.MCVR;

@Mixin(FramerateLimiter.class)
public class FramerateLimiterVRMixin {
    @Inject(method = "limitDisplayFPS", at = @At("HEAD"), cancellable = true)
    private static void vivecraft$noFPSlimit(CallbackInfo ci) {
        if (VRState.VR_RUNNING && !MCVR.get().capFPS()) {
            ci.cancel();
        }
    }
}
