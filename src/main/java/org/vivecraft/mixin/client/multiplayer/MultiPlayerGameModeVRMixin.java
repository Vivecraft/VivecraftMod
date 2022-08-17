package org.vivecraft.mixin.client.multiplayer;

import org.vivecraft.DataHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.NetworkHelper;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeVRMixin {

    @Inject(at = @At("HEAD"), method = "useItem")
    public void overrideUse(Player player, Level level, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        NetworkHelper.overrideLook(player, DataHolder.getInstance().vrPlayer.getRightClickLookOverride(player, interactionHand.ordinal()));
    }

    @Inject(at = @At("HEAD"), method = "releaseUsingItem")
    public void overrideReleaseUse(Player player, CallbackInfo ci) {
        NetworkHelper.overrideLook(player, DataHolder.getInstance().vrPlayer.getRightClickLookOverride(player, player.getUsedItemHand().ordinal()));
    }

    @Inject(at = @At("HEAD"), method = "useItemOn")
    public void overrideUseOn(LocalPlayer localPlayer, ClientLevel clientLevel, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        NetworkHelper.overrideLook(localPlayer, blockHitResult.getLocation().subtract(localPlayer.getEyePosition(1.0F)).normalize());
    }
}
