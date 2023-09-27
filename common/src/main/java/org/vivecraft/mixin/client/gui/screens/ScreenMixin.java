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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ClickEvent;getAction()Lnet/minecraft/network/chat/ClickEvent$Action;", ordinal = 0), method = "handleComponentClicked(Lnet/minecraft/network/chat/Style;)Z", cancellable = true)
    public void vivecraft$handleVivecraftClickEvents(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style.getClickEvent() instanceof VivecraftClickEvent) {
            VivecraftClickEvent.VivecraftAction action = ((VivecraftClickEvent) style.getClickEvent()).getVivecraftAction();
            if (action == VivecraftClickEvent.VivecraftAction.OPEN_SCREEN) {
                Minecraft.getInstance().setScreen((Screen) ((VivecraftClickEvent) style.getClickEvent()).getVivecraftValue());
                cir.setReturnValue(true);
            }
        }
    }
}
