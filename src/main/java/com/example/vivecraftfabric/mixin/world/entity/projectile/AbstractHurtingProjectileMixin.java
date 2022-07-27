package com.example.vivecraftfabric.mixin.world.entity.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;

@Mixin(AbstractHurtingProjectile.class)
public class AbstractHurtingProjectileMixin {
	
	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"), method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z" ,
			locals = LocalCapture.CAPTURE_FAILSOFT)
	public void hurtvive(DamageSource pSource, float f, CallbackInfoReturnable<Boolean> info, Entity entity, Vec3 vec3) {
		ServerVivePlayer serverviveplayer = NetworkHelper.vivePlayers.get(pSource.getEntity().getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			vec3 = serverviveplayer.getHMDDir();
		}
	}
}
