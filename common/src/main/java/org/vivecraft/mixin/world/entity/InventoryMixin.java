package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Shadow
    @Final
    public Player player;

    @Shadow
    @Final
    private EntityEquipment equipment;

    @ModifyReturnValue(method = "getSelectedItem", at = @At("RETURN"))
    private ItemStack vivecraft$dualHandingItem(ItemStack original) {
        return vivecraft$activeItem(original);
    }

    @Unique
    private ItemStack vivecraft$activeItem(ItemStack original) {
        VRBodyPart bodyPart = null;
        // server side
        if (this.player instanceof ServerPlayer serverPlayer && ServerConfig.DUAL_WIELDING.get()) {
            if (ServerVRPlayers.isVRPlayer(serverPlayer)) {
                ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
                // older clients don't reset the active hand
                if (vivePlayer.networkVersion >= CommonNetworkHelper.NETWORK_VERSION_DUAL_WIELDING) {
                    bodyPart = vivePlayer.activeBodyPart;
                }
            }
        }
        // client side
        else if (this.player.isLocalPlayer() && VRState.VR_RUNNING && ClientNetworking.SERVER_ALLOWS_DUAL_WIELDING) {
            bodyPart = ClientNetworking.getActiveBodyPart();
        }

        if (bodyPart != null) {
            if (bodyPart == VRBodyPart.OFF_HAND) {
                return this.equipment.get(EquipmentSlot.OFFHAND);
            } else if (bodyPart != VRBodyPart.MAIN_HAND && bodyPart != VRBodyPart.HEAD) {
                // feet
                return ItemStack.EMPTY;
            }
        }
        return original;
    }
}
