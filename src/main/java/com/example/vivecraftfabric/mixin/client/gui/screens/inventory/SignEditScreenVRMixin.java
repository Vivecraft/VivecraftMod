package com.example.vivecraftfabric.mixin.client.gui.screens.inventory;

import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;

@Mixin(SignEditScreen.class)
public class SignEditScreenVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;setEditable(Z)V", shift = At.Shift.AFTER), method = "init")
    public void showOverlay(CallbackInfo ci) {
        KeyboardHandler.setOverlayShowing(true);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;setEditable(Z)V", shift = At.Shift.AFTER), method = "removed")
    public void dontShowOverlay(CallbackInfo ci) {
        KeyboardHandler.setOverlayShowing(false);
    }
}
