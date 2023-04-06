package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"), method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	public Vec3 hurtvive(Entity instance) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(instance.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			return serverviveplayer.getHMDDir();
		}
		return instance.getLookAngle();
	}
}
