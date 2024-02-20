package org.vivecraft.mod_compat_vr.rei.mixin;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

@Pseudo
@Mixin(targets = {"me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget"})
public abstract class reiTextFieldWidgetMixin implements ContainerEventHandler {

    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void vivecraft$openKeyboard(boolean focused, CallbackInfo ci) {
        if (VRState.vrRunning && !ClientDataHolderVR.getInstance().vrSettings.seated && (focused || ClientDataHolderVR.getInstance().vrSettings.autoCloseKeyboard)) {
            KeyboardHandler.setOverlayShowing(focused);
        }
    }
}
