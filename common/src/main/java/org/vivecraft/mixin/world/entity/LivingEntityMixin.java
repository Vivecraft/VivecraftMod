package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
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
    public abstract boolean isBlocking();

    @Shadow
    protected ItemStack useItem;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "hasLineOfSight", at = @At(value = "NEW", target = "net/minecraft/world/phys/Vec3", ordinal = 0))
    private Vec3 vivecraft$modifyOwnHeadPos(double x, double y, double z, Operation<Vec3> original) {
        if ((Object) this instanceof ServerPlayer player) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                return serverVivePlayer.getHMDPos();
            }
        }
        return original.call(x, y, z);
    }

    @WrapOperation(method = "hasLineOfSight", at = @At(value = "NEW", target = "net/minecraft/world/phys/Vec3", ordinal = 1))
    private Vec3 vivecraft$modifyOtherHeadPos(
        double x, double y, double z, Operation<Vec3> original, @Local(argsOnly = true) Entity other)
    {
        if (other instanceof ServerPlayer player) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                return serverVivePlayer.getHMDPos();
            }
        }
        return original.call(x, y, z);
    }

    /**
     * dummy to be overridden in {@link ServerPlayerMixin}
     */
    @ModifyExpressionValue(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isBlocking()Z"))
    protected boolean vivecraft$roomscaleShieldBlockingItem(
        boolean isBlocking, @Local(argsOnly = true) DamageSource damageSource,
        @Share("roomscaleBlocked") LocalBooleanRef roomscaleBlocked)
    {
        return isBlocking;
    }

    /**
     * part of {@link #vivecraft$roomscaleShieldBlockingItem}
     */
    @ModifyReturnValue(method = "isDamageSourceBlocked", at = @At(value = "RETURN", ordinal = 0))
    private boolean vivecraft$roomscaleShieldIsBlocked(
        boolean blocked, @Share("roomscaleBlocked") LocalBooleanRef roomscaleBlocked)
    {
        return blocked || roomscaleBlocked.get();
    }
}
