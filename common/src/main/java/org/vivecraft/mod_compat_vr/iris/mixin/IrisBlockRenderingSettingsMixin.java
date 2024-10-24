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
    /**
     * menuworld fix
     */
    @Inject(method = "getAmbientOcclusionLevel", at = @At("HEAD"), remap = false, cancellable = true)
    private void vivecraft$defaultAOForMenuWorld(CallbackInfoReturnable<Float> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            cir.setReturnValue(1.0F);
        }
    }

    /**
     * menuworld fix
     */
    @Inject(method = "shouldUseSeparateAo", at = @At("HEAD"), remap = false, cancellable = true)
    private void vivecraft$noSeparationForMenuWorld(CallbackInfoReturnable<Boolean> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * menuworld fix
     */
    @Inject(method = "shouldDisableDirectionalShading", at = @At("HEAD"), remap = false, cancellable = true)
    private void vivecraft$forceShadingForMenuWorld(CallbackInfoReturnable<Boolean> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            cir.setReturnValue(false);
        }
    }
}
