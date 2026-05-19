package org.vivecraft.mixin.server.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

import java.util.function.Supplier;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow
    @Final
    protected ServerPlayer player;

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
