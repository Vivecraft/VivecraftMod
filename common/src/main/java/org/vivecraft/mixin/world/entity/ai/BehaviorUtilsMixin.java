package org.vivecraft.mixin.world.entity.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

@Mixin(BehaviorUtils.class)
public class BehaviorUtilsMixin {
    @WrapOperation(method = "isWithinMeleeAttackRange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getMeleeAttackRangeSqr(Lnet/minecraft/world/entity/LivingEntity;)D"))
    private static double vivecraft$adjustRange(Mob instance, LivingEntity entity, Operation<Double> original) {
        double attackRange = original.call(instance, entity);
        if (entity instanceof ServerPlayer player && ServerConfig.MOB_ATTACK_RANGE_ADJUSTMENT.get() < 0) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && !serverVivePlayer.isSeated()) {
                double reduction = ServerConfig.MOB_ATTACK_RANGE_ADJUSTMENT.get();
                attackRange += 2.0 * Math.sqrt(attackRange) * reduction + reduction * reduction;
            }
        }
        return attackRange;
    }
}
