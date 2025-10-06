package org.vivecraft.mod_compat_vr.rei.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.OpenKeyboardContext;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

@Pseudo
@Mixin(targets = {"me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget"})
public abstract class reiTextFieldWidgetMixin {

    @Inject(method = {"setFocused", "method_25365", "m_93692_"}, at = @At("HEAD"), remap = false)
    private void vivecraft$openKeyboard(boolean focused, CallbackInfo ci) {
        if (VRState.VR_RUNNING && focused) {
            KeyboardHandler.showOverlay(OpenKeyboardContext.FORCE);
        }
    }
}
