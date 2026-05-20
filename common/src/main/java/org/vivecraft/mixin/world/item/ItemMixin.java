package org.vivecraft.mixin.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.server.ServerVRPlayers;

@Mixin(Item.class)
public class ItemMixin {
    // these are used for bucket use/boat placement
    @WrapOperation(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getEyePosition()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 vivecraft$modifyAimPos(Player player, Operation<Vec3> original) {
        if (player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer)) {
            return ServerVRPlayers.getVivePlayer(serverPlayer).getAimPos(false);
        } else if (player.isLocalPlayer() && VRState.VR_RUNNING) {
            Vec3 pos = ClientNetworking.getActiveAimPos();
            if (pos != null) {
                return pos;
            }
        }
        return original.call(player);
    }

    @WrapOperation(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 vivecraft$modifyAimDir(Player player, float xRot, float yRot, Operation<Vec3> original) {
        if (player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer)) {
            return ServerVRPlayers.getVivePlayer(serverPlayer).getAimDir(false);
        } else if (player.isLocalPlayer() && VRState.VR_RUNNING) {
            return new Vec3(ClientNetworking.getActiveAimDir());
        }
        return original.call(player, xRot, yRot);
    }
}
