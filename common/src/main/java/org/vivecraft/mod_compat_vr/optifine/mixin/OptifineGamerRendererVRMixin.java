package org.vivecraft.mod_compat_vr.optifine.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;

@Mixin(GameRenderer.class)
public class OptifineGamerRendererVRMixin {
    /**
     * menuworld fix
     */
    @Inject(method = "setFxaaShader(I)Z", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void vivecraft$shutdownFXAA(int fxaaLevel, CallbackInfoReturnable<Boolean> cir) {
        if (VRState.vrRunning) {
            // don't create FXAA pass when VR is running, it messes with the recreation detection
            cir.setReturnValue(true);
        }
    }
}
