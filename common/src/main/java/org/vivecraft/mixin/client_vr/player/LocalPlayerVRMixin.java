package org.vivecraft.mixin.client_vr.player;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.common.network.packet.c2s.TeleportPayloadC2S;
import org.vivecraft.data.ViveModifiers;

import java.util.function.Predicate;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerVRMixin extends LocalPlayer_PlayerVRMixin implements PlayerExtension {

    @Unique
    private Vec3 vivecraft$moveMulIn = Vec3.ZERO;

    @Unique
    private boolean vivecraft$initFromServer;

    @Unique
    private boolean vivecraft$teleported;

    @Unique
    private final ClientDataHolderVR vivecraft$dataholder = ClientDataHolderVR.getInstance();

    @Final
    @Shadow
    protected Minecraft minecraft;

    @Shadow
    protected abstract void updateAutoJump(float movementX, float movementZ);

    @Shadow
    public abstract boolean isShiftKeyDown();

    @Shadow
    public abstract InteractionHand getUsedItemHand();

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(method = "startRiding", at = @At("TAIL"))
    private void vivecraft$startRidingTracker(
        CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) Entity vehicle)
    {
        if (VRState.VR_INITIALIZED && vivecraft$isLocalPlayer(this)) {
            ClientDataHolderVR.getInstance().vehicleTracker.onStartRiding(vehicle);
        }
    }

    @Inject(method = "removeVehicle", at = @At("TAIL"))
    private void vivecraft$stopRidingTracker(CallbackInfo ci) {
        if (VRState.VR_INITIALIZED && vivecraft$isLocalPlayer(this)) {
            ClientDataHolderVR.getInstance().vehicleTracker.onStopRiding();
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V"))
    private void vivecraft$preTick(CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            ClientDataHolderVR.getInstance().vrPlayer.doPermanentLookOverride((LocalPlayer) (Object) this,
                ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER))
    private void vivecraft$postTick(CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            ClientNetworking.overridePose((LocalPlayer) (Object) this);
            ClientDataHolderVR.getInstance().vrPlayer.doPermanentLookOverride((LocalPlayer) (Object) this,
                ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre);
        }
    }

    @ModifyVariable(method = "sendPosition", at = @At("STORE"), ordinal = 1)
    private boolean vivecraft$directTeleport(boolean updateRotation) {
        if (this.vivecraft$teleported) {
            updateRotation = true;
            ClientNetworking.sendServerPacket(
                new TeleportPayloadC2S((float) this.getX(), (float) this.getY(), (float) this.getZ()));
        }
        return updateRotation;
    }

    // needed, or the server will spam 'moved too quickly'/'moved wrongly'
    @WrapWithCondition(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private boolean vivecraft$noMovePacketsOnTeleport(ClientPacketListener instance, Packet packet) {
        return !this.vivecraft$teleported;
    }

    @Inject(method = "sendPosition", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;lastOnGround:Z", shift = At.Shift.AFTER, ordinal = 1))
    private void vivecraft$noAutoJump(CallbackInfo ci) {
        // clear teleport here, after all the packets would be sent
        this.vivecraft$teleported = false;
        if (VRState.VR_RUNNING && ClientDataHolderVR.getInstance().vrSettings.walkUpBlocks) {
            this.minecraft.options.autoJump().set(false);
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"))
    private void vivecraft$VRPlayerTick(CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            ClientDataHolderVR.getInstance().vrPlayer.tick((LocalPlayer) (Object) this);
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void vivecraft$overwriteMove(MoverType type, Vec3 pos, CallbackInfo ci) {
        if (!VRState.VR_RUNNING || !vivecraft$isLocalPlayer(this) ||
            Minecraft.getInstance().getCameraEntity() != (Object) this)
        {
            AttributeInstance attribute = this.getAttribute(Attributes.STEP_HEIGHT);
            if (attribute != null && attribute.hasModifier(ViveModifiers.WALK_UP_BLOCKS)) {
                attribute.removeModifier(ViveModifiers.WALK_UP_BLOCKS);
            }
            return;
        }
        // stuckSpeedMultiplier gets zeroed in the super call.
        this.vivecraft$moveMulIn = this.stuckSpeedMultiplier;

        if (pos.length() == 0 || this.isPassenger()) {
            super.move(type, pos);
        } else {
            boolean freeMove = VRPlayer.get().getFreeMove();
            boolean doY = freeMove || (ClientDataHolderVR.getInstance().vrSettings.simulateFalling &&
                !this.onClimbable() && !this.isShiftKeyDown()
            );

            if (ClientDataHolderVR.getInstance().climbTracker.isActive((LocalPlayer) (Object) this) &&
                (freeMove || ClientDataHolderVR.getInstance().climbTracker.isGrabbingLadder()))
            {
                doY = true;
            }

            Vec3 roomOrigin = VRPlayer.get().roomOrigin;

            if ((ClientDataHolderVR.getInstance().climbTracker.isGrabbingLadder() || freeMove ||
                ClientDataHolderVR.getInstance().swimTracker.isActive((LocalPlayer) (Object) this)
            ) && (this.zza != 0.0F || this.isFallFlying() || Math.abs(this.getDeltaMovement().x) > 0.01D ||
                Math.abs(this.getDeltaMovement().z) > 0.01D
            ))
            {
                double xOffset = roomOrigin.x - this.getX();
                double zOffset = roomOrigin.z - this.getZ();
                double oldX = this.getX();
                double oldZ = this.getZ();
                super.move(type, pos);

                AttributeInstance attribute = this.getAttribute(Attributes.STEP_HEIGHT);
                if (attribute != null) {
                    if (ClientDataHolderVR.getInstance().vrSettings.walkUpBlocks) {
                        if (this.getBlockJumpFactor() == 1.0F) {
                            if (!attribute.hasModifier(ViveModifiers.WALK_UP_BLOCKS)) {
                                attribute.addTransientModifier(
                                    new AttributeModifier(ViveModifiers.WALK_UP_BLOCKS, 0.4F,
                                        AttributeModifier.Operation.ADD_VALUE));
                            }
                        } else {
                            if (attribute.hasModifier(ViveModifiers.WALK_UP_BLOCKS)) {
                                attribute.removeModifier(ViveModifiers.WALK_UP_BLOCKS);
                            }
                        }
                    } else {
                        if (attribute.hasModifier(ViveModifiers.WALK_UP_BLOCKS)) {
                            attribute.removeModifier(ViveModifiers.WALK_UP_BLOCKS);
                        }
                        this.updateAutoJump((float) (this.getX() - oldX), (float) (this.getZ() - oldZ));
                    }
                }

                VRPlayer.get().setRoomOrigin(
                    this.getX() + xOffset,
                    this.getY() + this.vivecraft$getRoomYOffsetFromPose(),
                    this.getZ() + zOffset,
                    false);
            } else if (doY) {
                super.move(type, new Vec3(0.0D, pos.y, 0.0D));
                VRPlayer.get().setRoomOrigin(
                    VRPlayer.get().roomOrigin.x,
                    this.getY() + this.vivecraft$getRoomYOffsetFromPose(),
                    VRPlayer.get().roomOrigin.z,
                    false);
            } else {
                // do not move player, VRPlayer.doPlayerMoveInRoom will move him around.
                this.setOnGround(true);
            }
        }
        ci.cancel();
    }

    @ModifyArg(method = "updateAutoJump", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(D)F"))
    private double vivecraft$modifyAutoJumpSin(double original) {
        return VRState.VR_RUNNING ? ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre.getBodyYawRad() :
            original;
    }

    @ModifyArg(method = "updateAutoJump", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(D)F"))
    private double vivecraft$modifyAutoJumpCos(double original) {
        return VRState.VR_RUNNING ? ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_pre.getBodyYawRad() :
            original;
    }

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void vivecraft$hapticsOnEvent(byte id, CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            if (id == EntityEvent.DEATH) {
                ClientDataHolderVR.getInstance().vr.triggerHapticPulse(0, 2000);
                ClientDataHolderVR.getInstance().vr.triggerHapticPulse(1, 2000);
            }
        }
    }

    @Override
    protected void vivecraft$spearHaptic(CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(this.getUsedItemHand().ordinal(), 2000);
        }
    }

    @Inject(method = "getRopeHoldPosition", at = @At("HEAD"), cancellable = true)
    private void vivecraft$vrRopePosition(CallbackInfoReturnable<Vec3> cir) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            // vanilla rop position is fixed to the view in first person, so attached it to the hand instead
            cir.setReturnValue(RenderHelper.getControllerRenderPos(0));
        }
    }

    @ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"), index = 0)
    private static double vivecraft$getCrossVec(double hitDistance) {
        if (VRState.VR_RUNNING) {
            VRPlayer vrPlayer = ClientDataHolderVR.getInstance().vrPlayer;
            // get the end of the reach point here, to have the correct reach distance
            vrPlayer.crossVec = vrPlayer.AimedPointAtDistance(vrPlayer.vrdata_world_render.getAim(), hitDistance);
        }
        return hitDistance;
    }

    @ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
    private static Predicate<Entity> vivecraft$dontHitRiddenEntity(Predicate<Entity> filter) {
        // it is technically possible to hit the ridden entity when the distance is 0, we don't want that
        if (VRState.VR_RUNNING) {
            return filter.and(entity -> entity != Minecraft.getInstance().getCameraEntity().getVehicle());
        } else {
            return filter;
        }
    }

    /**
     * inject into {@link LivingEntity#releaseUsingItem()}
     */
    @Override
    protected void vivecraft$beforeReleaseUsingItem(CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            ClientNetworking.sendActiveHand(this.isUsingItem() ? this.getUsedItemHand() : InteractionHand.MAIN_HAND,
                false);
        }
    }

    /**
     * inject into {@link Entity#absSnapTo(double, double, double, float, float)}
     * and {@link Entity#snapTo(double, double, double, float, float)}
     */
    @Override
    protected void vivecraft$afterAbsMoveTo(CallbackInfo ci) {
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this) && this.vivecraft$initFromServer) {
            ClientDataHolderVR.getInstance().vrPlayer.snapRoomOriginToPlayerEntity((LocalPlayer) (Object) this, false,
                false);
        }
    }

    /**
     * inject into {@link Entity#setPos(double, double, double)}
     */
    @Override
    protected void vivecraft$wrapSetPos(double x, double y, double z, Operation<Void> original) {
        this.vivecraft$initFromServer = true;
        if (!VRState.VR_RUNNING || !vivecraft$isLocalPlayer(this)) {
            original.call(x, y, z);
            return;
        }
        boolean wasZero = this.position() == Vec3.ZERO;
        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();
        original.call(x, y, z);
        double newX = this.getX();
        double newY = this.getY();
        double newZ = this.getZ();

        if (Minecraft.getInstance().getCameraEntity() == (Object) this && this.isPassenger()) {
            ClientDataHolderVR.getInstance().vehicleTracker.updateRiderPos(x, y, z, this.getVehicle());
        } else if (!ClientDataHolderVR.getInstance().vehicleTracker.isRiding()) {
            if (wasZero) {
                VRPlayer.get().snapRoomOriginToPlayerEntity((LocalPlayer) (Object) this, x + y + z == 0.0D, false);
            } else {
                Vec3 roomOrigin = ClientDataHolderVR.getInstance().vrPlayer.roomOrigin;
                VRPlayer.get().setRoomOrigin(
                    roomOrigin.x + (newX - oldX),
                    roomOrigin.y + (newY - oldY),
                    roomOrigin.z + (newZ - oldZ),
                    x + y + z == 0.0D);
            }
        }
    }

    /**
     * inject into {@link Entity#moveRelative(float, Vec3)}
     */
    @Override
    protected Vec3 vivecraft$controllerMovement(Vec3 relative, float amount, float facing, Operation<Vec3> original) {
        if (!VRState.VR_RUNNING || !vivecraft$isLocalPlayer(this)) {
            return original.call(relative, amount, facing);
        } else {
            return this.vivecraft$dataholder.vrPlayer.freemoveDirection((LocalPlayer) (Object) this, relative, amount);
        }
    }

    /**
     * inject into {@link Entity#moveRelative(float, Vec3)}
     */
    @Override
    protected void vivecraft$afterMoveRelative(CallbackInfo ci, Vec3 movement) {
        // do drag after setting the delta movement
        if (VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) {
            this.vivecraft$dataholder.vrPlayer.applyDrag((LocalPlayer) (Object) this, movement);
        }
    }

    /**
     * modify into {@link LivingEntity#handleRelativeFrictionAndCalculateMovement}
     */
    @Override
    protected boolean vivecraft$disableVanillaClimbing(boolean original) {
        return original && (!(VRState.VR_RUNNING && vivecraft$isLocalPlayer(this)) ||
            this.vivecraft$dataholder.vrSettings.vanillaClimbing
        );
    }

    @Unique
    private boolean vivecraft$isLocalPlayer(Object player) {
        return player.getClass().equals(LocalPlayer.class) || Minecraft.getInstance().player == player;
    }

    @Override
    @Unique
    public boolean vivecraft$getInitFromServer() {
        return this.vivecraft$initFromServer;
    }

    @Override
    @Unique
    public float vivecraft$getMuhSpeedFactor() {
        // this shouldn't ever be null, but mixins doesn't always apply the default value
        return this.vivecraft$moveMulIn != null && this.vivecraft$moveMulIn.lengthSqr() > 0.0D ?
            this.getBlockSpeedFactor() * (float) (this.vivecraft$moveMulIn.x + this.vivecraft$moveMulIn.z) * 0.5F :
            this.getBlockSpeedFactor();
    }

    @Override
    @Unique
    public float vivecraft$getMuhJumpFactor() {
        // this shouldn't ever be null, but mixins doesn't always apply the default value
        return this.vivecraft$moveMulIn != null && this.vivecraft$moveMulIn.lengthSqr() > 0.0D ?
            this.getBlockJumpFactor() * (float) this.vivecraft$moveMulIn.y : this.getBlockJumpFactor();
    }

    @Override
    @Unique
    public void vivecraft$stepSound(BlockPos blockPos, Vec3 soundPos) {
        BlockState blockState = this.level().getBlockState(blockPos);
        SoundType soundType = blockState.getSoundType();
        BlockState aboveBlockState = this.level().getBlockState(blockPos.above());

        if (aboveBlockState.is(Blocks.SNOW)) {
            soundType = aboveBlockState.getSoundType();
        }

        // TODO: liquid is deprecated
        if (!this.isSilent() && !blockState.liquid()) {
            float volume = soundType.getVolume();
            float pitch = soundType.getPitch();
            SoundEvent soundevent = soundType.getStepSound();

            this.level()
                .playSound(null, soundPos.x, soundPos.y, soundPos.z, soundevent, this.getSoundSource(), volume, pitch);
        }
    }

    @Override
    @Unique
    public void vivecraft$setTeleported(boolean teleported) {
        this.vivecraft$teleported = teleported;
    }

    @Override
    @Unique
    public void vivecraft$setItemInUseRemainingClient(int count) {
        this.useItemRemaining = count;
    }

    @Override
    @Unique
    public double vivecraft$getRoomYOffsetFromPose() {
        // Adjust room origin to account for pose, only when not standing on something.
        if (this.getPose() == Pose.FALL_FLYING ||
            this.getPose() == Pose.SPIN_ATTACK ||
            (this.getPose() == Pose.SWIMMING && !ClientDataHolderVR.getInstance().crawlTracker.crawlsteresis))
        {
            return -1.2D;
        } else {
            return 0.0D;
        }
    }
}
