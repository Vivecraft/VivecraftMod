package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;

import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookEditScreen.class)
public class BookEditScreenVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;updateButtonVisibility()V", shift = Shift.BEFORE), method = "init")
    public void overlay(CallbackInfo ci) {
        if (vrRunning) {
            KeyboardHandler.setOverlayShowing(true);
        }
    }
}
