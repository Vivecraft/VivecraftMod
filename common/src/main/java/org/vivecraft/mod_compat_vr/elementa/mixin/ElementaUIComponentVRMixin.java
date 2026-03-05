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
 * Fallback compatibility for Elementa: when any component calls grabWindowFocus(),
 * open the VR keyboard if it is a text input. This runs on the exact call path
 * used by Resourcify's search box (searchBox.grabWindowFocus() on click).
 * Resourcify relocates Elementa to dev.dediamondpro.resourcify.libs.elementa.
 */
@Pseudo
@Mixin(targets = {
    "gg.essential.elementa.UIComponent",
    "dev.dediamondpro.resourcify.libs.elementa.UIComponent"
})
public abstract class ElementaUIComponentVRMixin {

    @Inject(method = "grabWindowFocus", at = @At("HEAD"), remap = false)
    private void vivecraft$openKeyboardWhenTextInputGrabsFocus(CallbackInfo ci) {
        if (VRState.VR_RUNNING && isTextInput(this)) {
            KeyboardHandler.showOverlay(OpenKeyboardContext.FORCE);
        }
    }

    private static boolean isTextInput(Object component) {
        if (component == null) return false;
        String name = component.getClass().getName();
        return "gg.essential.elementa.components.input.UITextInput".equals(name)
            || "gg.essential.elementa.components.input.AbstractTextInput".equals(name)
            || "dev.dediamondpro.resourcify.libs.elementa.components.input.UITextInput".equals(name)
            || "dev.dediamondpro.resourcify.libs.elementa.components.input.AbstractTextInput".equals(name);
    }
}
