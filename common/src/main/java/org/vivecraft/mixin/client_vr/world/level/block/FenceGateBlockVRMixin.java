package org.vivecraft.mixin.client_vr.world.level.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(FenceGateBlock.class)
public class FenceGateBlockVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"), method = "use", locals = LocalCapture.CAPTURE_FAILHARD)
    public void hapticFeedbackOnClose1(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir, boolean opening) {
        if (VRState.vrRunning && !opening && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isAlive() && Minecraft.getInstance().player.blockPosition().distSqr(blockPos) < 25.0D) {
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 250);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"), method = "neighborChanged", locals = LocalCapture.CAPTURE_FAILHARD)
    public void hapticFeedbackOnClose2(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl, CallbackInfo ci, boolean opening) {
        if (VRState.vrRunning && !opening && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isAlive() && Minecraft.getInstance().player.blockPosition().distSqr(blockPos) < 25.0D) {
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 250);
        }
    }
}
