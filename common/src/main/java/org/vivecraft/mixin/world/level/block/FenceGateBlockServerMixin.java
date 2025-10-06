package org.vivecraft.mixin.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.server.ServerVRPlayers;

@Mixin(FenceGateBlock.class)
public class FenceGateBlockServerMixin {
    @ModifyExpressionValue(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDirection()Lnet/minecraft/core/Direction;"))
    private Direction vivecraft$changeDirection(
        Direction direction, @Local(argsOnly = true) Player player, @Local(argsOnly = true) BlockHitResult hitResult)
    {
        // fix gate open direction being based on player look direction, instead of interaction direction
        if ((player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer)) ||
            // also do this on the client, but only if we can assume that the server also does this fix
            // since it would cause a wrong block state for a split second
            (player.isLocalPlayer() && VRState.VR_RUNNING &&
                ClientNetworking.USED_NETWORK_VERSION >= CommonNetworkHelper.NETWORK_VERSION_OPTION_TOGGLE
            ))
        {
            return hitResult.getDirection().getOpposite();
        } else {
            return direction;
        }
    }
}
