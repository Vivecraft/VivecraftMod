package org.vivecraft.mixin.client_vr.blaze3d.audio;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.common.utils.Utils;

@Mixin(com.mojang.blaze3d.audio.Library.class)
public class LibraryVRMixin {
    @ModifyVariable(method = "init", at = @At("HEAD"), argsOnly = true)
    private boolean vivecraft$shouldDoHRTF(boolean vanillaHRTF) {
        if (VRState.vrRunning) {
            // don't force HRTF in nonvr
            Utils.logger.info("enabling HRTF: {}", ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0);
            return ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0;
        }
        return vanillaHRTF;
    }
}
