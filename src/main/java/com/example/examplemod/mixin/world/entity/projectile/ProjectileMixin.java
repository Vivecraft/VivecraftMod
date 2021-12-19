package com.example.examplemod.mixin.world.entity.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity{
	
	public ProjectileMixin(EntityType<?> p_19870_, Level p_19871_) {
		super(p_19870_, p_19871_);
		// TODO Auto-generated constructor stub
	}

	@Inject(at = @At("HEAD"), method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V")
	public void shoot(Entity pProjectile, float pX, float pY, float pZ, float pVelocity, float pInaccuracy, CallbackInfo info) {
		ServerVivePlayer serverviveplayer = NetworkHelper.vivePlayers.get(pProjectile.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()){
			Vec3 vec3 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
			if ((Projectile)(Object)this instanceof AbstractArrow && !((Projectile)(Object)this instanceof ThrownTrident) && !serverviveplayer.isSeated() && serverviveplayer.getDraw() > 0.0F){
				vec3 = serverviveplayer.getControllerPos(1, (Player)pProjectile).subtract(serverviveplayer.getControllerPos(0, (Player)pProjectile)).normalize();
				pVelocity *= serverviveplayer.getDraw();
				((AbstractArrow)(Object)this).setBaseDamage(((AbstractArrow)(Object)this).getBaseDamage() * 2.0D);
			}
			pX = -((float)Math.toDegrees(Math.asin(vec3.y / vec3.length())));
			pY = (float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
		}
	}
	

}
