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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.oldapi.CommonNetworkHelper;
import org.vivecraft.api.oldapi.ServerVivePlayer;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity{

	@Shadow
	public abstract void shoot(double f2, double f, double f1, float pVelocity, float pInaccuracy);

	public ProjectileMixin(EntityType<?> p_19870_, Level p_19871_) {
		super(p_19870_, p_19871_);
		// TODO Auto-generated constructor stub
	}

	@Inject(at = @At("HEAD"), method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V", cancellable = true)
	public void shoot(Entity pProjectile, float pX, float pY, float pZ, float pVelocity, float pInaccuracy, CallbackInfo info) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(pProjectile.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()){
			Vec3 vec3 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
			if (((Projectile) (Object) this) instanceof AbstractArrow && !(((Projectile) (Object) this) instanceof ThrownTrident) && !serverviveplayer.isSeated() && serverviveplayer.getDraw() > 0.0F){
				vec3 = serverviveplayer.getControllerPos(1, (Player)pProjectile).subtract(serverviveplayer.getControllerPos(0, (Player)pProjectile)).normalize();
				pVelocity *= serverviveplayer.getDraw();
				((AbstractArrow)(Object)this).setBaseDamage(((AbstractArrow)(Object)this).getBaseDamage() * 2.0D);
			}
			pX = -((float)Math.toDegrees(Math.asin(vec3.y / vec3.length())));
			pY = (float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
		}
		float f2 = -Mth.sin(pY * ((float)Math.PI / 180F)) * Mth.cos(pX * ((float)Math.PI / 180F));
		float f = -Mth.sin((pX + pZ) * ((float)Math.PI / 180F));
		float f1 = Mth.cos(pY * ((float)Math.PI / 180F)) * Mth.cos(pX * ((float)Math.PI / 180F));
		this.shoot((double)f2, (double)f, (double)f1, pVelocity, pInaccuracy);
		Vec3 vec31 = pProjectile.getDeltaMovement();
		this.setDeltaMovement(this.getDeltaMovement().add(vec31.x, pProjectile.isOnGround() ? 0.0D : vec31.y, vec31.z));
		info.cancel();
	}

}
