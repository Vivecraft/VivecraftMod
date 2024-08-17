package org.vivecraft.mixin.client.renderer.entity.layers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {
    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack vivecraft$climbClawsOverride(
        ItemStack itemStack, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) HumanoidArm arm)
    {
        if (ClientNetworking.serverAllowsClimbey && entity instanceof Player && !ClimbTracker.isClaws(itemStack)) {
            ItemStack otherStack = arm == entity.getMainArm() ? entity.getOffhandItem() : entity.getMainHandItem();
            if (ClimbTracker.isClaws(otherStack)) {
                ClimbTracker tracker = ClientDataHolderVR.getInstance().climbTracker;
                if (entity instanceof LocalPlayer player && VRState.vrRunning && tracker.isActive(player) && ClimbTracker.hasClimbeyClimbEquipped(player)) {
                    return otherStack;
                } else if (entity instanceof RemotePlayer player && VRPlayersClient.getInstance().isVRPlayer(player) && !VRPlayersClient.getInstance().getRotationsForPlayer(player.getUUID()).seated) {
                    return otherStack;
                }
            }
        }

        return itemStack;
    }
}
