package org.vivecraft.mixin.client_vr.renderer.item.properties.numeric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(CompassAngleState.class)
public class CompassAngleStateVRMixin {

    @Unique
    private ItemStack vivecraft$currentItem = null;

    @WrapOperation(method = "calculate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/properties/numeric/CompassAngleState;getRotationTowardsCompassTarget(Lnet/minecraft/world/entity/Entity;JLnet/minecraft/core/BlockPos;)F"))
    private float vivecraft$rememberItem(
        CompassAngleState instance, Entity entity, long gameTime, BlockPos targetPos, Operation<Float> original,
        @Local(argsOnly = true) ItemStack item)
    {
        this.vivecraft$currentItem = item;
        float rotation = original.call(instance, entity, gameTime, targetPos);
        this.vivecraft$currentItem = null;
        return rotation;
    }

    @WrapOperation(method = "getRotationTowardsCompassTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/properties/numeric/CompassAngleState;getAngleFromEntityToPos(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)D"))
    private double vivecraft$handPosition(Entity entity, BlockPos target, Operation<Double> original) {
        if (VRState.VR_RUNNING && entity instanceof LocalPlayer player &&
            player == Minecraft.getInstance().player)
        {
            // check if the current item is held in a hand
            if (this.vivecraft$currentItem == player.getMainHandItem()) {
                return vivecraft$angleBetweenPoints(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c0.getPosition(), target);
            } else if (this.vivecraft$currentItem == player.getOffhandItem()) {
                return vivecraft$angleBetweenPoints(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c1.getPosition(), target);
            }
        }
        return original.call(entity, target);
    }

    @Unique
    private double vivecraft$angleBetweenPoints(Vec3 origin, BlockPos targetBlock) {
        Vec3 target = Vec3.atCenterOf(targetBlock);
        return Math.atan2(target.z() - origin.z(), target.x() - origin.x()) / Mth.TWO_PI;
    }

    @WrapOperation(method = "getRotationTowardsCompassTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/properties/numeric/CompassAngleState;getWrappedVisualRotationY(Lnet/minecraft/world/entity/Entity;)F"))
    private float vivecraft$handAngle(
        Entity entity, Operation<Float> original, @Share("bodyYaw") LocalFloatRef bodyYaw)
    {
        if (VRState.VR_RUNNING && entity instanceof LocalPlayer player &&
            player == Minecraft.getInstance().player)
        {
            // use body yaw for wobble
            bodyYaw.set(Mth.positiveModulo(
                ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyYawRad() / Mth.TWO_PI, 1.0F));
            return bodyYaw.get();
        }
        return original.call(entity);
    }

    @ModifyExpressionValue(method = "getRotationTowardsCompassTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/properties/numeric/NeedleDirectionHelper$Wobbler;rotation()F"))
    private float vivecraft$handRotationOffset(
        float rotation, @Local(argsOnly = true) Entity entity, @Share("bodyYaw") LocalFloatRef bodyYaw)
    {
        if (VRState.VR_RUNNING && entity instanceof LocalPlayer player &&
            player == Minecraft.getInstance().player)
        {
            // check if the current item is held in a hand
            if (this.vivecraft$currentItem == player.getMainHandItem()) {
                rotation = rotation + bodyYaw.get() - Mth.positiveModulo(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c0.getYawRad() / Mth.TWO_PI, 1.0F);
            } else if (this.vivecraft$currentItem == player.getOffhandItem()) {
                rotation = rotation + bodyYaw.get() - Mth.positiveModulo(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c1.getYawRad() / Mth.TWO_PI, 1.0F);
            }
        }
        return rotation;
    }
}
