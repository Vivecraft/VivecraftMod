package org.vivecraft.mixin.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.server.ServerVRPlayers;

@Mixin(Item.class)
public class ItemMixin {
    // these are used for bucket use/boat placement
    @WrapOperation(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getEyePosition()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 vivecraft$modifyAimPos(Player player, Operation<Vec3> original) {
        if (player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer)) {
            return ServerVRPlayers.getVivePlayer(serverPlayer).getAimPos(false);
        } else if (player.isLocalPlayer() && VRState.VR_RUNNING && !ClientNetworking.OVERRIDE_ACTIVE) {
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyPart(
                    ClientNetworking.IS_LAST_BODY_PART_AIM ? ClientNetworking.getActiveBodyPart() : VRBodyPart.MAIN_HAND)
                .getPosition();
        }
        return original.call(player);
    }

    @ModifyExpressionValue(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getXRot()F"))
    private static float vivecraft$localXRot(float xRot, @Local(argsOnly = true) Player player) {
        if (player.isLocalPlayer() && VRState.VR_RUNNING && ClientNetworking.OVERRIDE_ACTIVE) {
            return ClientNetworking.OVERRIDDEN_PITCH;
        } else {
            return xRot;
        }
    }

    @ModifyExpressionValue(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"))
    private static float vivecraft$localYRot(float yRot, @Local(argsOnly = true) Player player) {
        if (player.isLocalPlayer() && VRState.VR_RUNNING && ClientNetworking.OVERRIDE_ACTIVE) {
            return ClientNetworking.OVERRIDDEN_YAW;
        } else {
            return yRot;
        }
    }

    @WrapOperation(method = "getPlayerPOVHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 vivecraft$modifyAimDir(
        Vec3 instance, double x, double y, double z, Operation<Vec3> original, @Local(argsOnly = true) Player player)
    {
        Vec3 aim = null;
        if (player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer)) {
            aim = ServerVRPlayers.getVivePlayer(serverPlayer).getAimDir(false);
        } else if (player.isLocalPlayer() && VRState.VR_RUNNING && !ClientNetworking.OVERRIDE_ACTIVE) {
            aim = new Vec3(ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyPart(
                    ClientNetworking.IS_LAST_BODY_PART_AIM ? ClientNetworking.getActiveBodyPart() : VRBodyPart.MAIN_HAND)
                .getDirection());
        }
        if (aim != null) {
            double length = Math.sqrt(x * x + y * y + z * z);
            aim = aim.scale(length);
            return original.call(instance, aim.x, aim.y, aim.z);
        } else {
            return original.call(instance, x, y, z);
        }
    }
}
