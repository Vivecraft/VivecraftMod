package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

@Mixin(Mob.class)
public class MobMixin {
    @WrapOperation(method = "isWithinMeleeAttackRange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getAttackBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB vivecraft$adjustRange(
        Mob instance, Operation<AABB> original, @Local(argsOnly = true) LivingEntity other)
    {
        AABB attackRange = original.call(instance);
        if (other instanceof ServerPlayer player && ServerConfig.MOB_ATTACK_RANGE_ADNJUSTMENT.get() < 0) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && !serverVivePlayer.isSeated()) {
                attackRange = attackRange.inflate(ServerConfig.MOB_ATTACK_RANGE_ADNJUSTMENT.get(), 0,
                    ServerConfig.MOB_ATTACK_RANGE_ADNJUSTMENT.get());
            }
        }
        return attackRange;
    }
}
