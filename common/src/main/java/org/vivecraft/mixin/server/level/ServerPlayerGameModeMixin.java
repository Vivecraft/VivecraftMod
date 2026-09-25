package org.vivecraft.mixin.server.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

import java.util.function.Supplier;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    @Shadow
    private boolean isDestroyingBlock;

    @Unique
    private int vivecraft$lastRoomscaleAttackHitUpdate = 0;

    @Unique
    private int vivecraft$lastRoomscaleAttackParticlesRemaining = 0;

    @Unique
    // remember if the last block break action was done with roomscale, to send break updates correctly to clients
    private boolean vivecraft$lastHitRoomscale;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;incrementDestroyProgress(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;I)F", ordinal = 1))
    private float vivecraft$noUpdateForRoomscaleProgress(
        ServerPlayerGameMode instance, BlockState blockState, BlockPos delayedDestroyPos, int ticksSpentDestroying,
        Operation<Float> original)
    {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(this.player);
        // doesn't matter if they are currently in vr, if they hit roomscale do not update on tick
        if (vivePlayer != null && this.vivecraft$lastHitRoomscale) {
            ticksSpentDestroying = vivePlayer.roomscaleHitCount;
            // make sure this doesn't go over 1, or the progress will disappear
            float prog = blockState.getDestroyProgress(this.player, this.player.level(), delayedDestroyPos);
            // -2, because incrementDestroyProgress checks +1
            ticksSpentDestroying = Math.clamp(ticksSpentDestroying, 1, (int) (1.0F / prog) - 2);
        }

        return original.call(instance, blockState, delayedDestroyPos, ticksSpentDestroying);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V"))
    private void vivecraft$noUpdateForRoomscaleEvent(
        ServerLevel instance, Entity source, int event, BlockPos pos, int direction, Operation<Void> original)
    {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(this.player);
        // doesn't matter if they are currently in vr, if they hit roomscale do not send updates on tick
        if (vivePlayer == null || !this.vivecraft$lastHitRoomscale) {
            original.call(instance, source, event, pos, direction);
        } else if (vivePlayer.roomscaleHitCount != this.vivecraft$lastRoomscaleAttackHitUpdate) {
            this.vivecraft$lastRoomscaleAttackHitUpdate = vivePlayer.roomscaleHitCount;
            // send the sound on first tick after hit
            original.call(instance, source, LevelEvent.PARTICLES_AND_SOUND_DESTROY_PROGRESS, pos, direction);
            this.vivecraft$lastRoomscaleAttackParticlesRemaining = 3;
        } else if (this.vivecraft$lastRoomscaleAttackParticlesRemaining > 0) {
            // sned the particles for multiple ticks
            this.vivecraft$lastRoomscaleAttackParticlesRemaining--;
            original.call(instance, source, LevelEvent.PARTICLES_DESTROY_PROGRESS, pos, direction);
        }
    }

    @Inject(method = "abortDestroyBlock", at = @At("TAIL"))
    private void vivecraft$resetProgressAbort(CallbackInfo ci) {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(this.player);
        if (vivePlayer != null) {
            vivePlayer.roomscaleHitCount = 0;
        }
    }

    @Inject(method = "destroyBlock", at = @At("TAIL"))
    private void vivecraft$resetProgressDestroy(CallbackInfoReturnable<Boolean> cir) {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(this.player);
        if (vivePlayer != null) {
            vivePlayer.roomscaleHitCount = 0;
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("TAIL"))
    private void vivecraft$setRoomscaleHit(CallbackInfo ci) {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(this.player);
        if (vivePlayer != null) {
            this.vivecraft$lastHitRoomscale = this.isDestroyingBlock && vivePlayer.isVR() && vivePlayer.isHitRoomscale;
            if (!this.vivecraft$lastHitRoomscale) {
                vivePlayer.roomscaleHitCount = 0;
            }
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;incrementDestroyProgress(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;I)F", ordinal = 0))
    private float vivecraft$wrapDestroyProgress(
        ServerPlayerGameMode instance, BlockState state, BlockPos pos,
        int startTick, Operation<Float> original)
    {
        return vivecraft$wrapWithBodyPartChange(() -> original.call(instance, state, pos, startTick), false);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean vivecraft$wrapDestroy(ServerPlayerGameMode instance, BlockPos pos, Operation<Boolean> original) {
        return vivecraft$wrapWithBodyPartChange(() -> original.call(instance, pos), true);
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;hasDelayedDestroy:Z", opcode = Opcodes.PUTFIELD))
    private void vivecraft$storeDelayedBodyPart(CallbackInfo ci) {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(this.player);
        if (vivePlayer != null && vivePlayer.isVR()) {
            // store the BodyPart to continue destroying with it
            vivePlayer.delayedDestroyBodyPart = vivePlayer.getActiveItemBodyPart();
        }
    }

    @ModifyExpressionValue(method = "handleBlockBreakAction", at = @At(value = "CONSTANT", args = "floatValue=0.7F"))
    private float vivecraft$allowFasterBreak(float original) {
        if (ServerConfig.ALLOW_FASTER_BLOCK_BREAKING.get() && ServerVRPlayers.isVRPlayer(this.player)) {
            // allow VR players to break blocks faster than vanilla
            return 0F;
        }
        return original;
    }

    @Unique
    private <T> T vivecraft$wrapWithBodyPartChange(Supplier<T> supplier, boolean reset) {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(this.player);
        VRBodyPart org = null;
        if (vivePlayer != null && vivePlayer.isVR() && vivePlayer.delayedDestroyBodyPart != null) {
            org = vivePlayer.activeBodyPart;
            vivePlayer.activeBodyPart = vivePlayer.delayedDestroyBodyPart;
        }

        T res = supplier.get();

        if (org != null) {
            vivePlayer.activeBodyPart = org;
            if (reset) {
                vivePlayer.delayedDestroyBodyPart = null;
            }
        }
        return res;
    }
}
