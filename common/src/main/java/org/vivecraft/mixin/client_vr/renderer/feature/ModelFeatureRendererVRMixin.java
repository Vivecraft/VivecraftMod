package org.vivecraft.mixin.client_vr.renderer.feature;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ModelFeatureRendererSubmitExtension;

@Mixin(ModelFeatureRenderer.class)
public class ModelFeatureRendererVRMixin {
    @Inject(method = "prepareModel", at = @At("HEAD"))
    private void vivecraft$setFirstPerson(
        CallbackInfo ci, @Local(argsOnly = true) ModelFeatureRenderer.Submit<?> modelPartSubmit)
    {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.isFpHand.set(
                ((ModelFeatureRendererSubmitExtension) (Object) modelPartSubmit).vivecraft$isFirstPerson());
        }
    }

    @Inject(method = "prepareModel", at = @At("TAIL"))
    private void vivecraft$firstPersonReset(CallbackInfo ci) {
        ClientDataHolderVR.isFpHand.set(false);
    }
}
