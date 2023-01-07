package org.vivecraft.titleworlds.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {

    @Redirect(method = "startConnecting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;clearLevel()V"))
    private static void cancelEarlyClearLevel(Minecraft instance) {

    }
}
