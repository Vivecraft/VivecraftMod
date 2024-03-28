package org.vivecraft.mod_compat_vr.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_xr.render_pass.RenderPassType;

//TODO Move rendering to here
@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.pipeline.HandRenderer",
    "net.irisshaders.iris.pathways.HandRenderer"
})
public class IrisHandRendererVRMixin {

    @Inject(at = @At("HEAD"), method = "setupGlState", cancellable = true, remap = false)
    public void vivecraft$glState(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSolid", cancellable = true, remap = false)
    public void vivecraft$rendersolid(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderTranslucent", cancellable = true, remap = false)
    public void vivecraft$rendertranslucent(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }
}
