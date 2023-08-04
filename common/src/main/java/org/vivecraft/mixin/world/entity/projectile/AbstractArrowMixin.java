package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.common.utils.Utils;
import org.vivecraft.server.config.ServerConfig;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity {

	protected AbstractArrowMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
		super(p_37248_, p_37249_);
		// TODO Auto-generated constructor stub
	}

	@Shadow
	private double baseDamage;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V")
	public void pickup(EntityType<? extends AbstractArrow> p_36717_, LivingEntity p_36718_, Level p_36719_, CallbackInfo info) {
		if (p_36718_ instanceof ServerPlayer player) {
			ServerVivePlayer serverviveplayer = ServerVRPlayers.getVivePlayer(player);
			if (serverviveplayer != null && serverviveplayer.isVR()) {
				Vec3 vec3 = serverviveplayer.getControllerPos(serverviveplayer.activeHand, (Player) p_36718_);
				Vec3 vec31 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);

				if (!serverviveplayer.isSeated() && serverviveplayer.getDraw() > 0.0F) {
					vec31 = serverviveplayer.getControllerPos(1, (Player) p_36718_).subtract(serverviveplayer.getControllerPos(0, (Player) p_36718_)).normalize();
					vec3 = serverviveplayer.getControllerPos(0, (Player) p_36718_);
				}

				this.setPos(vec3.x + vec31.x, vec3.y + vec31.y, vec3.z + vec31.z);
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "onHitEntity")
	public void damageMultiplier(EntityHitResult entityHitResult, CallbackInfo ci) {
		if (((Projectile)(Object)this).getOwner() instanceof ServerPlayer owner) {
			ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(owner);
			Vec3 hitpos;
			double multiplier = 1.0;
			if ((hitpos = isHeadshot(entityHitResult)) != null) {
				if (serverVivePlayer != null && serverVivePlayer.isVR()) {
					if (serverVivePlayer.isSeated()) {
						multiplier = baseDamage * ServerConfig.bowSeatedHeadshotMultiplier.get();
					} else {
						multiplier = baseDamage * ServerConfig.bowStandingHeadshotMultiplier.get();
					}
				} else {
					multiplier = baseDamage * ServerConfig.bowVanillaHeadshotMultiplier.get();
				}

				if (multiplier > 1.0) {
					// send headshot particles
					((ServerLevel)this.level).sendParticles(
						owner,
						ParticleTypes.CRIT,
						true, // always render the hit particles on the client
						hitpos.x,
						hitpos.y,
						hitpos.z,
						5,
						- this.getDeltaMovement().x,
						- this.getDeltaMovement().y,
						- this.getDeltaMovement().z,
						0.1);
					// send sound effect
					owner.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ITEM_BREAK), SoundSource.PLAYERS, owner.getX(), owner.getY(), owner.getZ(), 0.7f, 0.5f, owner.level.random.nextLong()));
				}
			}
			// if headshots are disabled, still use the regular multiplier
			if (serverVivePlayer != null) {
				if (serverVivePlayer.isSeated()) {
					multiplier = Math.max(multiplier, ServerConfig.bowSeatedMultiplier.get());
				} else {
					multiplier = Math.max(multiplier, ServerConfig.bowStandingMultiplier.get());
				}
			}

			baseDamage *= multiplier;
		}
	}

	@Unique
	// checks if the hit was a headshot, and returns the hit position, if it was, null otherwise
	private Vec3 isHeadshot(EntityHitResult hit) {
		AABB headBox;
		if ((headBox = Utils.getEntityHeadHitbox(hit.getEntity(), 0.3)) != null) {
			Vec3 originalHitpos = hit.getEntity()
				.getBoundingBox()
				.clip(this.position(), this.position().add(this.getDeltaMovement().scale(2.0)))
				.orElse(this.position().add(this.getDeltaMovement()));
			return headBox
				.clip(this.position(), originalHitpos)
				.orElse(headBox.contains(this.position()) ? this.position() : null);
		}
		return null;
	}
}
