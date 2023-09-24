package org.vivecraft.mixin.world.entity.projectile;

import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static org.joml.Math.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {

	protected FishingHookMixin(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
		// TODO Auto-generated constructor stub
	}

	@Unique
	private ServerVivePlayer serverviveplayer;
	@Unique
	private Vec3 controllerDir = null;
	@Unique
	private Vec3 controllerPos = null;

	@ModifyVariable(at = @At("STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 0)
	private float modifyXrot(float xRot, Player player) {
		this.serverviveplayer = ServerVRPlayers.getVivePlayer((ServerPlayer) player);
		if (this.serverviveplayer != null && this.serverviveplayer.isVR()) {
			this.controllerDir = this.serverviveplayer.getControllerDir(this.serverviveplayer.activeHand);
			this.controllerPos = this.serverviveplayer.getControllerPos(this.serverviveplayer.activeHand, player);
			return -((float) toDegrees(asin(this.controllerDir.y / this.controllerDir.length())));
		}
		return xRot;
	}
	@ModifyVariable(at = @At("STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 1)
	private float modifyYrot(float yRot) {
		if (this.serverviveplayer != null && this.serverviveplayer.isVR()) {
			return (float) toDegrees(atan2(-this.controllerDir.x, this.controllerDir.z));
		}
		return yRot;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;moveTo(DDDFF)V"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
	private void modifyMoveTo(FishingHook instance, double x, double y, double z, float yRot, float xRot) {
		if (this.serverviveplayer != null && this.serverviveplayer.isVR()) {
			instance.moveTo(this.controllerPos.x + this.controllerDir.x * (double)0.6F, this.controllerPos.y + this.controllerDir.y * (double)0.6F, this.controllerPos.z + this.controllerDir.z * (double)0.6F, yRot, xRot);
			this.controllerDir = null;
			this.controllerPos = null;
		} else {
			this.moveTo(x, y, z, yRot, xRot);
		}

		this.serverviveplayer = null;
	}
}