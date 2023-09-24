package org.vivecraft.mixin.client_vr.blaze3d.audio;

import org.slf4j.Logger;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(com.mojang.blaze3d.audio.Library.class)
public class LibraryVRMixin {
    @Shadow
    @Final
    static Logger LOGGER;

    @ModifyVariable(method = "init", at = @At("HEAD"), argsOnly = true)
    private boolean shouldDoHRTF(boolean vanillaHRTF) {
        if (vrRunning) {
            // don't force HRTF in nonvr
            LOGGER.info("enabling HRTF: {}", dh.vrSettings.hrtfSelection >= 0);
            return dh.vrSettings.hrtfSelection >= 0;
        }
        return vanillaHRTF;
    }
}
