package org.vivecraft.mixin.client_vr.blaze3d.audio;

import com.mojang.blaze3d.audio.Library;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(Library.class)
public class LibraryVRMixin {
    @Shadow
    @Final
    static Logger LOGGER;

    @ModifyVariable(method = "init", at = @At("HEAD"), argsOnly = true)
    private boolean shouldDoHRTF(boolean vanillaHRTF) {
        LOGGER.info("enabling HRTF: {}", ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0);
        return ClientDataHolderVR.getInstance().vrSettings.hrtfSelection >= 0;
    }
}
