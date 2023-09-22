package org.vivecraft.mixin.world.entity.ai.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

@Mixin(SwellGoal.class)
public class SwellGoalMixin {

    @Final
    @Shadow
    private Creeper creeper;

    @Inject(at = @At("HEAD"), method = "canUse", cancellable = true)
    public void vivecraft$vrSwellDistance(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = this.creeper.getTarget();
        if (target instanceof ServerPlayer player && ServerVRPlayers.isVRPlayer(player)) {
            ServerVivePlayer data = ServerVRPlayers.getVivePlayer(player);
            if (data != null && !data.isSeated()) {
                double swellDistance = ServerConfig.creeperSwellDistance.get();
                cir.setReturnValue(this.creeper.getSwellDir() > 0 || this.creeper.distanceToSqr(target) < swellDistance * swellDistance);
            }
        }
    }
}
