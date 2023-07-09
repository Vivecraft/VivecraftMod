package org.vivecraft.mixin.client_vr.tutorial;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.MCVR;

import java.util.HashSet;
import java.util.Set;

@Mixin(MovementTutorialStepInstance.class)
public class MovementTutorialStepInstanceVRMixin {

    @Shadow private int moveCompleted;

    @Shadow private boolean moved;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 0), index = 1, method = "tick")
    private Component alterMovementTitle(Component title) {
        if (!VRState.vrRunning) {
            return title;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            // find the currently used movement binding
            if (MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveStrafe).isActive()) {
                // moveStrafe active
                return Component.translatable("vivecraft.toasts.move1", Component.literal(MCVR.get().getOriginName(MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveStrafe).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            } else if (MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveRotate).isActive()) {
                // moveRotate active
                return Component.translatable("vivecraft.toasts.move1", Component.literal(MCVR.get().getOriginName(MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveRotate).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            } else if (MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).isActive() ||
                    MCVR.get().getInputAction(Minecraft.getInstance().options.keyDown).isActive() ||
                    MCVR.get().getInputAction(Minecraft.getInstance().options.keyLeft).isActive() ||
                    MCVR.get().getInputAction(Minecraft.getInstance().options.keyRight).isActive()
            ) {
                // individual movement bindings
                Set<String> buttons = new HashSet<>();
                if (MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).isActive()) {
                    buttons.add(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyUp).getLastOrigin()));
                }
                if (MCVR.get().getInputAction(Minecraft.getInstance().options.keyDown).isActive()) {
                    buttons.add(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyDown).getLastOrigin()));
                }
                if (MCVR.get().getInputAction(Minecraft.getInstance().options.keyLeft).isActive()) {
                    buttons.add(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyLeft).getLastOrigin()));
                }
                if (MCVR.get().getInputAction(Minecraft.getInstance().options.keyRight).isActive()) {
                    buttons.add(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyRight).getLastOrigin()));
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
            } else if (MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyTeleportFallback).isActive()) {
                // teleport fallback
                return Component.translatable("vivecraft.toasts.move1", Component.literal(MCVR.get().getOriginName(MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyTeleportFallback).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            } else if (MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyTeleport).isActive()) {
                // teleport
                return Component.translatable("vivecraft.toasts.teleport", Component.literal(MCVR.get().getOriginName(MCVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyTeleport).getLastOrigin())).withStyle(ChatFormatting.BOLD));
            }
        }
        return title;
    }
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 0), index = 2, method = "tick")
    private Component alterMovementDescription(Component description) {
        if (!VRState.vrRunning) {
            return description;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated && MCVR.get().getInputAction(Minecraft.getInstance().options.keyJump).isActive()) {
            return Component.translatable("tutorial.move.description", Component.literal(MCVR.get().getOriginName(MCVR.get().getInputAction(Minecraft.getInstance().options.keyJump).getLastOrigin())).withStyle(ChatFormatting.BOLD));
        }
        return description;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/TutorialToast;<init>(Lnet/minecraft/client/gui/components/toasts/TutorialToast$Icons;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V", ordinal = 1), index = 2, method = "tick")
    private Component alterLookDescription(Component title) {
        if (!VRState.vrRunning) {
            return title;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            return Component.translatable("vivecraft.toasts.point_controller", Component.translatable(ClientDataHolderVR.getInstance().vrSettings.reverseHands ? "vivecraft.toasts.point_controller.left" : "vivecraft.toasts.point_controller.right").withStyle(ChatFormatting.BOLD));
        }
        return title;
    }

    @Inject(at = @At("TAIL"), method = "onInput")
    private void addTeleport(Input input, CallbackInfo ci) {
        moved |= VivecraftVRMod.INSTANCE.keyTeleport.isDown();
    }

    @Inject(at = @At("HEAD"), method = "onMouse", cancellable = true)
    private void onlyAfterMove(double d, double e, CallbackInfo ci) {
        if (moveCompleted == -1) {
            ci.cancel();
        }
    }
}
