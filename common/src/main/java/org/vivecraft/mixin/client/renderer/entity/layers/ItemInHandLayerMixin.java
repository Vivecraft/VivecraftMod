package org.vivecraft.mixin.client.renderer.entity.layers;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {
    @ModifyVariable(at = @At("HEAD"), method = "renderArmWithItem", argsOnly = true)
    private ItemStack vivecraft$climbClawsOverride(ItemStack itemStack, LivingEntity livingEntity, ItemStack itemStack2, ItemDisplayContext itemDisplayContext, HumanoidArm humanoidArm) {
        ClimbTracker tracker = ClientDataHolderVR.getInstance().climbTracker;
        if (ClientNetworking.serverAllowsClimbey && livingEntity instanceof Player && !tracker.isClaws(itemStack)) {
            ItemStack otherStack = humanoidArm == livingEntity.getMainArm() ? livingEntity.getOffhandItem() : livingEntity.getMainHandItem();
            if (tracker.isClaws(otherStack)) {
                if (livingEntity instanceof LocalPlayer player && VRState.vrRunning && tracker.isActive(player) && ((PlayerExtension) player).vivecraft$isClimbeyClimbEquipped()) {
                    return otherStack;
                } else if (livingEntity instanceof RemotePlayer player && VRPlayersClient.getInstance().isVRPlayer(player) && !VRPlayersClient.getInstance().getRotationsForPlayer(player.getUUID()).seated) {
                    return otherStack;
                }
            }
        }

        return itemStack;
    }
}
