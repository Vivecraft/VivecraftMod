package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.Random;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVec3;

public class EatingTracker extends Tracker {
    float mouthtoEyeDistance = 0.0F;
    float threshold = 0.25F;
    public boolean[] eating = new boolean[2];
    int eattime = 2100;
    long eatStart;
    private Random r = new Random();

    public boolean isEating() {
        return this.eating[0] || this.eating[1];
    }

    @Override
    public boolean isActive() {
        if (dh.vrSettings.seated) {
            return false;
        } else if (mc.player == null) {
            return false;
        } else if (mc.gameMode == null) {
            return false;
        } else if (!mc.player.isAlive()) {
            return false;
        } else if (mc.player.isSleeping()) {
            return false;
        } else {
            if (mc.player.getMainHandItem() != null) {
                UseAnim useanim = mc.player.getMainHandItem().getUseAnimation();

                if (useanim == UseAnim.EAT || useanim == UseAnim.DRINK || useanim == UseAnim.TOOT_HORN) {
                    return true;
                }
            }

            if (mc.player.getOffhandItem() != null) {
                UseAnim useanim1 = mc.player.getOffhandItem().getUseAnimation();

                return useanim1 == UseAnim.EAT || useanim1 == UseAnim.DRINK || useanim1 == UseAnim.TOOT_HORN;
            }

            return false;
        }
    }

    @Override
    public void reset() {
        this.eating[0] = false;
        this.eating[1] = false;
    }

    @Override
    public void doProcess() {
        Vec3 hmdPos = dh.vrPlayer.vrdata_room_pre.hmd.getPosition();
        Vec3 mouthPos = dh.vrPlayer.vrdata_room_pre.getController(0).getCustomVector(new Vec3(0.0D, -this.mouthtoEyeDistance, 0.0D)).add(hmdPos);

        for (int c = 0; c < 2; ++c) {

            Vec3 controllerPos = convertToVec3(dh.vr.controllerHistory[c].averagePosition(0.333D, new Vector3d())).add(dh.vrPlayer.vrdata_room_pre.getController(c).getCustomVector(new Vec3(0.0D, 0.0D, -0.1D)));
            controllerPos = controllerPos.add(dh.vrPlayer.vrdata_room_pre.getController(c).getDirection().scale(0.1D));

            if (mouthPos.distanceTo(controllerPos) < (double) this.threshold) {
                ItemStack itemstack = c == 0 ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
                if (itemstack == ItemStack.EMPTY) {
                    continue;
                }

                int crunchiness = 0;

                if (itemstack.getUseAnimation() == UseAnim.DRINK) {//thats how liquid works.
                    if (dh.vrPlayer.vrdata_room_pre.getController(c).getCustomVector(new Vec3(0, 1, 0)).y > 0) {
                        continue;
                    }
                } else if (itemstack.getUseAnimation() == UseAnim.EAT) {
                    crunchiness = 2;
                } else if (itemstack.getUseAnimation() == UseAnim.TOOT_HORN) {
                    crunchiness = 1;
                } else {
                    continue;
                }

                if (!this.eating[c]) {
                    //Minecraft.getInstance().physicalGuiManager.preClickAction();

                    if (mc.gameMode.useItem(mc.player, c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).consumesAction()) {
                        mc.gameRenderer.itemInHandRenderer.itemUsed(c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                        this.eating[c] = true;
                        this.eatStart = Util.getMillis();
                    }
                }

                if (this.eating[c]) {
                    long k = mc.player.getUseItemRemainingTicks();

                    if (k > 0L && k % 5L <= (long) crunchiness) {
                        dh.vr.triggerHapticPulse(c, 700);
                    }
                }

                if (Util.getMillis() - this.eatStart > (long) this.eattime) {
                    this.eating[c] = false;
                }
            } else {
                this.eating[c] = false;
            }
        }
    }
}
