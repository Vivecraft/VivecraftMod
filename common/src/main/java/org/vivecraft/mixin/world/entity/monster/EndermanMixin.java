package org.vivecraft.mixin.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;

@Mixin(EnderMan.class)
public abstract class EndermanMixin extends Monster {

    protected EndermanMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "isBeingStaredBy", at = @At(value = "CONSTANT", args = "doubleValue=0.025"))
    private double vivecraft$biggerViewCone(double original, @Local(argsOnly = true) Player player) {
        // increase the view cone check from 1.4° to 5.7°, makes it easier to stop enderman,
        // since it's hard to know where the center of the view is
        return player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer) ? 0.1 : original;
    }
}
