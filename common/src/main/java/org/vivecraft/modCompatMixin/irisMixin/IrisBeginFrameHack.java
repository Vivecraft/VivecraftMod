package org.vivecraft.modCompatMixin.irisMixin;

import org.vivecraft.ClientDataHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
        "net.coderbot.iris.uniforms.SystemTimeUniforms$FrameCounter",
        "net.coderbot.iris.uniforms.SystemTimeUniforms$Timer"
})
public class IrisBeginFrameHack {

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelShadows(CallbackInfo ci) {
        if (!ClientDataHolder.getInstance().isFirstPass) {
            ci.cancel();
        }
    }
}
