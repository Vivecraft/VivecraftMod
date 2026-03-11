package org.vivecraft.mod_compat_vr.elementa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.OpenKeyboardContext;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

import java.util.List;

/**
 * Fallback compatibility for Elementa: when any component calls grabWindowFocus(),
 * open the VR keyboard if it is a text input. This runs on the exact call path
 * used by Resourcify's search box (searchBox.grabWindowFocus() on click).
 * Resourcify relocates Elementa to dev.dediamondpro.resourcify.libs.elementa.
 */
@Pseudo
@Mixin(targets = {
    // base elementa
    "gg.essential.elementa.UIComponent",
    // resourcify
    "dev.dediamondpro.resourcify.libs.elementa.UIComponent"
})
public class ElementaUIComponentVRMixin {

    @Unique
    private static final List<String> vivecraft$textInputs = List.of(
        // base elementa class names
        "gg.essential.elementa.components.input.UITextInput",
        "gg.essential.elementa.components.input.AbstractTextInput",
        // resourcify repackage
        "dev.dediamondpro.resourcify.libs.elementa.components.input.UITextInput",
        "dev.dediamondpro.resourcify.libs.elementa.components.input.AbstractTextInput",
        // essential repackage
        "gg.essential.gui.common.input.UITextInput",
        "gg.essential.gui.common.input.AbstractTextInput"
    );

    @Inject(method = "grabWindowFocus", at = @At("HEAD"), remap = false)
    private void vivecraft$openKeyboardWhenTextInputGrabsFocus(CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$textInputs.contains(this.getClass().getName())) {
            KeyboardHandler.showOverlay(OpenKeyboardContext.FORCE);
        }
    }
}
