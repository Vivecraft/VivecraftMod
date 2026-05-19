package org.vivecraft.mod_compat_vr.elementa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.OpenKeyboardContext;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

/**
 * Compatibility patch for Elementa (used by Essential, Resourcify and other mods).
 * When an Elementa text input becomes active (e.g. search field focused), opens the VR keyboard.
 * Resourcify relocates Elementa to dev.dediamondpro.resourcify.libs.elementa.
 */
@Pseudo
@Mixin(targets = {
    // base elementa
    "gg.essential.elementa.components.input.AbstractTextInput",
    // resourcify
    "dev.dediamondpro.resourcify.libs.elementa.components.input.AbstractTextInput",
    // essential
    "gg.essential.gui.common.input.AbstractTextInput"
})
public class ElementaAbstractTextInputVRMixin {

    @Inject(method = "setActive", at = @At("HEAD"), remap = false)
    private void vivecraft$openKeyboardWhenActive(boolean isActive, CallbackInfo ci) {
        if (VRState.VR_RUNNING && isActive) {
            KeyboardHandler.showOverlay(OpenKeyboardContext.FORCE);
        }
    }
}
