package org.vivecraft.mixin.client.player;

import net.minecraft.world.level.Level;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.extensions.ItemInHandRendererExtension;
import org.vivecraft.extensions.PlayerExtension;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.ClientNetworkHelper;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.render.VRFirstPersonArmSwing;
import org.vivecraft.utils.external.jinfinadeck;
import org.vivecraft.utils.external.jkatvr;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.stream.StreamSupport;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerVRMixin extends AbstractClientPlayer implements PlayerExtension {

	@Unique
	private Vec3 moveMulIn = Vec3.ZERO;
	@Unique
	private boolean initFromServer;
	@Unique
	private int movementTeleportTimer;
	@Unique
	public String lastMsg = null;
	@Unique
	private boolean snapReq;
	@Unique
	private boolean teleported;
	@Unique
	private double additionX;
	@Unique
	private double additionZ;
	@Final
	@Shadow
	protected Minecraft minecraft;
	@Shadow
	private boolean startedUsingItem;
	@Shadow
	@Final
	public ClientPacketListener connection;
	private final ClientDataHolder dataholder = ClientDataHolder.getInstance();
	@Shadow
	public double xLast;
	@Shadow
	public double yLast1;
	@Shadow
	public double zLast;
	@Shadow
	public float yRotLast;
	@Shadow
	public float xRotLast;
	@Shadow
	public boolean lastOnGround;
	@Shadow
	public boolean wasShiftKeyDown;
	@Shadow
	public boolean wasSprinting;
	@Shadow
	public int positionReminder;
	@Shadow
	private InteractionHand usingItemHand;
	@Shadow
	private int autoJumpTime;
	@Shadow
	public Input input;

	public LocalPlayerVRMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Shadow
	protected abstract void updateAutoJump(float f, float g);

	@Shadow
	public abstract void swing(InteractionHand interactionHand);

	@Inject(at = @At("TAIL"), method = "startRiding")
	public void startRidingTracker(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
		ClientDataHolder.getInstance().vehicleTracker.onStartRiding(entity, (LocalPlayer) (Object) this);
		this.snapReq = true;
	}

	@Inject(at = @At("TAIL"), method = "removeVehicle")
	public void stopRidingTracker(CallbackInfo ci) {
		ClientDataHolder.getInstance().vehicleTracker.onStopRiding((LocalPlayer) (Object) this);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.BEFORE), method = "tick")
	public void overrideLookPre(CallbackInfo ci) {
		ClientDataHolder.getInstance().vrPlayer.doPermanantLookOverride((LocalPlayer) (Object) this, ClientDataHolder.getInstance().vrPlayer.vrdata_world_pre);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER), method = "tick")
	public void overridePose(CallbackInfo ci) {
		ClientNetworkHelper.overridePose((LocalPlayer) (Object) this);
		ClientDataHolder.getInstance().vrPlayer.doPermanantLookOverride((LocalPlayer) (Object) this, ClientDataHolder.getInstance().vrPlayer.vrdata_world_pre);
	}

	/* TODO: not working, is this needed?
	//TODO verify
	@ModifyVariable(at = @At("STORE"), ordinal = 3, method = "sendPosition")
	public boolean changeFlag(boolean b) {
		return this.teleported || b;
	}
	*/

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z", shift = At.Shift.BEFORE), method = "sendPosition")
	public void passenger(CallbackInfo ci) {
		if (this.teleported) {
			this.teleported = false;
			// flag2 = true; TODO?
			ByteBuf bytebuf = Unpooled.buffer();
			bytebuf.writeFloat((float) this.getX());
			bytebuf.writeFloat((float) this.getY());
			bytebuf.writeFloat((float) this.getZ());
			byte[] abyte = new byte[bytebuf.readableBytes()];
			bytebuf.readBytes(abyte);
			ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworkHelper.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.TELEPORT, abyte);
			this.connection.send(serverboundcustompayloadpacket);
		}
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;lastOnGround:Z", shift = At.Shift.AFTER, ordinal = 1), method = "sendPosition")
	public void walkUp(CallbackInfo ci) {
		if (ClientDataHolder.getInstance().vrSettings.walkUpBlocks) {
			this.minecraft.options.autoJump().set(false);
		}
	}

	@Override
	public void swingArm(InteractionHand interactionhand, VRFirstPersonArmSwing interact) {
		((ItemInHandRendererExtension) this.minecraft.getEntityRenderDispatcher().getItemInHandRenderer()).setSwingType(interact);
		this.swing(interactionhand);
	}


	@Inject(at = @At("HEAD"), method = "swing")
	public void vrSwing(InteractionHand interactionHand, CallbackInfo ci) {
		if (!this.swinging) {
			if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
				((ItemInHandRendererExtension) this.minecraft.getEntityRenderDispatcher().getItemInHandRenderer()).setXdist((float) this.minecraft.hitResult.getLocation().subtract(ClientDataHolder.getInstance().vrPlayer.vrdata_world_pre.getController(interactionHand.ordinal()).getPosition()).length());
			} else {
				((ItemInHandRendererExtension) this.minecraft.getEntityRenderDispatcher().getItemInHandRenderer()).setXdist(0F);
			}
		}
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
			boolean flag1 = flag || ClientDataHolder.getInstance().vrSettings.simulateFalling && !this.onClimbable()
					&& !this.isShiftKeyDown();

			if (ClientDataHolder.getInstance().climbTracker.isActive((LocalPlayer) (Object) this)
					&& (flag || ClientDataHolder.getInstance().climbTracker.isGrabbingLadder())) {
				flag1 = true;
			}

			Vec3 vec3 = VRPlayer.get().roomOrigin;

			if ((ClientDataHolder.getInstance().climbTracker.isGrabbingLadder() || flag
					|| ClientDataHolder.getInstance().swimTracker.isActive((LocalPlayer) (Object) this))
					&& (this.zza != 0.0F || this.isFallFlying() || Math.abs(this.getDeltaMovement().x) > 0.01D
					|| Math.abs(this.getDeltaMovement().z) > 0.01D)) {
				double d0 = vec3.x - this.getX();
				double d1 = vec3.z - this.getZ();
				double d2 = this.getX();
				double d3 = this.getZ();
				super.move(pType, pPos);

				if (ClientDataHolder.getInstance().vrSettings.walkUpBlocks) {
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
	public double getRoomYOffsetFromPose() {
		double d0 = 0.0D;

		if (this.getPose() == Pose.FALL_FLYING || this.getPose() == Pose.SPIN_ATTACK
				|| this.getPose() == Pose.SWIMMING && !ClientDataHolder.getInstance().crawlTracker.crawlsteresis) {
			d0 = -1.2D;
		}

		return d0;
	}

//	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"), method = "updateAutoJump")
//	public float yRot(LocalPlayer instance) {
//		return DataHolder.getInstance().vrPlayer.vrdata_world_pre.getBodyYaw();
//	}

	@Inject(at = @At("HEAD"), method = "updateAutoJump", cancellable = true)
	public void autostep1(float f, float g, CallbackInfo ci) {
		float l;
		if (!this.canAutoJump()) {
			ci.cancel();
			return;
		}
		Vec3 vec3 = this.position();
		Vec3 vec32 = vec3.add(f, 0.0, g);
		Vec3 vec33 = new Vec3(f, 0.0, g);
		float h = this.getSpeed();
		float i = (float)vec33.lengthSqr();
		if (i <= 0.001f) {
			Vec2 vec2 = this.input.getMoveVector();
			float j = h * vec2.x;
			float k = h * vec2.y;
			l = Mth.sin(ClientDataHolder.getInstance().vrPlayer.vrdata_world_pre.getBodyYaw() * ((float)Math.PI / 180));
			float m = Mth.cos(ClientDataHolder.getInstance().vrPlayer.vrdata_world_pre.getBodyYaw() * ((float)Math.PI / 180));
			vec33 = new Vec3(j * m - k * l, vec33.y, k * m + j * l);
			i = (float)vec33.lengthSqr();
			if (i <= 0.001f) {
				ci.cancel();
				return;
			}
		}
		float vec2 = Mth.fastInvSqrt(i);
		Vec3 j = vec33.scale(vec2);
		Vec3 k = this.getForward();
		l = (float)(k.x * j.x + k.z * j.z);
		if (l < -0.15f) {
			ci.cancel();
			return;
		}
		CollisionContext m = CollisionContext.of(this);
		BlockPos blockPos = new BlockPos(this.getX(), this.getBoundingBox().maxY, this.getZ());
		BlockState blockState = this.level.getBlockState(blockPos);
		if (!blockState.getCollisionShape(this.level, blockPos, m).isEmpty()) {
			ci.cancel();
			return;
		}
		BlockState blockState2 = this.level.getBlockState(blockPos = blockPos.above());
		if (!blockState2.getCollisionShape(this.level, blockPos, m).isEmpty()) {
			ci.cancel();
			return;
		}
		float n = 7.0f;
		float o = 1.2f;
		if (this.hasEffect(MobEffects.JUMP)) {
			o += (float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.75f;
		}
		float p = Math.max(h * 7.0f, 1.0f / vec2);
		Vec3 vec34 = vec3;
		Vec3 vec35 = vec32.add(j.scale(p));
		float q = this.getBbWidth();
		float r = this.getBbHeight();
		AABB aABB = new AABB(vec34, vec35.add(0.0, r, 0.0)).inflate(q, 0.0, q);
		vec34 = vec34.add(0.0, 0.51f, 0.0);
		vec35 = vec35.add(0.0, 0.51f, 0.0);
		Vec3 vec36 = j.cross(new Vec3(0.0, 1.0, 0.0));
		Vec3 vec37 = vec36.scale(q * 0.5f);
		Vec3 vec38 = vec34.subtract(vec37);
		Vec3 vec39 = vec35.subtract(vec37);
		Vec3 vec310 = vec34.add(vec37);
		Vec3 vec311 = vec35.add(vec37);
		Iterable<VoxelShape> iterable = this.level.getCollisions(this, aABB);
		Iterator iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap(voxelShape -> voxelShape.toAabbs().stream()).iterator();
		float s = Float.MIN_VALUE;
		while (iterator.hasNext()) {
			AABB aABB2 = (AABB)iterator.next();
			if (!aABB2.intersects(vec38, vec39) && !aABB2.intersects(vec310, vec311)) continue;
			s = (float)aABB2.maxY;
			Vec3 vec312 = aABB2.getCenter();
			BlockPos blockPos2 = new BlockPos(vec312);
			int t = 1;
			while ((float)t < p) {
				BlockState blockState4;
				BlockPos blockPos3 = blockPos2.above(t);
				BlockState blockState3 = this.level.getBlockState(blockPos3);
				VoxelShape voxelShape2 = blockState3.getCollisionShape(this.level, blockPos3, m);
				if (!voxelShape2.isEmpty() && (double)(s = (float)voxelShape2.max(Direction.Axis.Y) + (float)blockPos3.getY()) - this.getY() > (double)p) {
					ci.cancel();
					return;
				}
				if (t > 1 && !(blockState4 = this.level.getBlockState(blockPos = blockPos.above())).getCollisionShape(this.level, blockPos, m).isEmpty()) {
					ci.cancel();
					return;
				}
				++t;
			}
			break;
		}
		if (s == Float.MIN_VALUE) {
			ci.cancel();
			return;
		}
		float aABB2 = (float)((double)s - this.getY());
		if (aABB2 <= 0.5f || aABB2 > p) {
			ci.cancel();
			return;
		}
		this.autoJumpTime = 1;
		ci.cancel();
	}

	@Override
	public ItemStack eat(Level level, ItemStack itemStack)
	{
		if (itemStack.isEdible() && ((LocalPlayer)(Object)this) == Minecraft.getInstance().player && itemStack.getHoverName().getString().equals("EAT ME"))
		{
			ClientDataHolder.getInstance().vrPlayer.wfMode = 0.5D;
			ClientDataHolder.getInstance().vrPlayer.wfCount = 400;
		}
		return super.eat(level, itemStack);
	}

	@Shadow
	protected abstract boolean canAutoJump();

	@Override
	public void moveTo(double pX, double p_20109_, double pY, float p_20111_, float pZ) {
		super.moveTo(pX, p_20109_, pY, p_20111_, pZ);
		if (this.initFromServer) {
			ClientDataHolder.getInstance().vrPlayer.snapRoomOriginToPlayerEntity((LocalPlayer) (Object) this, false, false);
		}
	}

	@Override
	public void absMoveTo(double pX, double p_19892_, double pY, float p_19894_, float pZ) {
		super.absMoveTo(pX, p_19892_, pY, p_19894_, pZ);
		ClientDataHolder.getInstance().vrPlayer.snapRoomOriginToPlayerEntity((LocalPlayer) (Object) this, false, false);
	}

	@Override
	public void setPos(double pX, double p_20211_, double pY) {
		if (!this.initFromServer) {
			this.initFromServer = true;
		}
		double d0 = this.getX();
		double d1 = this.getY();
		double d2 = this.getZ();
		super.setPos(pX, p_20211_, pY);
		double d3 = this.getX();
		double d4 = this.getY();
		double d5 = this.getZ();
		Entity entity = this.getVehicle();

		if (this.isPassenger()) {
			Vec3 vec3 = ClientDataHolder.getInstance().vehicleTracker.Premount_Pos_Room;
			vec3 = vec3.yRot(ClientDataHolder.getInstance().vrPlayer.vrdata_world_pre.rotation_radians);
			pX = pX - vec3.x;
			p_20211_ = ClientDataHolder.getInstance().vehicleTracker.getVehicleFloor(entity, p_20211_);
			pY = pY - vec3.z;
			ClientDataHolder.getInstance().vrPlayer.setRoomOrigin(pX, p_20211_, pY, pX + p_20211_ + pY == 0.0D);
		} else {
			Vec3 vec31 = ClientDataHolder.getInstance().vrPlayer.roomOrigin;
			VRPlayer.get().setRoomOrigin(vec31.x + (d3 - d0), vec31.y + (d4 - d1), vec31.z + (d5 - d2),
					pX + p_20211_ + pY == 0.0D);
		}
	}

	public void doDrag() {
		float f = 0.91F;
		if (this.onGround) {
			f = this.level.getBlockState(new BlockPos(this.getX(), this.getBoundingBox().minY - 1.0D, this.getZ())).getBlock().getFriction() * 0.91F;
		}
		double d0 = (double) f;
		double d1 = (double) f;
		this.setDeltaMovement(this.getDeltaMovement().x / d0, this.getDeltaMovement().y, this.getDeltaMovement().z / d1);
		double d2 = dataholder.vrSettings.inertiaFactor.getFactor();
		double d3 = this.getBoundedAddition(this.additionX);
		double d4 = (double) f * d3 / (double) (1.0F - f);
		double d5 = d4 / ((double) f * (d4 + d3 * d2));
		d0 = d0 * d5;
		double d6 = this.getBoundedAddition(this.additionZ);
		double d7 = (double) f * d6 / (double) (1.0F - f);
		double d8 = d7 / ((double) f * (d7 + d6 * d2));
		d1 = d1 * d8;
		this.setDeltaMovement(this.getDeltaMovement().x * d0, this.getDeltaMovement().y, this.getDeltaMovement().z * d1);
	}

	public double getBoundedAddition(double orig) {
		return orig >= -1.0E-6D && orig <= 1.0E-6D ? 1.0E-6D : orig;
	}

	@Override
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

			if (d3 >= (double) 1.0E-4F || ClientDataHolder.katvr) {
				d3 = (double) Mth.sqrt((float) d3);

				if (d3 < 1.0D && !ClientDataHolder.katvr) {
					d3 = 1.0D;
				}

				d3 = (double) pAmount / d3;
				d1 = d1 * d3;
				d2 = d2 * d3;
				Vec3 vec3 = new Vec3(d1, 0.0D, d2);
				VRPlayer vrplayer1 = this.dataholder.vrPlayer;
				boolean flag = !this.isPassenger() && (this.getAbilities().flying || this.isSwimming());

				if (ClientDataHolder.katvr) {
					jkatvr.query();
					d3 = (double) (jkatvr.getSpeed() * jkatvr.walkDirection() * this.dataholder.vrSettings.movementSpeedMultiplier);
					vec3 = new Vec3(0.0D, 0.0D, d3);

					if (flag) {
						vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float) Math.PI / 180F));
					}

					vec3 = vec3.yRot(-jkatvr.getYaw() * ((float) Math.PI / 180F) + this.dataholder.vrPlayer.vrdata_world_pre.rotation_radians);
				} else if (ClientDataHolder.infinadeck) {
					jinfinadeck.query();
					d3 = (double) (jinfinadeck.getSpeed() * jinfinadeck.walkDirection() * this.dataholder.vrSettings.movementSpeedMultiplier);
					vec3 = new Vec3(0.0D, 0.0D, d3);

					if (flag) {
						vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float) Math.PI / 180F));
					}

					vec3 = vec3.yRot(-jinfinadeck.getYaw() * ((float) Math.PI / 180F) + this.dataholder.vrPlayer.vrdata_world_pre.rotation_radians);
				} else if (this.dataholder.vrSettings.seated) {
					int j = 0;
					if (this.dataholder.vrSettings.seatedUseHMD) {
						j = 1;
					}

					if (flag) {
						vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.getController(j).getPitch() * ((float) Math.PI / 180F));
					}

					vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.getController(j).getYaw() * ((float) Math.PI / 180F));
				} else {
					if (flag) {
						switch (this.dataholder.vrSettings.vrFreeMoveMode) {
							case CONTROLLER:
								vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.getController(1).getPitch() * ((float) Math.PI / 180F));
								break;
							case HMD:
							case RUN_IN_PLACE:
							case ROOM:
								vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float) Math.PI / 180F));
						}
					}
					if (this.dataholder.jumpTracker.isjumping()) {
						vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.hmd.getYaw() * ((float) Math.PI / 180F));
					} else {
						switch (this.dataholder.vrSettings.vrFreeMoveMode) {
							case CONTROLLER:
								vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.getController(1).getYaw() * ((float) Math.PI / 180F));
								break;

							case HMD:
								vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.hmd.getYaw() * ((float) Math.PI / 180F));
								break;

							case RUN_IN_PLACE:
								vec3 = vec3.yRot((float) (-this.dataholder.runTracker.getYaw() * (double) ((float) Math.PI / 180F)));
								vec3 = vec3.scale(this.dataholder.runTracker.getSpeed());

							case ROOM:
								vec3 = vec3.yRot((180.0F + this.dataholder.vrSettings.worldRotation) * ((float) Math.PI / 180F));
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

				this.setDeltaMovement(this.getDeltaMovement().x + d4 * d7, this.getDeltaMovement().y + d6 * (double) f, this.getDeltaMovement().z + d5 * d7);
				this.additionX = d4;
				this.additionZ = d5;
			}

			if (!this.getAbilities().flying && !this.wasTouchingWater) {
				this.doDrag();
			}
		}
	}

	@Override
	public void die(DamageSource pCause) {
		super.die(pCause);
		ClientDataHolder.getInstance().vr.triggerHapticPulse(0, 2000);
		ClientDataHolder.getInstance().vr.triggerHapticPulse(1, 2000);
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
		return this.moveMulIn.lengthSqr() > 0.0D ? (float) ((double) this.getBlockSpeedFactor() * (this.moveMulIn.x + this.moveMulIn.z) / 2.0D) : this.getBlockSpeedFactor();
	}

	@Override
	public float getMuhJumpFactor() {
		return this.moveMulIn.lengthSqr() > 0.0D ? (float) ((double) this.getBlockJumpFactor() * this.moveMulIn.y) : this.getBlockJumpFactor();
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
			this.level.playSound((LocalPlayer) null, soundPos.x, soundPos.y, soundPos.z, soundevent, this.getSoundSource(), f, f1);
		}
	}

	@Override
	public boolean isClimbeyJumpEquipped() {
		return this.getItemBySlot(EquipmentSlot.FEET) != null && ClientDataHolder.getInstance().jumpTracker.isBoots(this.getItemBySlot(EquipmentSlot.FEET));
	}

	@Override
	public boolean isClimbeyClimbEquipped() {
		if (this.getMainHandItem() != null && ClientDataHolder.getInstance().climbTracker.isClaws(this.getMainHandItem())) {
			return true;
		}
		else {
			return this.getOffhandItem() != null && ClientDataHolder.getInstance().climbTracker.isClaws(this.getOffhandItem());
		}
	}

	@Override
	public void releaseUsingItem() {
		ClientNetworkHelper.sendActiveHand((byte) this.getUsedItemHand().ordinal());
		super.releaseUsingItem();
	}

//	@Override
//	public void updateSyncFields(LocalPlayer old) {
//		this.xLast = old.xLast;
//		this.yLast1 = old.yLast1;
//		this.zLast = old.zLast;
//		this.yRotLast = old.yRotLast;
//		this.xRotLast = old.xRotLast;
//		this.lastOnGround = old.lastOnGround;
//		this.wasShiftKeyDown = old.wasShiftKeyDown;
//		this.wasSprinting = old.wasSprinting;
//		this.positionReminder = old.positionReminder;
//	}

	@Override
	public void setItemInUseClient(ItemStack item, InteractionHand hand) {
		this.useItem = item;

		if (item != ItemStack.EMPTY) {
			this.startedUsingItem = true;
			this.usingItemHand = hand;
		}
		else {
			this.startedUsingItem = false;
			this.usingItemHand = hand;
		}
	}

	@Override
	public void setTeleported(boolean teleported) {
		this.teleported = teleported;
	}

	@Override
	public void setItemInUseCountClient(int count) {
		this.useItemRemaining = count;
	}
}
