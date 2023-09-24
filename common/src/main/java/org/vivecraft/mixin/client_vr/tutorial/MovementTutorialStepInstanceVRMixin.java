package org.vivecraft.mixin.client_vr.tutorial;

import org.vivecraft.client.VivecraftVRMod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.Input;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

import static org.vivecraft.client_vr.VRState.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementTutorialStepInstance.class)
public class MovementTutorialStepInstanceVRMixin {

    @Shadow private int moveCompleted;

    @Shadow private boolean moved;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 0), index = 1, method = "tick")
    private Component alterMovementTitle(Component title) {
        if (!vrRunning) {
            return title;
        }

        if (!dh.vrSettings.seated) {
            // find the currently used movement binding
            if (dh.vr.getInputAction(VivecraftVRMod.keyFreeMoveStrafe).isActive()) {
                // moveStrafe active
                return Component.translatable("vivecraft.toasts.move1", Component.literal(dh.vr.getOriginName(dh.vr.getInputAction(VivecraftVRMod.keyFreeMoveStrafe).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            } else if (dh.vr.getInputAction(VivecraftVRMod.keyFreeMoveRotate).isActive()) {
                // moveRotate active
                return Component.translatable("vivecraft.toasts.move1", Component.literal(dh.vr.getOriginName(dh.vr.getInputAction(VivecraftVRMod.keyFreeMoveRotate).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            } else if (dh.vr.getInputAction(mc.options.keyUp).isActive() ||
                    dh.vr.getInputAction(mc.options.keyDown).isActive() ||
                    dh.vr.getInputAction(mc.options.keyLeft).isActive() ||
                    dh.vr.getInputAction(mc.options.keyRight).isActive()
            ) {
                // individual movement bindings
                Set<String> buttons = new HashSet<>();
                if (dh.vr.getInputAction(mc.options.keyUp).isActive()) {
                    buttons.add(dh.vr.getOriginName(dh.vr.getInputAction(mc.options.keyUp).getLastOrigin()));
                }
                if (dh.vr.getInputAction(mc.options.keyDown).isActive()) {
                    buttons.add(dh.vr.getOriginName(dh.vr.getInputAction(mc.options.keyDown).getLastOrigin()));
                }
                if (dh.vr.getInputAction(mc.options.keyLeft).isActive()) {
                    buttons.add(dh.vr.getOriginName(dh.vr.getInputAction(mc.options.keyLeft).getLastOrigin()));
                }
                if (dh.vr.getInputAction(mc.options.keyRight).isActive()) {
                    buttons.add(dh.vr.getOriginName(dh.vr.getInputAction(mc.options.keyRight).getLastOrigin()));
                }

                String[] stringArray = buttons.toArray(new String[0]);
                return switch (buttons.size()) {
                    case 1 -> Component.translatable(
                            "vivecraft.toasts.move1",
                            Component.literal(stringArray[0]).withStyle(ChatFormatting.BOLD)
                    );
                    case 2 -> Component.translatable(
                            "vivecraft.toasts.move2",
                            Component.literal(stringArray[0]).withStyle(ChatFormatting.BOLD),
                            Component.literal(stringArray[1]).withStyle(ChatFormatting.BOLD)
                    );
                    case 3 -> Component.translatable(
                            "vivecraft.toasts.move3",
                            Component.literal(stringArray[0]).withStyle(ChatFormatting.BOLD),
                            Component.literal(stringArray[1]).withStyle(ChatFormatting.BOLD),
                            Component.literal(stringArray[2]).withStyle(ChatFormatting.BOLD)
                    );
                    case 4 -> Component.translatable(
                            "vivecraft.toasts.move4",
                            Component.literal(stringArray[0]).withStyle(ChatFormatting.BOLD),
                            Component.literal(stringArray[1]).withStyle(ChatFormatting.BOLD),
                            Component.literal(stringArray[2]).withStyle(ChatFormatting.BOLD),
                            Component.literal(stringArray[3]).withStyle(ChatFormatting.BOLD)
                    );
                    default -> Component.literal("");
                };
            } else if (dh.vr.getInputAction(VivecraftVRMod.keyTeleportFallback).isActive()) {
                // teleport fallback
                return Component.translatable("vivecraft.toasts.move1", Component.literal(dh.vr.getOriginName(dh.vr.getInputAction(VivecraftVRMod.keyTeleportFallback).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            } else if (dh.vr.getInputAction(VivecraftVRMod.keyTeleport).isActive()) {
                // teleport
                return Component.translatable("vivecraft.toasts.teleport", Component.literal(dh.vr.getOriginName(dh.vr.getInputAction(VivecraftVRMod.keyTeleport).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            }
        }
        return title;
    }
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 0), index = 2, method = "tick")
    private Component alterMovementDescription(Component description) {
        if (!vrRunning) {
            return description;
        }

        if (!dh.vrSettings.seated && dh.vr.getInputAction(mc.options.keyJump).isActive()) {
            return Component.translatable("tutorial.move.description", Component.literal(dh.vr.getOriginName(dh.vr.getInputAction(mc.options.keyJump).getLastOrigin())).withStyle(ChatFormatting.BOLD));
        }
        return description;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 1), index = 2, method = "tick")
    private Component alterLookDescription(Component title) {
        return (!vrRunning || dh.vrSettings.seated ?
            title :
            Component.translatable(
                "vivecraft.toasts.point_controller",
                Component.translatable(dh.vrSettings.reverseHands ?
                    "vivecraft.toasts.point_controller.left" :
                    "vivecraft.toasts.point_controller.right"
                ).withStyle(ChatFormatting.BOLD)
            )
        );
    }

    @Inject(at = @At("TAIL"), method = "onInput")
    private void addTeleport(Input input, CallbackInfo ci) {
        this.moved |= VivecraftVRMod.keyTeleport.isDown();
    }

    @Inject(at = @At("HEAD"), method = "onMouse", cancellable = true)
    private void onlyAfterMove(double d, double e, CallbackInfo ci) {
        if (this.moveCompleted == -1) {
            ci.cancel();
        }
    }
}
