package org.vivecraft.mod_compat_vr.emf.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;
import traben.entity_model_features.models.parts.EMFModelPart;

@Pseudo
@ClassDependentMixin("traben.entity_model_features.models.parts.EMFModelPart$Animator")
@Mixin(targets = {
    "traben.entity_model_features.models.parts.EMFModelPartWithState",
    "traben.entity_model_features.models.EMFModelPartWithState"
})
public class EMFModelPartWithStateAnimatorVRMixin {
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Ltraben/entity_model_features/models/parts/EMFModelPart$Animator;run()V"))
    private boolean vivecraft$noAnimationForFirstPerson(EMFModelPart.Animator original) {
        return !VRState.VR_RUNNING || !ClientDataHolderVR.isFpHand.get();
    }
}
