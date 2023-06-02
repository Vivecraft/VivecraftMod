package org.vivecraft.mod_compat_vr.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(targets = {
        "net.coderbot.iris.uniforms.SystemTimeUniforms$FrameCounter",
        "net.coderbot.iris.uniforms.SystemTimeUniforms$Timer"
})
public class IrisBeginFrameHack {
    // only count frames from the first RenderPass, so that all RenderPasses have the same frame counter
    // only update the timer on the first RenderPass, so that it counts the time from all RenderPasses
    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelShadows(CallbackInfo ci) {
        if (!RenderPassType.isVanilla() && !ClientDataHolderVR.getInstance().isFirstPass) {
            ci.cancel();
        }
    }
}
