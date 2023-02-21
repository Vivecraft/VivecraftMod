package org.vivecraft.mixin.client.tutorial;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.provider.MCVR;

import java.util.HashSet;
import java.util.Set;

@Mixin(MovementTutorialStepInstance.class)
public class MovementTutorialStepInstanceVRMixin {

    @Shadow private int moveCompleted;

    @Shadow private boolean moved;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 0), index = 1, method = "tick")
    private Component alterMovementTitle(Component title) {
        if (!ClientDataHolder.getInstance().vrSettings.seated) {
            String moveString = I18n.get("tutorial.move.title", "%s", "", "", "");
            moveString = moveString.substring(0, moveString.indexOf(","));

            // find the currently used movement binding
            if (MCVR.get().getInputAction(MCVR.get().keyFreeMoveStrafe).isActive()) {
                // moveStrafe active
                return new TextComponent(moveString.formatted("§l" + MCVR.get().getOriginName(MCVR.get().getInputAction(MCVR.get().keyFreeMoveStrafe).getLastOrigin()) + "§r"));
            } else if (MCVR.get().getInputAction(MCVR.get().keyFreeMoveRotate).isActive()) {
                // moveRotate active
                return new TextComponent(moveString.formatted("§l" + MCVR.get().getOriginName(MCVR.get().getInputAction(MCVR.get().keyFreeMoveRotate).getLastOrigin()) + "§r"));
            } else if (MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).isActive() ||
                    MCVR.get().getInputAction(Minecraft.getInstance().options.keyDown).isActive() ||
                    MCVR.get().getInputAction(Minecraft.getInstance().options.keyLeft).isActive() ||
                    MCVR.get().getInputAction(Minecraft.getInstance().options.keyRight).isActive()
            ) {
                // individual movement bindings
                Set<String> buttons = new HashSet<>();
                buttons.add(MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).isActive() ? MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).getLastOrigin()) : "");
                buttons.add(MCVR.get().getInputAction(Minecraft.getInstance().options.keyDown).isActive() ? MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).getLastOrigin()) : "");
                buttons.add(MCVR.get().getInputAction(Minecraft.getInstance().options.keyLeft).isActive() ? MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).getLastOrigin()) : "");
                buttons.add(MCVR.get().getInputAction(Minecraft.getInstance().options.keyRight).isActive() ? MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).getLastOrigin()) : "");

                StringBuilder buttonsString = new StringBuilder();
                for (String s : buttons) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    buttonsString.append(buttonsString.isEmpty() ? "§l" + s + "§r" : ", " + "§l" + s + "§r");
                }
                return new TextComponent(moveString.formatted(buttonsString.toString()));
            } else if (MCVR.get().getInputAction(MCVR.get().keyTeleportFallback).isActive()) {
                // teleport fallback
                return new TextComponent(moveString.formatted("§l" + MCVR.get().getOriginName(MCVR.get().getInputAction(MCVR.get().keyTeleportFallback).getLastOrigin()) + "§r"));
            } else if (MCVR.get().getInputAction(MCVR.get().keyTeleport).isActive()) {
                // teleport
                return new TranslatableComponent("vivecraft.toasts.teleport", new TextComponent(MCVR.get().getOriginName(MCVR.get().getInputAction(MCVR.get().keyTeleport).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            }
        }
        return title;
    }
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 0), index = 2, method = "tick")
    private Component alterMovementDescription(Component description) {
        if (!ClientDataHolder.getInstance().vrSettings.seated && MCVR.get().getInputAction(Minecraft.getInstance().options.keyJump).isActive()) {
            return new TranslatableComponent("tutorial.move.description", new TextComponent(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyJump).getLastOrigin())).withStyle(ChatFormatting.BOLD));
        }
        return description;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 1), index = 2, method = "tick")
    private Component alterLookDescription(Component title) {
        if (!ClientDataHolder.getInstance().vrSettings.seated) {
            return new TranslatableComponent("vivecraft.toasts.point_controller", new TranslatableComponent(ClientDataHolder.getInstance().vrSettings.reverseHands ? "vivecraft.toasts.point_controller.left" : "vivecraft.toasts.point_controller.right").withStyle(ChatFormatting.BOLD));
        }
        return title;
    }

    @Inject(at = @At("TAIL"), method = "onInput")
    private void addTeleport(Input input, CallbackInfo ci) {
        moved |= MCVR.get().keyTeleport.isDown;
    }

    @Inject(at = @At("HEAD"), method = "onMouse", cancellable = true)
    private void onlyAfterMove(double d, double e, CallbackInfo ci) {
        if (moveCompleted == -1) {
            ci.cancel();
        }
    }
}
