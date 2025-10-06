package org.vivecraft.mixin.world.entity.ai.goal;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

@Mixin(targets = {
    "net.minecraft.world.entity.ai.goal.MeleeAttackGoal",
    "net.minecraft.world.entity.animal.PolarBear$PolarBearMeleeAttackGoal",
    "net.minecraft.world.entity.animal.Rabbit$EvilRabbitAttackGoal",
    "net.minecraft.world.entity.monster.Ravager$RavagerMeleeAttackGoal",
    "net.minecraft.world.entity.monster.Spider$SpiderAttackGoal",
    "net.minecraft.world.entity.monster.Vindicator$VindicatorMeleeAttackGoal"
})
public class MeleeAttackGoalMixin {
    @ModifyReturnValue(method = "getAttackReachSqr", at = @At(value = "RETURN", ordinal = 0))
    private double vivecraft$reducedAttackRange(double original, @Local(argsOnly = true) LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer player && ServerConfig.MOB_ATTACK_RANGE_ADJUSTMENT.get() < 0) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && !serverVivePlayer.isSeated()) {
                double reduction = ServerConfig.MOB_ATTACK_RANGE_ADJUSTMENT.get();
                original += 2.0 * Math.sqrt(original) * reduction + reduction * reduction;
            }
        }
        return original;
    }
}
