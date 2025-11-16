package org.vivecraft.mixin.client_vr.multiplayer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.function.Supplier;

/**
 * we override the players look direction so the server handles any interactions as if the player looked at the interacted block
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeVRMixin {

    @WrapMethod(method = "useItem")
    private InteractionResult vivecraft$useLookOverride(
        Player player, InteractionHand hand, Operation<InteractionResult> original)
    {
        return this.vivecraft$wrapWithLookOverride(() -> original.call(player, hand), player,
            () -> ClientDataHolderVR.getInstance().vrPlayer.getRightClickLookOverride(player, hand.ordinal()));
    }

    @WrapMethod(method = "releaseUsingItem")
    private void vivecraft$releaseUseLookOverride(Player player, Operation<Void> original) {
        this.vivecraft$wrapWithLookOverride(() -> original.call(player), player,
            () -> ClientDataHolderVR.getInstance().vrPlayer.getRightClickLookOverride(player,
                player.getUsedItemHand().ordinal()));
    }

    @WrapMethod(method = "useItemOn")
    private InteractionResult vivecraft$useOnLookOverride(
        LocalPlayer player, InteractionHand hand, BlockHitResult result, Operation<InteractionResult> original)
    {
        return this.vivecraft$wrapWithLookOverride(() -> original.call(player, hand, result), player,
            () -> ClientDataHolderVR.getInstance().vrPlayer.getRightClickLookOverride(player, hand.ordinal()));
    }

    @Unique
    private <T> T vivecraft$wrapWithLookOverride(Supplier<T> useCall, Player player, Supplier<Vec3> viewSupplier) {
        if (VRState.VR_RUNNING) {
            ClientNetworking.overrideLook(player, viewSupplier);
        }
        T result = useCall.get();
        if (VRState.VR_RUNNING) {
            ClientNetworking.restoreLook();
        }
        return result;
    }

    @WrapOperation(method = "method_41929", at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ServerboundUseItemPacket"))
    private ServerboundUseItemPacket vivecraft$lookOverridePacket(
        InteractionHand hand, int sequence, float yRot, float xRot, Operation<ServerboundUseItemPacket> original)
    {
        if (VRState.VR_RUNNING) {
            if (ClientNetworking.OVERRIDE_ACTIVE) {
                yRot = ClientNetworking.OVERRIDDEN_YAW;
                xRot = ClientNetworking.OVERRIDDEN_PITCH;
            } else {
                VRBodyPart bp = ClientNetworking.IS_LAST_BODY_PART_AIM ? ClientNetworking.getActiveBodyPart() :
                    ClientDataHolderVR.getInstance().vrSettings.aimDevice == VRSettings.AimDevice.HMD ?
                        VRBodyPart.HEAD : VRBodyPart.MAIN_HAND;
                Vector3f dir = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyPart(bp)
                    .getDirection();
                yRot = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
                xRot = (float) Math.toDegrees(Math.asin(-dir.y / dir.length()));
            }
        }
        return original.call(hand, sequence, yRot, xRot);
    }

    @WrapMethod(method = "sameDestroyTarget")
    private boolean vivecraft$dualWieldingSkipItemCheck(BlockPos pos, Operation<Boolean> original) {
        if (VRState.VR_RUNNING && ClientNetworking.SERVER_ALLOWS_DUAL_WIELDING) {
            // check if main or offhand items match the started item, we want to limit abuse of this,
            // but still make both items work

            ClientNetworking.BODY_PART_CLIENT_OVERRIDE = VRBodyPart.MAIN_HAND;
            boolean sameItem = original.call(pos);

            ClientNetworking.BODY_PART_CLIENT_OVERRIDE = VRBodyPart.OFF_HAND;
            sameItem |= original.call(pos);

            ClientNetworking.BODY_PART_CLIENT_OVERRIDE = null;
            return sameItem;
        } else {
            return original.call(pos);
        }
    }
}
