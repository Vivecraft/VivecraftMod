package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

public class RunTracker extends Tracker {
    private double direction = 0.0D;
    private double speed = 0.0D;

    public RunTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (!this.dh.vrPlayer.getFreeMove() || this.dh.vrSettings.seated) {
            return false;
        } else if (this.dh.vrSettings.vrFreeMoveMode != VRSettings.FreeMove.RUN_IN_PLACE) {
            return false;
        } else if (player == null || !player.isAlive()) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (!player.onGround() && (player.isInWater() || player.isInLava())) {
            return false;
        } else if (player.onClimbable()) {
            return false;
        } else {
            return !this.dh.bowTracker.isNotched();
        }
    }

    /**
     * @return yaw direction the player is running at, in radians
     */
    public double getYaw() {
        return this.direction;
    }

    public double getSpeed() {
        return this.speed;
    }

    @Override
    public void reset(LocalPlayer player) {
        this.speed = 0.0D;
    }

    @Override
    public void doProcess(LocalPlayer player) {

        double c0Move = this.dh.vr.controllerHistory[0].averageSpeed(0.33D);
        double c1Move = this.dh.vr.controllerHistory[1].averageSpeed(0.33D);

        if (this.speed > 0.0D) {
            if (c0Move < 0.1D && c1Move < 0.1D) {
                this.speed = 0.0D;
                return;
            }
        } else if (c0Move < 0.6D && c1Move < 0.6D) {
            this.speed = 0.0D;
            return;
        }

        if (Math.abs(c0Move - c1Move) > 0.5D) {
            this.speed = 0.0D;
            return;
        }

        Vec3 v = this.dh.vrPlayer.vrdata_world_pre.getController(0).getDirection()
            .add(this.dh.vrPlayer.vrdata_world_pre.getController(1).getDirection())
            .scale(0.5D);
        this.direction = Math.atan2(-v.x, v.z);
        double spd = (c0Move + c1Move) / 2.0D;
        this.speed = spd * 1.0D * 1.3D;

        if (this.speed > 0.1D) {
            this.speed = 1.0D;
        }

        if (this.speed > 1.0D) {
            this.speed = 1.3F;
        }
    }
}
