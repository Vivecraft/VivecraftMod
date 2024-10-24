package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
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
}
