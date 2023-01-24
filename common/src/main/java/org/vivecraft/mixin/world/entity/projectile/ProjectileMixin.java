package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.util.Mth;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.vivecraft.api.ClientNetworkHelper;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {

	@Shadow
	public abstract void shoot(double f2, double f, double f1, float pVelocity, float pInaccuracy);

	public ProjectileMixin(EntityType<?> p_19870_, Level p_19871_) {
		super(p_19870_, p_19871_);
		// TODO Auto-generated constructor stub
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), index = 2, ordinal = 0, argsOnly = true)
	public float pX(float pXIn, Entity pProjectile) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(pProjectile.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			Vec3 vec3 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
			if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !serverviveplayer.isSeated() && serverviveplayer.getDraw() > 0.0F) {
				vec3 = serverviveplayer.getControllerPos(1, (Player) pProjectile).subtract(serverviveplayer.getControllerPos(0, (Player) pProjectile)).normalize();
			}
			return -((float) Math.toDegrees(Math.asin(vec3.y / vec3.length())));
		}
		return pXIn;
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), index = 3, ordinal = 1, argsOnly = true)
	public float pY(float pYIn, Entity pProjectile) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(pProjectile.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			Vec3 vec3 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
			if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !serverviveplayer.isSeated() && serverviveplayer.getDraw() > 0.0F) {
				vec3 = serverviveplayer.getControllerPos(1, (Player) pProjectile).subtract(serverviveplayer.getControllerPos(0, (Player) pProjectile)).normalize();
			}
			return (float) Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
		}
		return pYIn;
	}

	@ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
			at = @At("HEAD"), index = 5, ordinal = 3, argsOnly = true)
	public float pVelocity(float pVelocity, Entity pProjectile) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(pProjectile.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !serverviveplayer.isSeated() && serverviveplayer.getDraw() > 0.0F){
				((AbstractArrow)(Object)this).setBaseDamage(((AbstractArrow)(Object)this).getBaseDamage() * 2.0D);
				return pVelocity * serverviveplayer.getDraw();
			}
		}
		return pVelocity;
	}
}
