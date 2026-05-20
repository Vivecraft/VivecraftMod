package org.vivecraft.mod_compat_vr.emf.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Pseudo
@Mixin(targets = {
    "traben.entity_model_features.models.parts.EMFModelPartWithState",
    "traben.entity_model_features.models.EMFModelPartWithState"
})
public class EMFModelPartWithStateVRMixin {
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V"))
    private boolean vivecraft$noRenderEventForFirstPerson(Runnable instance) {
        return !VRState.VR_RUNNING || !ClientDataHolderVR.isFpHand.get();
    }
}
