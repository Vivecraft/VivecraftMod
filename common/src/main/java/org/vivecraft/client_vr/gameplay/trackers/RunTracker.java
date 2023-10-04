package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.settings.VRSettings.FreeMove;

import static org.joml.Math.abs;
import static org.joml.Math.atan2;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class RunTracker extends Tracker {
    private double direction = 0.0D;
    private double speed = 0.0D;
    private Vec3 movedir;

    @Override
    public boolean isActive() {
        if (dh.vrPlayer.getFreeMove() && !dh.vrSettings.seated) {
            if (dh.vrSettings.vrFreeMoveMode != FreeMove.RUN_IN_PLACE) {
                return false;
            } else if (mc.player != null && mc.player.isAlive()) {
                if (mc.gameMode == null) {
                    return false;
                } else if (mc.player.onGround() || !mc.player.isInWater() && !mc.player.isInLava()) {
                    if (mc.player.onClimbable()) {
                        return false;
                    } else {
                        return !dh.bowTracker.isNotched();
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public double getYaw() {
        return this.direction;
    }

    public double getSpeed() {
        return this.speed;
    }

    @Override
    public void reset() {
        this.speed = 0.0D;
    }

    @Override
    public void doProcess() {
        Vec3 vec3 = dh.vrPlayer.vrdata_world_pre.getController(0).getPosition();
        Vec3 vec31 = dh.vrPlayer.vrdata_world_pre.getController(1).getPosition();
        double d0 = dh.vr.controllerHistory[0].averageSpeed(0.33D);
        double d1 = dh.vr.controllerHistory[1].averageSpeed(0.33D);

        if (this.speed > 0.0D) {
            if (d0 < 0.1D && d1 < 0.1D) {
                this.speed = 0.0D;
                return;
            }
        } else if (d0 < 0.6D && d1 < 0.6D) {
            this.speed = 0.0D;
            return;
        }

        if (abs(d0 - d1) > 0.5D) {
            this.speed = 0.0D;
        } else {
            Vec3 vec32 = dh.vrPlayer.vrdata_world_pre.getController(0).getDirection().add(dh.vrPlayer.vrdata_world_pre.getController(1).getDirection()).scale(0.5D);
            this.direction = atan2(-vec32.x, vec32.z);
            double d2 = (d0 + d1) / 2.0D;
            this.speed = d2 * 1.0D * 1.3D;

            if (this.speed > 0.1D) {
                this.speed = 1.0D;
            }

            if (this.speed > 1.0D) {
                this.speed = 1.3F;
            }
        }
    }
}
