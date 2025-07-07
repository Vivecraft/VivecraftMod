package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.mixin.server.ServerPlayerMixin;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    protected ItemStack useItem;

    @Shadow
    public abstract boolean isBlocking();

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z", at = @At(value = "NEW", target = "net/minecraft/world/phys/Vec3", ordinal = 0))
    private Vec3 vivecraft$modifyOwnHeadPos(double x, double y, double z, Operation<Vec3> original) {
        if ((Object) this instanceof ServerPlayer player) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                return serverVivePlayer.getHMDPos();
            }
        }
        return original.call(x, y, z);
    }

    @WrapOperation(method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;D)Z", at = @At(value = "NEW", target = "net/minecraft/world/phys/Vec3", ordinal = 1))
    private Vec3 vivecraft$modifyOtherHeadPos(
        double x, double y, double z, Operation<Vec3> original, @Local(argsOnly = true) Entity other)
    {
        if (other instanceof ServerPlayer player) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                Vec3 hmdPos = serverVivePlayer.getHMDPos();
                // only use the hmd if it's meant to be the eye height
                return original.call(hmdPos.x, y == other.getEyeY() ? hmdPos.y : y, hmdPos.z);
            }
        }
        return original.call(x, y, z);
    }

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 vivecraft$hmdDir(
        LivingEntity instance, float partialTick, Operation<Vec3> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        if (instance instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer)) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
            hmdPos.set(serverVivePlayer.getHMDPos());
            return serverVivePlayer.getHMDDir();
        } else {
            return original.call(instance, partialTick);
        }
    }

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 1))
    private double vivecraft$hmdPosX(
        LivingEntity instance, Operation<Double> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        return hmdPos.get() != null ? hmdPos.get().x : original.call(instance);
    }

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEyeY()D"))
    private double vivecraft$hmdPosY(
        LivingEntity instance, Operation<Double> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        return hmdPos.get() != null ? hmdPos.get().y : original.call(instance);
    }

    @WrapOperation(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", ordinal = 1))
    private double vivecraft$hmdPosZ(
        LivingEntity instance, Operation<Double> original, @Share("hmdPos") LocalRef<Vec3> hmdPos)
    {
        return hmdPos.get() != null ? hmdPos.get().z : original.call(instance);
    }

    /**
     * dummy to be overridden in {@link ServerPlayerMixin}
     */
    @ModifyExpressionValue(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBlockingWith()Lnet/minecraft/world/item/ItemStack;"))
    protected ItemStack vivecraft$roomscaleShieldBlockingItem(
        ItemStack original, @Local(argsOnly = true) DamageSource damageSource,
        @Share("roomscaleBlocked") LocalBooleanRef roomscaleBlocked)
    {
        return original;
    }

    /**
     * part of {@link #vivecraft$roomscaleShieldBlockingItem}
     */
    @ModifyReturnValue(method = "isDamageSourceBlocked", at = @At(value = "RETURN", ordinal = 0))
    protected boolean vivecraft$roomscaleShieldIsBlocked(
        boolean blocked, @Share("roomscaleBlocked") LocalBooleanRef roomscaleBlocked)
    {
        return blocked || roomscaleBlocked.get();
    }
}
