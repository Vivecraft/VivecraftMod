package org.vivecraft.mod_compat_vr.dynamicfps.mixin;

import dynamic_fps.impl.DynamicFPSMod;
import dynamic_fps.impl.PowerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;

@Pseudo
@Mixin(DynamicFPSMod.class)
public class DynamicFPSModVRMixin {
    @Inject(at = @At("HEAD"), method = {"checkForRender()Z", "shouldShowLevels()Z"}, remap = false, cancellable = true)
    private static void vivecraft$alwaysRenderVR(CallbackInfoReturnable<Boolean> cir) {
        if (VRState.vrRunning) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(at = @At(value = "LOAD", target = "Lorg/vivecraft/mod_compat_vr/dynamicfps/mixin/DynamicFPSModVRMixin;state:Ldynamic_fps/impl/PowerState;"), method = "checkForStateChanges()V", remap = false)
    private static PowerState vivecraft$alwaysFocused(PowerState value) {
        // always focused in VR
        return VRState.vrRunning ? PowerState.FOCUSED : value;
    }
}
