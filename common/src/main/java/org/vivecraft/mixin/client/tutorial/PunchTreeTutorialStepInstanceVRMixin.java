package org.vivecraft.mixin.client.tutorial;

import net.minecraft.client.tutorial.PunchTreeTutorialStepInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.ClientDataHolder;

@Mixin(PunchTreeTutorialStepInstance.class)
public class PunchTreeTutorialStepInstanceVRMixin {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V"), index = 2, method = "tick")
    private Component alterDescription(Component component) {
        if (!ClientDataHolder.getInstance().vrSettings.seated) {
            return Component.translatable("tutorial.find_tree.description");
        }
        return component;
    }
}
