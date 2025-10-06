package org.vivecraft.mixin.client.renderer.entity.layers;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.render.VRPlayerModel;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.trackers.ClimbTracker;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin extends RenderLayer {

    public ItemInHandLayerMixin(RenderLayerParent renderer) {
        super(renderer);
    }

    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("STORE"), ordinal = 0)
    private boolean vivecraft$isRightMainHand(boolean isRightMainHand, @Local(argsOnly = true) LivingEntity entity) {
        if (this.getParentModel() instanceof VRPlayerModel) {
            // we ignore the vanilla main arm setting, and use our own
            return !ClientVRPlayers.getInstance().isVRAndLeftHanded(entity.getUUID());
        } else {
            return isRightMainHand;
        }
    }

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack vivecraft$climbClawsOverride(
        ItemStack itemStack, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) HumanoidArm arm)
    {
        if (ClientNetworking.SERVER_ALLOWS_CLIMBEY && entity instanceof Player &&
            ClientVRPlayers.getInstance().isVRPlayer(entity.getUUID()) && !ClimbTracker.isClaws(itemStack))
        {
            boolean mainHand = arm ==
                (ClientVRPlayers.getInstance().isVRAndLeftHanded(entity.getUUID()) ? HumanoidArm.LEFT :
                    HumanoidArm.RIGHT
                );
            ItemStack otherStack = mainHand ? entity.getOffhandItem() : entity.getMainHandItem();
            if (ClimbTracker.isClaws(otherStack)) {
                if (entity instanceof LocalPlayer player && VRState.VR_RUNNING &&
                    ClientDataHolderVR.getInstance().climbTracker.isActive(player) &&
                    ClimbTracker.hasClimbeyClimbEquipped(player))
                {
                    return otherStack;
                } else if (entity instanceof RemotePlayer &&
                    !ClientVRPlayers.getInstance().isVRAndSeated(entity.getUUID()))
                {
                    return otherStack;
                }
            }
        }

        return itemStack;
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void vivecraft$firstPersonItemScale(
        CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) PoseStack poseStack)
    {
        if (VREffectsHelper.isRenderingFirstPersonPlayer(entity)) {
            // make the item scale equal in all directions
            poseStack.translate(0.0F, 0.65F, 0.0F);
            poseStack.scale(1F, ClientDataHolderVR.getInstance().vrSettings.playerModelArmsScale, 1f);
            poseStack.translate(0.0F, -0.65F, 0.0F);
        }
    }
}
