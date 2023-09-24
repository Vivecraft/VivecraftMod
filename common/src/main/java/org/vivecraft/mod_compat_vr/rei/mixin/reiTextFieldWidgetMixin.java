package org.vivecraft.mod_compat_vr.rei.mixin;

import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {"me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget"})
public class reiTextFieldWidgetMixin {

    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void openKeyboard(boolean focused, CallbackInfo ci) {
        if (vrRunning && !dh.vrSettings.seated) {
            KeyboardHandler.setOverlayShowing(focused);
        }
    }
}
