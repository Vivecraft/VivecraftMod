package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {

	@Unique
	private ServerVivePlayer serverVivePlayer;

	@Unique
	private Vec3 controllerDir;

	@Shadow
	public abstract void shoot(double f2, double f, double f1, float pVelocity, float pInaccuracy);

	public ProjectileMixin(EntityType<?> p_19870_, Level p_19871_) {
		super(p_19870_, p_19871_);
		// TODO Auto-generated constructor stub
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), ordinal = 0, argsOnly = true)
	public float pX(float pXIn, Entity pProjectile) {
		this.serverVivePlayer = CommonNetworkHelper.vivePlayers.get(pProjectile.getUUID());
		if (this.serverVivePlayer != null && this.serverVivePlayer.isVR()) {
			this.controllerDir = this.serverVivePlayer.getControllerDir(this.serverVivePlayer.activeHand);
			if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !this.serverVivePlayer.isSeated() && this.serverVivePlayer.getDraw() > 0.0F) {
				this.controllerDir = this.serverVivePlayer.getControllerPos(1, (Player) pProjectile).subtract(this.serverVivePlayer.getControllerPos(0, (Player) pProjectile)).normalize();
			}
			return -((float) Math.toDegrees(Math.asin(this.controllerDir.y / this.controllerDir.length())));
		}
		return pXIn;
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), ordinal = 1, argsOnly = true)
	public float pY(float pYIn, Entity pProjectile) {
		if (this.controllerDir != null) {
			return (float) Math.toDegrees(Math.atan2(-this.controllerDir.x, this.controllerDir.z));
		}
		return pYIn;
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), ordinal = 3, argsOnly = true)
	public float pVelocity(float pVelocity, Entity pProjectile) {
		if (this.serverVivePlayer != null && this.serverVivePlayer.isVR()) {
			if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !this.serverVivePlayer.isSeated() && this.serverVivePlayer.getDraw() > 0.0F){
				((AbstractArrow)(Object)this).setBaseDamage(((AbstractArrow)(Object)this).getBaseDamage() * 2.0D);
				return pVelocity * this.serverVivePlayer.getDraw();
			}
		}
		return pVelocity;
	}
}
