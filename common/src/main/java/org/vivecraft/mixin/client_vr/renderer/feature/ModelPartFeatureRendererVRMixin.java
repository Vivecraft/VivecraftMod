package org.vivecraft.mixin.client_vr.renderer.feature;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ModelPartSubmitExtension;

@Mixin(ModelPartFeatureRenderer.class)
public class ModelPartFeatureRendererVRMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelPartSubmit;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private void vivecraft$setFirstPerson(CallbackInfo ci, @Local SubmitNodeStorage.ModelPartSubmit modelPartSubmit) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.isFpHand.set(
                ((ModelPartSubmitExtension) (Object) modelPartSubmit).vivecraft$isFirstPerson());
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void vivecraft$firstPersonReset(CallbackInfo ci) {
        ClientDataHolderVR.isFpHand.set(false);
    }
}
