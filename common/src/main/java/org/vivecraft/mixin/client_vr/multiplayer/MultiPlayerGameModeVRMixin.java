package org.vivecraft.mixin.client_vr.multiplayer;

import org.vivecraft.client.network.ClientNetworking;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(net.minecraft.client.multiplayer.MultiPlayerGameMode.class)
public class MultiPlayerGameModeVRMixin {

    @Inject(at = @At("HEAD"), method = "useItem")
    public void overrideUse(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (vrRunning) {
            ClientNetworking.overrideLook(player, dh.vrPlayer.getRightClickLookOverride(player, interactionHand.ordinal()));
        }
    }

    @Inject(at = @At("HEAD"), method = "releaseUsingItem")
    public void overrideReleaseUse(Player player, CallbackInfo ci) {
        if (vrRunning) {
            ClientNetworking.overrideLook(player, dh.vrPlayer.getRightClickLookOverride(player, player.getUsedItemHand().ordinal()));
        }
    }

    @Inject(at = @At("HEAD"), method = "useItemOn")
    public void overrideUseOn(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (vrRunning) {
            ClientNetworking.overrideLook(localPlayer, blockHitResult.getLocation().subtract(localPlayer.getEyePosition(1.0F)).normalize());
        }
    }
}
