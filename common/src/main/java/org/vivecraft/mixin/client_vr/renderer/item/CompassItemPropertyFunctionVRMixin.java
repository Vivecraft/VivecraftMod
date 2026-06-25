package org.vivecraft.mixin.client_vr.renderer.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(targets = "net.minecraft.client.renderer.item.ItemProperties$2")
public class CompassItemPropertyFunctionVRMixin {

    @WrapOperation(method = "unclampedCall", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemProperties$2;getAngleTo(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/Entity;)D"))
    private double vivecraft$handPosition(
        @Coerce Object instance, Vec3 target, Entity entity,
        Operation<Double> original, @Local(argsOnly = true) ItemStack item)
    {
        if (VRState.VR_RUNNING && entity instanceof LocalPlayer player &&
            player == Minecraft.getInstance().player)
        {
            // check if the current item is held in a hand
            if (item == player.getMainHandItem()) {
                return vivecraft$angleBetweenPoints(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c0.getPosition(), target);
            } else if (item == player.getOffhandItem()) {
                return vivecraft$angleBetweenPoints(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c1.getPosition(), target);
            }
        }
        return original.call(instance, target, entity);
    }

    @Unique
    private double vivecraft$angleBetweenPoints(Vec3 origin, Vec3 target) {
        return Math.atan2(target.z() - origin.z(), target.x() - origin.x());
    }

    @WrapOperation(method = "unclampedCall", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;positiveModulo(DD)D"))
    private double vivecraft$handAngle(
        double numerator, double denominator, Operation<Double> original,
        @Local(argsOnly = true) LivingEntity entity, @Share("bodyYaw") LocalFloatRef bodyYaw)
    {
        if (VRState.VR_RUNNING && entity instanceof LocalPlayer player &&
            player == Minecraft.getInstance().player)
        {
            // use body yaw for wobble
            bodyYaw.set(Mth.positiveModulo(
                ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyYawRad() / Mth.TWO_PI, 1.0F));
            return bodyYaw.get();
        }
        return original.call(numerator, denominator);
    }

    @ModifyExpressionValue(method = "unclampedCall", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/item/ItemProperties$CompassWobble;rotation:D"))
    private double vivecraft$handRotationOffset(
        double rotation, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) ItemStack item,
        @Share("bodyYaw") LocalFloatRef bodyYaw)
    {
        if (VRState.VR_RUNNING && entity instanceof LocalPlayer player &&
            player == Minecraft.getInstance().player)
        {
            // check if the current item is held in a hand
            if (item == player.getMainHandItem()) {
                rotation = rotation + bodyYaw.get() - Mth.positiveModulo(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c0.getYawRad() / Mth.TWO_PI, 1.0F);
            } else if (item == player.getOffhandItem()) {
                rotation = rotation + bodyYaw.get() - Mth.positiveModulo(
                    ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().c1.getYawRad() / Mth.TWO_PI, 1.0F);
            }
        }
        return rotation;
    }
}
