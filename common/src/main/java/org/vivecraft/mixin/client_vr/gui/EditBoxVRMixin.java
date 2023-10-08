package org.vivecraft.mixin.client_vr.gui;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

import static org.vivecraft.client_vr.VRState.vrRunning;

@Mixin(net.minecraft.client.gui.components.EditBox.class)
public abstract class EditBoxVRMixin extends net.minecraft.client.gui.components.AbstractWidget {

    public EditBoxVRMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Inject(at = @At("HEAD"), method = "onClick")
    public void vivecraft$openKeyboard(double d, double e, CallbackInfo ci) {
        if (vrRunning) {
            KeyboardHandler.setOverlayShowing(true);
        }
    }
}
