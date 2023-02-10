package org.vivecraft.mixin.client.tutorial;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.tutorial.OpenInventoryTutorialStep;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.provider.MCVR;

@Mixin(OpenInventoryTutorialStep.class)
public class OpenInventoryTutorialStepVRMixin {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V"), index = 2, method = "tick")
    private Component alterDescription(Component component) {
        if (!ClientDataHolder.getInstance().vrSettings.seated && MCVR.get().getInputAction(Minecraft.getInstance().options.keyInventory).isActive()) {
            return Component.translatable("tutorial.open_inventory.description", Component.literal(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyInventory).getLastOrigin())).withStyle(ChatFormatting.BOLD));
        }
        return component;
    }
}
