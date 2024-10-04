package org.vivecraft.mod_compat_vr.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.gl.IrisRenderSystem",
    "net.irisshaders.iris.gl.IrisRenderSystem"
})
public class IrisRenderSystemVRMixin {
    // expect 0 since this is only for iris 1.6+
    // disable SSBOS since I didn't get to find a good way to share them between passes yet
    @Inject(method = "supportsSSBO", at = @At("HEAD"), remap = false, cancellable = true, expect = 0)
    private static void vivecraft$noSSBOInVR(CallbackInfoReturnable<Boolean> cir) {
        if (!RenderPassType.isVanilla()) {
            cir.setReturnValue(false);
        }
    }
}
