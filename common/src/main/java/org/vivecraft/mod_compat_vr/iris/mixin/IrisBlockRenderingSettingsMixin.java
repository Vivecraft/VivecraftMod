package org.vivecraft.mod_compat_vr.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Pseudo
@Mixin(targets = {
    "net.coderbot.iris.block_rendering.BlockRenderingSettings",
    "net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings"
})
public class IrisBlockRenderingSettingsMixin {
    @Inject(at = @At("HEAD"), method = "getAmbientOcclusionLevel", remap = false, cancellable = true)
    private void vivecrat$defaultAOforMenuWorld(CallbackInfoReturnable<Float> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            cir.setReturnValue(1.0F);
        }
    }
}
