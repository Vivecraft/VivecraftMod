package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

@Mixin(SignEditScreen.class)
public class SignEditScreenVRMixin {

    @Inject(at = @At("HEAD"), method = "init")
    public void showOverlay(CallbackInfo ci) {
        if(VRState.vrRunning) {
            KeyboardHandler.setOverlayShowing(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "removed")
    public void dontShowOverlay(CallbackInfo ci) {
        if(VRState.vrRunning) {
            KeyboardHandler.setOverlayShowing(false);
    }
    }
}
