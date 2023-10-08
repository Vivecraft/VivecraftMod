package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.settings.AutoCalibration;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVec3;
import static org.vivecraft.common.utils.Utils.convertToVector3f;

public class JumpTracker extends Tracker {
    public final Vector3f latchStartc0 = new Vector3f();
    public final Vector3f latchStartc1 = new Vector3f();
    public final Vector3f latchStartOriginc0 = new Vector3f();
    public final Vector3f latchStartOriginc1 = new Vector3f();
    public final Vector3f latchStartPlayerc0 = new Vector3f();
    public final Vector3f latchStartPlayerc1 = new Vector3f();
    private boolean c0Latched = false;
    private boolean c1Latched = false;

    public boolean isClimbeyJump() {
        return this.isActive() && this.isClimbeyJumpEquipped();
    }

    public boolean isClimbeyJumpEquipped() {
        return ClientNetworking.serverAllowsClimbey && ((PlayerExtension) mc.player).vivecraft$isClimbeyJumpEquipped();
    }

    @Override
    public boolean isActive() {
        if (dh.vrSettings.seated) {
            return false;
        } else if (!dh.vrPlayer.getFreeMove() && !dh.vrSettings.simulateFalling) {
            return false;
        } else if (!dh.vrSettings.realisticJumpEnabled) {
            return false;
        } else if (mc.player != null && mc.player.isAlive()) {
            if (mc.gameMode == null) {
                return false;
            } else if (!mc.player.isInWater() && !mc.player.isInLava() && mc.player.onGround()) {
                return !mc.player.isShiftKeyDown() && !mc.player.isPassenger();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isjumping() {
        return this.c1Latched || this.c0Latched;
    }

    @Override
    public void idleTick() {
        dh.vr.getInputAction(VivecraftVRMod.keyClimbeyJump).setEnabled(this.isClimbeyJumpEquipped() && (this.isActive() || dh.climbTracker.isClimbeyClimbEquipped() && dh.climbTracker.isGrabbingLadder()));
    }

    @Override
    public void reset() {
        this.c1Latched = false;
        this.c0Latched = false;
    }

    @Override
    public void doProcess() {
        if (this.isClimbeyJumpEquipped()) {
            Vector3fc playerPos = convertToVector3f(mc.player.position(), new Vector3f());
            boolean[] aboolean = new boolean[2];

            for (int i = 0; i < 2; ++i) {
                aboolean[i] = VivecraftVRMod.keyClimbeyJump.isDown();
            }

            boolean flag = false;

            if (!aboolean[0] && this.c0Latched) {
                dh.vr.triggerHapticPulse(0, 200);
                flag = true;
            }

            Vector3f vec32 = dh.vrPlayer.vrdata_room_pre.getController(0).getPosition(new Vector3f())
                .add(dh.vrPlayer.vrdata_room_pre.getController(1).getPosition(new Vector3f()))
                .mul(0.5F);

            if (aboolean[0] && !this.c0Latched) {
                this.latchStartc0.set(vec32);
                this.latchStartOriginc0.set(dh.vrPlayer.vrdata_world_pre.origin);
                this.latchStartPlayerc0.set(playerPos);
                dh.vr.triggerHapticPulse(0, 1000);
            }

            if (!aboolean[1] && this.c1Latched) {
                dh.vr.triggerHapticPulse(1, 200);
                flag = true;
            }

            if (aboolean[1] && !this.c1Latched) {
                this.latchStartc1.set(vec32);
                this.latchStartOriginc1.set(dh.vrPlayer.vrdata_world_pre.origin);
                this.latchStartPlayerc1.set(playerPos);
                dh.vr.triggerHapticPulse(1, 1000);
            }

            this.c0Latched = aboolean[0];
            this.c1Latched = aboolean[1];
            vec32.sub(this.latchStartc0).rotateY(dh.vrPlayer.vrdata_world_pre.rotation_radians);

            if (!flag && this.isjumping()) {
                dh.vr.triggerHapticPulse(0, 200);
                dh.vr.triggerHapticPulse(1, 200);
            }

            if (flag) {
                dh.climbTracker.forceActivate = true;
                Vector3f vec34 = dh.vr.controllerHistory[0].netMovement(0.3F, new Vector3f()).add(dh.vr.controllerHistory[1].netMovement(0.3F, new Vector3f()));
                float d0 = (dh.vr.controllerHistory[0].averageSpeed(0.3F) + dh.vr.controllerHistory[1].averageSpeed(0.3F)) / 2.0F;
                vec34.mul(0.33F * d0);
                float f = 0.66F;

                if (vec34.length() > f) {
                    vec34.mul(f / vec34.length());
                }

                if (mc.player.hasEffect(MobEffects.JUMP)) {
                    vec34.mul(mc.player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5F);
                }

                vec34.rotateY(dh.vrPlayer.vrdata_world_pre.rotation_radians);

                if (vec32.y < 0.0F && vec34.y < 0.0F) {
                    playerPos.sub(vec32, vec32);
                    mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x - vec34.x * 1.25F,
                        -vec34.y,
                        mc.player.getDeltaMovement().z - vec34.z * 1.25F
                    );
                    mc.player.xOld = vec32.x;
                    mc.player.yOld = vec32.y;
                    mc.player.zOld = vec32.z;
                    vec32.add(convertToVector3f(mc.player.getDeltaMovement(), new Vector3f()));
                    mc.player.setPos(vec32.x, vec32.y, vec32.z);
                    dh.vrPlayer.snapRoomOriginToPlayerEntity(false, true);
                    mc.player.causeFoodExhaustion(0.3F);
                    mc.player.setOnGround(false);
                } else {
                    dh.vrPlayer.snapRoomOriginToPlayerEntity(false, true);
                }
            } else if (this.isjumping()) {
                Vector3f vec36 = this.latchStartOriginc0.sub(this.latchStartPlayerc0, new Vector3f()).add(playerPos).sub(vec32);
                dh.vrPlayer.setRoomOrigin(vec36.x, vec36.y, vec36.z, false);
            }
        } else if (dh.vr.hmdPivotHistory.netMovement(0.25D, new Vector3d()).y > 0.1D && dh.vr.hmdPivotHistory.latest(new Vector3d()).y - (double) AutoCalibration.getPlayerHeight() > (double) dh.vrSettings.jumpThreshold) {
            mc.player.jumpFromGround();
        }
    }

    public boolean isBoots(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        } else if (!i.hasCustomHoverName()) {
            return false;
        } else if (i.getItem() != Items.LEATHER_BOOTS) {
            return false;
        } else if (!i.hasTag() || !i.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return i.getHoverName().getContents() instanceof TranslatableContents && "vivecraft.item.jumpboots".equals(((TranslatableContents) i.getHoverName().getContents()).getKey()) || "Jump Boots".equals(i.getHoverName().getString());
        }
    }
}
