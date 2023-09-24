package org.vivecraft.mixin.client_vr.tutorial;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(net.minecraft.client.tutorial.OpenInventoryTutorialStep.class)
public class OpenInventoryTutorialStepVRMixin {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V"), index = 2, method = "tick")
    private Component alterDescription(Component component) {
        if (!vrRunning) {
            return component;
        }
        if (!dh.vrSettings.seated && dh.vr.getInputAction(mc.options.keyInventory).isActive()) {
            return Component.translatable("tutorial.open_inventory.description", Component.literal(dh.vr.getOriginName(dh.vr.getInputAction(mc.options.keyInventory).getLastOrigin())).withStyle(ChatFormatting.BOLD));
        }
        return component;
    }
}
