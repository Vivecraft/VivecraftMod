package com.example.examplemod.mixin.client.player;

import com.example.examplemod.ItemInHandRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.gameplay.VRPlayer;

import com.example.examplemod.DataHolder;
import com.example.examplemod.PlayerExtension;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.render.VRFirstPersonArmSwing;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerVRMixin extends AbstractClientPlayer implements PlayerExtension{

	@Unique
	private Vec3 moveMulIn;
	@Shadow
	private Minecraft minecraft;

	@Shadow
	@Final
	public ClientPacketListener connection;
	@Shadow
	protected abstract void updateAutoJump(float f, float g);

	@Shadow
	public abstract void swing(InteractionHand interactionHand);

	public LocalPlayerVRMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
		// TODO Auto-generated constructor stub
	}

	@Inject(at = @At("HEAD"), method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", cancellable = true)
	public void overwriteMove(MoverType pType, Vec3 pPos, CallbackInfo info) {
		this.moveMulIn = this.stuckSpeedMultiplier;

		if (pPos.length() != 0.0D && !this.isPassenger()) {
			boolean flag = VRPlayer.get().getFreeMove();
			boolean flag1 = flag || DataHolder.getInstance().vrSettings.simulateFalling && !this.onClimbable()
					&& !this.isShiftKeyDown();

			if (DataHolder.getInstance().climbTracker.isActive((LocalPlayer) (Object) this)
					&& (flag || DataHolder.getInstance().climbTracker.isGrabbingLadder())) {
				flag1 = true;
			}

			Vec3 vec3 = VRPlayer.get().roomOrigin;

			if ((DataHolder.getInstance().climbTracker.isGrabbingLadder() || flag
					|| DataHolder.getInstance().swimTracker.isActive((LocalPlayer) (Object) this))
					&& (this.zza != 0.0F || this.isFallFlying() || Math.abs(this.getDeltaMovement().x) > 0.01D
							|| Math.abs(this.getDeltaMovement().z) > 0.01D)) {
				double d0 = vec3.x - this.getX();
				double d1 = vec3.z - this.getZ();
				double d2 = this.getX();
				double d3 = this.getZ();
				super.move(pType, pPos);

				if (DataHolder.getInstance().vrSettings.walkUpBlocks) {
					this.maxUpStep = this.getBlockJumpFactor() == 1.0F ? 1.0F : 0.6F;
				} else {
					this.maxUpStep = 0.6F;
					this.updateAutoJump((float) (this.getX() - d2), (float) (this.getZ() - d3));
				}

				double d4 = this.getY() + this.getRoomYOffsetFromPose();
				VRPlayer.get().setRoomOrigin(this.getX() + d0, d4, this.getZ() + d1, false);
			} else if (flag1) {
				super.move(pType, new Vec3(0.0D, pPos.y, 0.0D));
				VRPlayer.get().setRoomOrigin(VRPlayer.get().roomOrigin.x, this.getY() + this.getRoomYOffsetFromPose(),
						VRPlayer.get().roomOrigin.z, false);
			} else {
				this.onGround = true;
			}
		} else {
			super.move(pType, pPos);
		}
		info.cancel();
	}
	
	@Override
	public void setPos(double pX, double p_20211_, double pY) {
		double d0 = this.getX();
		double d1 = this.getY();
		double d2 = this.getZ();
		super.setPos(pX, p_20211_, pY);
		double d3 = this.getX();
		double d4 = this.getY();
		double d5 = this.getZ();
		Entity entity = this.getVehicle();

		if (this.isPassenger()) {
			Vec3 vec3 = DataHolder.getInstance().vehicleTracker.Premount_Pos_Room;
			vec3 = vec3.yRot(DataHolder.getInstance().vrPlayer.vrdata_world_pre.rotation_radians);
			pX = pX - vec3.x;
			p_20211_ = DataHolder.getInstance().vehicleTracker.getVehicleFloor(entity, p_20211_);
			pY = pY - vec3.z;
			DataHolder.getInstance().vrPlayer.setRoomOrigin(pX, p_20211_, pY, pX + p_20211_ + pY == 0.0D);
		} else {
			Vec3 vec31 = DataHolder.getInstance().vrPlayer.roomOrigin;
			VRPlayer.get().setRoomOrigin(vec31.x + (d3 - d0), vec31.y + (d4 - d1), vec31.z + (d5 - d2),
					pX + p_20211_ + pY == 0.0D);
		}
	}

	public double getRoomYOffsetFromPose() {
		double d0 = 0.0D;

		if (this.getPose() == Pose.FALL_FLYING || this.getPose() == Pose.SPIN_ATTACK
				|| this.getPose() == Pose.SWIMMING && !DataHolder.getInstance().crawlTracker.crawlsteresis) {
			d0 = -1.2D;
		}

		return d0;
	}

	@Inject(at = @At("HEAD"), method = "swing")
	public void vrSwing(InteractionHand interactionHand, CallbackInfo ci) {
		if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
			((ItemInHandRendererExtension)this.minecraft.getItemInHandRenderer()).setXdist((float) this.minecraft.hitResult.getLocation().subtract(DataHolder.getInstance().vrPlayer.vrdata_world_pre.getController(interactionHand.ordinal()).getPosition()).length());
		} else {
			((ItemInHandRendererExtension)this.minecraft.getItemInHandRenderer()).setXdist(0F);
		}
	}

	@Override
	public void swingArm(InteractionHand interactionhand, VRFirstPersonArmSwing interact) {
		((ItemInHandRendererExtension)this.minecraft.getItemInHandRenderer()).setSwingType(interact);
		this.swing(interactionhand);
	}


}
