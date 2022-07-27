package com.example.vivecraftfabric.mixin.client.player;

import com.example.vivecraftfabric.ItemInHandRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.gameplay.VRPlayer;

import com.example.vivecraftfabric.DataHolder;
import com.example.vivecraftfabric.PlayerExtension;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.render.VRFirstPersonArmSwing;
import org.vivecraft.utils.external.jinfinadeck;
import org.vivecraft.utils.external.jkatvr;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerVRMixin extends AbstractClientPlayer implements PlayerExtension{

	@Unique
	private Vec3 moveMulIn = Vec3.ZERO;
	@Unique
	private boolean initFromServer;
	@Unique
	private int movementTeleportTimer;
	@Unique
	private double additionX;
	@Unique
	private double additionZ;
	@Final
	@Shadow
	protected Minecraft minecraft;
	@Shadow
	@Final
	public ClientPacketListener connection;
	private final DataHolder dataholder = DataHolder.getInstance();

	@Shadow
	protected abstract void updateAutoJump(float f, float g);

	@Shadow
	public abstract void swing(InteractionHand interactionHand);

	public LocalPlayerVRMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
		// TODO Auto-generated constructor stub
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V"), method = "tick")
	public void overridePose(CallbackInfo ci) {
		NetworkHelper.overridePose(this);
		DataHolder.getInstance().vrPlayer.doPermanantLookOverride((LocalPlayer) (Object)this, DataHolder.getInstance().vrPlayer.vrdata_world_pre);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"), method = "aiStep")
	public void ai(CallbackInfo ci) {
		this.dataholder.vrPlayer.tick((LocalPlayer) (Object) this, this.minecraft, this.random);
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
		if (!this.swinging) {
			if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
				((ItemInHandRendererExtension) this.minecraft.getItemInHandRenderer()).setXdist((float) this.minecraft.hitResult.getLocation().subtract(DataHolder.getInstance().vrPlayer.vrdata_world_pre.getController(interactionHand.ordinal()).getPosition()).length());
			} else {
				((ItemInHandRendererExtension) this.minecraft.getItemInHandRenderer()).setXdist(0F);
			}
		}
	}

	@Override
	public void swingArm(InteractionHand interactionhand, VRFirstPersonArmSwing interact) {
		((ItemInHandRendererExtension)this.minecraft.getItemInHandRenderer()).setSwingType(interact);
		this.swing(interactionhand);
	}

	public void moveTo(double pX, double p_20109_, double pY, float p_20111_, float pZ) {
		super.moveTo(pX, p_20109_, pY, p_20111_, pZ);
		if (this.initFromServer) {
			DataHolder.getInstance().vrPlayer.snapRoomOriginToPlayerEntity((LocalPlayer)(Object)this, false, false);
		}
	}

	public void absMoveTo(double pX, double p_19892_, double pY, float p_19894_, float pZ) {
		super.absMoveTo(pX, p_19892_, pY, p_19894_, pZ);
		DataHolder.getInstance().vrPlayer.snapRoomOriginToPlayerEntity((LocalPlayer)(Object)this, false, false);
		if (!this.initFromServer)
		{
			this.moveTo(pX, p_19892_, pY, p_19894_, pZ);
			this.initFromServer = true;
		}
	}

	public void doDrag() {
		float f = 0.91F;
		if (this.onGround) {
			f = this.level.getBlockState(new BlockPos(this.getX(), this.getBoundingBox().minY - 1.0D, this.getZ())).getBlock().getFriction() * 0.91F;
		}
		double d0 = (double)f;
		double d1 = (double)f;
		this.setDeltaMovement(this.getDeltaMovement().x / d0, this.getDeltaMovement().y, this.getDeltaMovement().z / d1);
		double d2 = dataholder.vrSettings.inertiaFactor.getFactor();
		double d3 = this.getBoundedAddition(this.additionX);
		double d4 = (double)f * d3 / (double)(1.0F - f);
		double d5 = d4 / ((double)f * (d4 + d3 * d2));
		d0 = d0 * d5;
		double d6 = this.getBoundedAddition(this.additionZ);
		double d7 = (double)f * d6 / (double)(1.0F - f);
		double d8 = d7 / ((double)f * (d7 + d6 * d2));
		d1 = d1 * d8;
		this.setDeltaMovement(this.getDeltaMovement().x * d0, this.getDeltaMovement().y, this.getDeltaMovement().z * d1);
	}

	public double getBoundedAddition(double orig) {
		return orig >= -1.0E-6D && orig <= 1.0E-6D ? 1.0E-6D : orig;
	}

	public void moveRelative(float pAmount, Vec3 pRelative) {
		double d0 = pRelative.y;
		double d1 = pRelative.x;
		double d2 = pRelative.z;
		VRPlayer vrplayer = this.dataholder.vrPlayer;

		if (vrplayer.getFreeMove()) {
			double d3 = d1 * d1 + d2 * d2;
			double d4 = 0.0D;
			double d5 = 0.0D;
			double d6 = 0.0D;
			double d7 = 1.0D;

			if (d3 >= (double)1.0E-4F || DataHolder.katvr) {
				d3 = (double) Mth.sqrt((float) d3);

				if (d3 < 1.0D && !DataHolder.katvr) {
					d3 = 1.0D;
				}

				d3 = (double)pAmount / d3;
				d1 = d1 * d3;
				d2 = d2 * d3;
				Vec3 vec3 = new Vec3(d1, 0.0D, d2);
				VRPlayer vrplayer1 = this.dataholder.vrPlayer;
				boolean flag = !this.isPassenger() && (this.getAbilities().flying || this.isSwimming());

				if (DataHolder.katvr) {
					jkatvr.query();
					d3 = (double)(jkatvr.getSpeed() * jkatvr.walkDirection() * this.dataholder.vrSettings.movementSpeedMultiplier);
					vec3 = new Vec3(0.0D, 0.0D, d3);

					if (flag) {
						vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float)Math.PI / 180F));
					}

					vec3 = vec3.yRot(-jkatvr.getYaw() * ((float)Math.PI / 180F) + this.dataholder.vrPlayer.vrdata_world_pre.rotation_radians);
				}
				else if (DataHolder.infinadeck) {
					jinfinadeck.query();
					d3 = (double)(jinfinadeck.getSpeed() * jinfinadeck.walkDirection() * this.dataholder.vrSettings.movementSpeedMultiplier);
					vec3 = new Vec3(0.0D, 0.0D, d3);

					if (flag) {
						vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float)Math.PI / 180F));
					}

					vec3 = vec3.yRot(-jinfinadeck.getYaw() * ((float)Math.PI / 180F) + this.dataholder.vrPlayer.vrdata_world_pre.rotation_radians);
				}
				else if (this.dataholder.vrSettings.seated) {
					int j = 0;
					if (this.dataholder.vrSettings.seatedUseHMD) {
						j = 1;
					}

					if (flag) {
						vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.getController(j).getPitch() * ((float)Math.PI / 180F));
					}

					vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.getController(j).getYaw() * ((float)Math.PI / 180F));
				}
				else {
					if (flag) {
						switch (this.dataholder.vrSettings.vrFreeMoveMode) {
							case CONTROLLER:
								vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.getController(1).getPitch() * ((float)Math.PI / 180F));
								break;
							case HMD:
							case RUN_IN_PLACE:
							case ROOM:
								vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float)Math.PI / 180F));
						}
					}
					if (this.dataholder.jumpTracker.isjumping()) {
						vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.hmd.getYaw() * ((float)Math.PI / 180F));
					}
					else {
						switch (this.dataholder.vrSettings.vrFreeMoveMode)                         {
							case CONTROLLER:
								vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.getController(1).getYaw() * ((float)Math.PI / 180F));
								break;

							case HMD:
								vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.hmd.getYaw() * ((float)Math.PI / 180F));
								break;

				            case RUN_IN_PLACE:
								vec3 = vec3.yRot((float)(-this.dataholder.runTracker.getYaw() * (double)((float)Math.PI / 180F)));
								vec3 = vec3.scale(this.dataholder.runTracker.getSpeed());

							case ROOM:
								vec3 = vec3.yRot((180.0F + this.dataholder.vrSettings.worldRotation) * ((float)Math.PI / 180F));
						}
					}
				}

				d4 = vec3.x;
				d6 = vec3.y;
				d5 = vec3.z;

				if (!this.getAbilities().flying && !this.wasTouchingWater) {
					d7 = this.dataholder.vrSettings.inertiaFactor.getFactor();
				}

				float f = 1.0F;

				if (this.getAbilities().flying) {
					f = 5.0F;
				}

				this.setDeltaMovement(this.getDeltaMovement().x + d4 * d7, this.getDeltaMovement().y + d6 * (double)f, this.getDeltaMovement().z + d5 * d7);
				this.additionX = d4;
				this.additionZ = d5;
			}

			if (!this.getAbilities().flying && !this.wasTouchingWater)
			{
				this.doDrag();
			}
		}
	}
	private boolean isThePlayer() {
		return (LocalPlayer) (Object)this == Minecraft.getInstance().player;
	}

	@Override
	public boolean getInitFromServer() {
		return this.initFromServer;
	}

	@Override
	public void setMovementTeleportTimer(int value) {
		this.movementTeleportTimer = value;
	}

	@Override
	public int getMovementTeleportTimer() {
		return movementTeleportTimer;
	}

	@Override
	public float getMuhSpeedFactor() {
		return this.moveMulIn.lengthSqr() > 0.0D ? (float)((double)this.getBlockSpeedFactor() * (this.moveMulIn.x + this.moveMulIn.z) / 2.0D) : this.getBlockSpeedFactor();
	}

	@Override
	public float getMuhJumpFactor() {
		return this.moveMulIn.lengthSqr() > 0.0D ? (float)((double)this.getBlockJumpFactor() * this.moveMulIn.y) : this.getBlockJumpFactor();
	}

	@Override
	public void stepSound(BlockPos blockforNoise, Vec3 soundPos) {
		BlockState blockstate = this.level.getBlockState(blockforNoise);
		Block block = blockstate.getBlock();
		SoundType soundtype = block.getSoundType(blockstate);
		BlockState blockstate1 = this.level.getBlockState(blockforNoise.above());

		if (blockstate1.getBlock() == Blocks.SNOW) {
			soundtype = Blocks.SNOW.getSoundType(blockstate1);
		}

		float f = soundtype.getVolume();
		float f1 = soundtype.getPitch();
		SoundEvent soundevent = soundtype.getStepSound();

		if (!this.isSilent() && !block.defaultBlockState().getMaterial().isLiquid()) {
			this.level.playSound((LocalPlayer)null, soundPos.x, soundPos.y, soundPos.z, soundevent, this.getSoundSource(), f, f1);
		}
	}
}
