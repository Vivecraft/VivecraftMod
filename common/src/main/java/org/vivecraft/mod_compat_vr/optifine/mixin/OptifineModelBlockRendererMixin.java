package org.vivecraft.mod_compat_vr.optifine.mixin;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

@Mixin(ModelBlockRenderer.class)
public class OptifineModelBlockRendererMixin {
    @Inject(at = @At(value = "HEAD"), method = "isSeparateAoLightValue()Z", remap = false, cancellable = true)
    private static void vivecraft$optifineNoSeparateAO(CallbackInfoReturnable<Boolean> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "fixAoLightValue(F)F", remap = false, cancellable = true)
    private static void vivecraft$optifineNoAOoverride(float ao, CallbackInfoReturnable<Float> cir) {
        if (ao == 0.2F && ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            cir.setReturnValue(1.0F - (float) OptifineHelper.getAoLevel() * 0.8F);
        }
    }
}
