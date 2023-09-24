package org.vivecraft.mixin.world.item;

import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

import org.joml.Vector3f;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.common.utils.Utils.up;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(net.minecraft.world.item.CrossbowItem.class)
public class CrossbowItemMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"),
			method = "shootProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V")
	private static Vec3 shoot(LivingEntity livingEntity, float v) {
		Vec3 vec3 = livingEntity.getViewVector(v);
		if (livingEntity instanceof ServerPlayer player) {
			ServerVivePlayer serverviveplayer = ServerVRPlayers.getVivePlayer(player);
			if (serverviveplayer != null && serverviveplayer.isVR()) {
				vec3 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
				serverviveplayer.getControllerVectorCustom(serverviveplayer.activeHand, new Vector3f(up));
			}
		}
		return vec3;
	}
}
