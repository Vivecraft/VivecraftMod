package org.vivecraft.mixin.client.gui.screens;

import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.VivecraftClickEvent.VivecraftAction;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;

import static org.vivecraft.client_vr.VRState.mc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ClickEvent;getAction()Lnet/minecraft/network/chat/ClickEvent$Action;", ordinal = 0), method = "handleComponentClicked(Lnet/minecraft/network/chat/Style;)Z", cancellable = true)
    public void handleVivecraftClickEvents(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style.getClickEvent() instanceof VivecraftClickEvent) {
            VivecraftAction action = ((VivecraftClickEvent) style.getClickEvent()).getVivecraftAction();
            if (action == VivecraftAction.OPEN_SCREEN) {
                mc.setScreen((Screen) ((VivecraftClickEvent) style.getClickEvent()).getVivecraftValue());
                cir.setReturnValue(true);
            }
        }
    }

}
