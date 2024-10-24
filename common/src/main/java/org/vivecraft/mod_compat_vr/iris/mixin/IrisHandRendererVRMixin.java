package org.vivecraft.mod_compat_vr.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.pipeline.HandRenderer",
    "net.irisshaders.iris.pathways.HandRenderer"
})
public class IrisHandRendererVRMixin {

    @Inject(method = "setupGlState", at = @At("HEAD"), cancellable = true, remap = false)
    private void vivecraft$noViewBobbingInVR(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSolid", at = @At("HEAD"), cancellable = true, remap = false)
    private void vivecraft$NoHandSolid(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTranslucent", at = @At("HEAD"), cancellable = true, remap = false)
    private void vivecraft$NoHandTranslucent(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }
}
