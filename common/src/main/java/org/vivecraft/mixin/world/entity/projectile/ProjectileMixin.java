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
	private Vec3 controllerDir;

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), ordinal = 3, argsOnly = true)
	public float pVelocity(float pVelocity, Entity pProjectile) {
		if (pProjectile instanceof ServerPlayer player) {
			ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
			if (serverVivePlayer != null && serverVivePlayer.isVR()) {
				this.controllerDir = serverVivePlayer.getControllerDir(serverVivePlayer.activeHand);
				if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !serverVivePlayer.isSeated() && serverVivePlayer.getDraw() > 0.0F){
					this.controllerDir = serverVivePlayer.getControllerPos(1, (Player) pProjectile).subtract(serverVivePlayer.getControllerPos(0, (Player) pProjectile)).normalize();
					((AbstractArrow)(Object)this).setBaseDamage(((AbstractArrow)(Object)this).getBaseDamage() * 2.0D);
					return pVelocity * serverVivePlayer.getDraw();
				}
			}
		}
		return pVelocity;
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public float pX(float pXIn, Entity pProjectile) {
		if (this.controllerDir != null) {
			return -((float) Math.toDegrees(Math.asin(this.controllerDir.y / this.controllerDir.length())));
		}
		return pXIn;
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), ordinal = 1, argsOnly = true)
	public float pY(float pYIn, Entity pProjectile) {
		if (this.controllerDir != null) {
			float toRet = (float) Math.toDegrees(Math.atan2(-this.controllerDir.x, this.controllerDir.z));
			this.controllerDir = null;
			return toRet;
		}
		return pYIn;
	}
}
