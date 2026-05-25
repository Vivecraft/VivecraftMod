package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ModelFeatureRendererSubmitExtension;

@Mixin(ModelFeatureRenderer.Submit.class)
public class ModelFeatureRendererSubmitVRMixin implements ModelFeatureRendererSubmitExtension {

    @Unique
    private boolean vivecraft$isFpHand;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$storeFirstPerson(CallbackInfo ci) {
        this.vivecraft$isFpHand = VRState.VR_RUNNING && ClientDataHolderVR.isFpHand.get();
    }

    @Override
    public boolean vivecraft$isFirstPerson() {
        return this.vivecraft$isFpHand;
    }
}
