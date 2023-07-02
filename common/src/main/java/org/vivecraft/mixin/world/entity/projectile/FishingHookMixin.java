package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {

	protected FishingHookMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
		super(p_37248_, p_37249_);
		// TODO Auto-generated constructor stub
	}

	@Unique
	private ServerVivePlayer serverviveplayer = null;
	@Unique
	private Vec3 controllerDir = null;
	@Unique
	private Vec3 controllerPos = null;

	@ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 0)
	private float modifyXrot(float xRot, Player player) {
		serverviveplayer = ServerVRPlayers.getVivePlayer((ServerPlayer) player);
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			controllerDir = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
			controllerPos = serverviveplayer.getControllerPos(serverviveplayer.activeHand, player);
			return -((float) Math.toDegrees(Math.asin(controllerDir.y / controllerDir.length())));
		}
		return xRot;
	}
	@ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 1)
	private float modifyYrot(float yRot) {
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			return (float) Math.toDegrees(Math.atan2(-controllerDir.x, controllerDir.z));
		}
		return yRot;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;moveTo(DDDFF)V"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
	private void modifyMoveTo(FishingHook instance, double x, double y, double z, float yRot, float xRot) {
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			instance.moveTo(controllerPos.x + controllerDir.x * (double)0.6F, controllerPos.y + controllerDir.y * (double)0.6F, controllerPos.z + controllerDir.z * (double)0.6F, yRot, xRot);
			controllerDir = null;
			controllerPos = null;
		} else {
			this.moveTo(x, y, z, yRot, xRot);
		}

		serverviveplayer = null;
	}
}