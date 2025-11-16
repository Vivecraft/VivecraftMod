package org.vivecraft.mixin.client_vr.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.MCVR;

@Mixin(RenderSystem.class)
public class RenderSystemVRMixin {
    @Inject(method = "limitDisplayFPS", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vivecraft$noFPSlimit(CallbackInfo ci) {
        if (VRState.VR_RUNNING && !MCVR.get().capFPS()) {
            ci.cancel();
        }
    }
}
