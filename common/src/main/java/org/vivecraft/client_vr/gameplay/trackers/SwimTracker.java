package org.vivecraft.client_vr.gameplay.trackers;

import org.joml.Vector3f;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class SwimTracker extends Tracker {
    final Vector3f motion = new Vector3f();
    final float friction = 0.9F;
    float lastDist;
    final float riseSpeed = 0.005F;
    final float swimspeed = 1.3F;

    @Override
    public boolean isActive() {
        if (dh.vrSettings.seated) {
            return false;
        } else if (!dh.vrSettings.realisticSwimEnabled) {
            return false;
        } else if (mc.screen != null) {
            return false;
        } else if (mc.player != null && mc.player.isAlive()) {
            if (mc.gameMode == null) {
                return false;
            } else if (!mc.player.isInWater() && !mc.player.isInLava()) {
                return false;
            } else if (mc.player.zza > 0.0F) {
                return false;
            } else {
                return !(mc.player.xxa > 0.0F);
            }
        } else {
            return false;
        }
    }

    @Override
    public void doProcess() {
        Vector3f vec3 = dh.vrPlayer.vrdata_world_pre.getController(0).getPosition(new Vector3f());
        Vector3f vec32 = dh.vrPlayer.vrdata_world_pre.getController(1).getPosition(new Vector3f()).sub(vec3).mul(0.5F).add(vec3);
        Vector3f vec33 = dh.vrPlayer.vrdata_world_pre.getHeadPivot(new Vector3f()).sub(0.0F, 0.3F, 0.0F);
        Vector3f vec34 = vec32.sub(vec33, new Vector3f()).normalize().add(dh.vrPlayer.vrdata_world_pre.hmd.getDirection(new Vector3f())).mul(0.5F);
        Vector3f vec35 = dh.vrPlayer.vrdata_world_pre.getController(0).getCustomVector(new Vector3f(0.0F, 0.0F, -1.0F)).add(dh.vrPlayer.vrdata_world_pre.getController(1).getCustomVector(new Vector3f(0.0F, 0.0F, -1.0F))).mul(0.5F);
        float d0 = vec34.add(vec35.x, vec35.y, vec35.z).length() / 2.0F;
        float d1 = vec33.distance(vec32);
        float d2 = this.lastDist - d1;

        if (d2 > 0.0F) {
            this.motion.add(vec34.mul(d2 * this.swimspeed * d0).mul(0.15F));
        }

        this.lastDist = d1;
        mc.player.setSwimming(this.motion.length() > 0.3F);
        mc.player.setSprinting(this.motion.length() > 1.0F);
        mc.player.push(this.motion.x, this.motion.y, this.motion.z);
        this.motion.mul(this.friction);
    }
}
