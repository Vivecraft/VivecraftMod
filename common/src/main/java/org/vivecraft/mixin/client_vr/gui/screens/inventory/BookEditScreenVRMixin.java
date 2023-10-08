package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

import static org.vivecraft.client_vr.VRState.vrRunning;

@Mixin(BookEditScreen.class)
public class BookEditScreenVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;updateButtonVisibility()V", shift = Shift.BEFORE), method = "init")
    public void vivecraft$overlay(CallbackInfo ci) {
        if (vrRunning) {
            KeyboardHandler.setOverlayShowing(true);
        }
    }
}
