package org.vivecraft.mixin.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.gui.VivecraftClickEvent;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    /**
     * handles {@link VivecraftClickEvent}
     */
    @Inject(method = "handleComponentClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ClickEvent;getAction()Lnet/minecraft/network/chat/ClickEvent$Action;", ordinal = 0), cancellable = true)
    private void vivecraft$handleVivecraftClickEvents(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style.getClickEvent() instanceof VivecraftClickEvent viveEvent) {
            VivecraftClickEvent.VivecraftAction action = viveEvent.getVivecraftAction();
            if (action == VivecraftClickEvent.VivecraftAction.OPEN_SCREEN) {
                Minecraft.getInstance().setScreen((Screen) viveEvent.getVivecraftValue());
                cir.setReturnValue(true);
            }
        }
    }
}
