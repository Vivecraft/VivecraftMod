package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(Projectile.class)
public class ProjectileMixin {

    @Unique
    private Vec3 vivecraft$controllerDir;

    @ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
        at = @At("HEAD"), ordinal = 3, argsOnly = true)
    public float vivecraft$pVelocity(float pVelocity, Entity pProjectile) {
        if (pProjectile instanceof ServerPlayer player) {
            ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
            if (serverVivePlayer != null && serverVivePlayer.isVR()) {
                this.vivecraft$controllerDir = serverVivePlayer.getControllerDir(serverVivePlayer.activeHand);
                if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !serverVivePlayer.isSeated() && serverVivePlayer.getDraw() > 0.0F) {
                    this.vivecraft$controllerDir = serverVivePlayer.getControllerPos(1, (Player) pProjectile).subtract(serverVivePlayer.getControllerPos(0, (Player) pProjectile)).normalize();
                }
            }
        }
        return pVelocity;
    }

    @ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
        at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float vivecraft$pX(float pXIn, Entity pProjectile) {
        if (this.vivecraft$controllerDir != null) {
            return -((float) Math.toDegrees(Math.asin(this.vivecraft$controllerDir.y / this.vivecraft$controllerDir.length())));
        }
        return pXIn;
    }

    @ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
        at = @At("HEAD"), ordinal = 1, argsOnly = true)
    public float vivecraft$pY(float pYIn, Entity pProjectile) {
        if (this.vivecraft$controllerDir != null) {
            float toRet = (float) Math.toDegrees(Math.atan2(-this.vivecraft$controllerDir.x, this.vivecraft$controllerDir.z));
            this.vivecraft$controllerDir = null;
            return toRet;
        }
        return pYIn;
    }
}
