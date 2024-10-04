package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRSettings;

public class JumpTracker extends Tracker {
    public Vec3[] latchStart = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartOrigin = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartPlayer = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    private boolean c0Latched = false;
    private boolean c1Latched = false;

    public JumpTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    /**
     * @param player Player to check
     * @return if the given {@code player} can use climbey jump
     */
    public boolean isClimbeyJump(LocalPlayer player) {
        return this.isActive(player) && hasClimbeyJumpEquipped(player);
    }

    /**
     * @param player Player to check
     * @return if the given {@code player} has jump boots equipped
     */
    public static boolean hasClimbeyJumpEquipped(Player player) {
        return ClientNetworking.serverAllowsClimbey && isBoots(player.getItemBySlot(EquipmentSlot.FEET));
    }

    /**
     * @param itemStack ItemStack to check
     * @return if the given {@code itemStack} is a jump boots item
     */
    public static boolean isBoots(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else if (!itemStack.hasCustomHoverName()) {
            return false;
        } else if (itemStack.getItem() != Items.LEATHER_BOOTS) {
            return false;
        } else if (!itemStack.hasTag() || !itemStack.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return itemStack.getHoverName().getString().equals("Jump Boots") ||
                (itemStack.getHoverName().getContents() instanceof TranslatableContents translatableContent &&
                    translatableContent.getKey().equals("vivecraft.item.jumpboots")
                );
        }
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.dh.vrSettings.seated) {
            return false;
        } else if (!this.dh.vrPlayer.getFreeMove() && !this.dh.vrSettings.simulateFalling) {
            return false;
        } else if (this.dh.vrSettings.realisticJumpEnabled == VRSettings.RealisticJump.OFF) {
            return false;
        } else if (player == null || !player.isAlive()) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (player.isInWater() || player.isInLava() || !player.onGround()) {
            return false;
        } else {
            return !player.isShiftKeyDown() && !player.isPassenger();
        }
    }

    public boolean isjumping() {
        return this.c1Latched || this.c0Latched;
    }

    @Override
    public void idleTick(LocalPlayer player) {
        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyJump).setEnabled(hasClimbeyJumpEquipped(player) &&
            (this.isActive(player) ||
                (ClimbTracker.hasClimbeyClimbEquipped(player) && this.dh.climbTracker.isGrabbingLadder())
            ));
    }

    @Override
    public void reset(LocalPlayer player) {
        this.c1Latched = false;
        this.c0Latched = false;
    }

    @Override
    public void doProcess(LocalPlayer player) {
        boolean climbeyEquipped = hasClimbeyJumpEquipped(player);

        if (climbeyEquipped) {
            boolean[] ok = new boolean[2];

            for (int c = 0; c < 2; c++) {
                ok[c] = VivecraftVRMod.INSTANCE.keyClimbeyJump.isDown();
            }

            boolean jump = false;

            if (!ok[0] && this.c0Latched) {
                // let go right
                this.dh.vr.triggerHapticPulse(0, 200);
                jump = true;
            }

            Vec3 rPos = this.dh.vrPlayer.vrdata_room_pre.getController(0).getPosition();
            Vec3 lPos = this.dh.vrPlayer.vrdata_room_pre.getController(1).getPosition();
            Vec3 now = rPos.add(lPos).scale(0.5D);

            if (ok[0] && !this.c0Latched) {
                // grabbed right
                this.latchStart[0] = now;
                this.latchStartOrigin[0] = this.dh.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[0] = this.mc.player.position();
                this.dh.vr.triggerHapticPulse(0, 1000);
            }

            if (!ok[1] && this.c1Latched) {
                // let go left
                this.dh.vr.triggerHapticPulse(1, 200);
                jump = true;
            }

            if (ok[1] && !this.c1Latched) {
                // grabbed left
                this.latchStart[1] = now;
                this.latchStartOrigin[1] = this.dh.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[1] = this.mc.player.position();
                this.dh.vr.triggerHapticPulse(1, 1000);
            }

            this.c0Latched = ok[0];
            this.c1Latched = ok[1];

            int c = 0;
            Vec3 delta = now.subtract(this.latchStart[c]);
            delta = delta.yRot(this.dh.vrPlayer.vrdata_world_pre.rotation_radians);

            if (!jump && this.isjumping()) {
                // bzzzzzz
                this.dh.vr.triggerHapticPulse(0, 200);
                this.dh.vr.triggerHapticPulse(1, 200);
            }

            if (jump) {
                this.dh.climbTracker.forceActivate = true;
                Vec3 movement = this.dh.vr.controllerHistory[0].netMovement(0.3D)
                    .add(this.dh.vr.controllerHistory[1].netMovement(0.3D));

                double speed = (this.dh.vr.controllerHistory[0].averageSpeed(0.3D) + this.dh.vr.controllerHistory[1].averageSpeed(0.3D)) / 2.0D;

                movement = movement.scale((double) 0.33F * speed);

                // cap
                final float limit = 0.66F;

                if (movement.length() > limit) {
                    movement = movement.scale(limit / movement.length());
                }

                if (player.hasEffect(MobEffects.JUMP)) {
                    movement = movement.scale((double) player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5D);
                }

                movement = movement.yRot(this.dh.vrPlayer.vrdata_world_pre.rotation_radians);
                Vec3 lastPosition = this.mc.player.position().subtract(delta);

                if (delta.y < 0.0D && movement.y < 0.0D) {
                    player.setDeltaMovement(
                        player.getDeltaMovement().x - movement.x * 1.25D,
                        -movement.y,
                        player.getDeltaMovement().z - movement.z * 1.25D);

                    player.xOld = lastPosition.x;
                    player.yOld = lastPosition.y;
                    player.zOld = lastPosition.z;

                    lastPosition = lastPosition.add(player.getDeltaMovement());

                    player.setPos(lastPosition.x, lastPosition.y, lastPosition.z);

                    this.dh.vrPlayer.snapRoomOriginToPlayerEntity(player, false, true);
                    this.mc.player.causeFoodExhaustion(0.3F);
                    this.mc.player.setOnGround(false);
                } else {
                    this.dh.vrPlayer.snapRoomOriginToPlayerEntity(player, false, true);
                }
            } else if (this.isjumping()) {
                Vec3 thing = this.latchStartOrigin[0]
                    .subtract(this.latchStartPlayer[0])
                    .add(this.mc.player.position())
                    .subtract(delta);
                this.dh.vrPlayer.setRoomOrigin(thing.x, thing.y, thing.z, false);
            }
        }
        if ((!climbeyEquipped || ClientDataHolderVR.getInstance().vrSettings.realisticJumpEnabled == VRSettings.RealisticJump.ON) &&
            this.dh.vr.hmdPivotHistory.netMovement(0.25D).y > 0.1D &&
            this.dh.vr.hmdPivotHistory.latest().y - AutoCalibration.getPlayerHeight() > this.dh.vrSettings.jumpThreshold)
        {
            player.jumpFromGround();
        }
    }
}
