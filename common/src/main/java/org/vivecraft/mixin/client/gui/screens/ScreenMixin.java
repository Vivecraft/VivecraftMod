package org.vivecraft.mixin.client.gui.screens;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.VivecraftClickEvent;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    /**
     * handles {@link VivecraftClickEvent}
     */
    @Inject(method = "defaultHandleClickEvent", at = @At("HEAD"), cancellable = true)
    private static void vivecraft$handleVivecraftClickEvents(
        CallbackInfo ci, @Local(argsOnly = true) ClickEvent clickEvent, @Local(argsOnly = true) Minecraft minecraft)
    {
        if (clickEvent instanceof VivecraftClickEvent viveEvent) {
            VivecraftClickEvent.VivecraftAction action = viveEvent.getVivecraftAction();
            if (action == VivecraftClickEvent.VivecraftAction.OPEN_SCREEN) {
                minecraft.setScreen((Screen) viveEvent.getVivecraftValue());
            }
            ci.cancel();
        }
    }
}
