package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(DeltaTracker.Timer.class)
public class DeltaTrackerTimerVRMixin {
    @ModifyReturnValue(method = "getGameTimeDeltaPartialTick", at = @At("RETURN"))
    private float vivecraft$partialTickOverride(float original) {
        if (VRState.VR_RUNNING && ClientDataHolderVR.getInstance().partialTickOverride != null) {
            return ClientDataHolderVR.getInstance().partialTickOverride;
        } else {
            return original;
        }
    }
}
