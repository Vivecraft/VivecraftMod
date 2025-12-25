package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

@Mixin(Mob.class)
public class MobMixin {
    @ModifyArg(method = "isWithinMeleeAttackRange", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getAttackBoundingBox(D)Lnet/minecraft/world/phys/AABB;", ordinal = 0))
    private double vivecraft$adjustRange(double distance, @Local(argsOnly = true) LivingEntity other)
    {
        if (other instanceof ServerPlayer player && ServerConfig.MOB_ATTACK_RANGE_ADJUSTMENT.get() < 0) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && !serverVivePlayer.isSeated()) {
                distance += ServerConfig.MOB_ATTACK_RANGE_ADJUSTMENT.get();
            }
        }
        return distance;
    }
}
