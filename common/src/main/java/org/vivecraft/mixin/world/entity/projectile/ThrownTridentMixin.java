package org.vivecraft.mixin.world.entity.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;

@Mixin(ThrownTrident.class)
public class ThrownTridentMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyePosition()Lnet/minecraft/world/phys/Vec3;"), method = "tick()V")
	public Vec3 tick(Entity entity) {
		Vec3 vec3 = entity.getEyePosition();
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(entity.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			vec3 = serverviveplayer.getControllerPos(0, (Player)entity);
		}
		return vec3;
	}
}
