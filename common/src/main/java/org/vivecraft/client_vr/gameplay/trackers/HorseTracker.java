package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

public class HorseTracker extends Tracker {
    double boostTrigger = 1.4D;
    double pullTrigger = 0.8D;
    int speedLevel = 0;
    int maxSpeedLevel = 3;
    int coolDownMillis = 500;
    long lastBoostMillis = -1L;
    double turnspeed = 6.0D;
    double bodyturnspeed = 0.2D;
    float baseSpeed = 0.2F;
    Horse horse = null;
    ModelInfo info = new ModelInfo();

    @Override
    public void reset() {
        if (this.horse != null) {
            this.horse.setNoAi(false);
        }
    }

    @Override
    public void doProcess() {
        this.horse = (Horse) mc.player.getVehicle();
        this.horse.setNoAi(true);
        float f = (this.horse.getYRot() + 360.0F) % 360.0F;
        float f1 = (this.horse.yBodyRot + 360.0F) % 360.0F;
        Vec3 vec3 = convertToVec3(dh.vr.controllerHistory[1].netMovement(0.1D, new Vector3d())).scale(10.0D);
        Vec3 vec31 = convertToVec3(dh.vr.controllerHistory[0].netMovement(0.1D, new Vector3d())).scale(10.0D);
        double d0 = min(-vec3.y, -vec31.y);

        if (d0 > this.boostTrigger) {
            this.boost();
        }

        Quaternionf quaternion = new Quaternionf().setAngleAxis(-this.horse.yBodyRot, 0.0F, 1.0F, 0.0F)
            .mul(new Quaternionf().setAngleAxis(0.0F, 1.0F, 0.0F, 0.0F), new Quaternionf())
            .mul(new Quaternionf().setAngleAxis(0.0F, 0.0F, 0.0F, 1.0F), new Quaternionf());
        Vec3 vec32 = convertToVec3(quaternion.transformUnit(forward, new Vector3f()));
        Vec3 vec33 = convertToVec3(quaternion.transformUnit(right, new Vector3f()));
        Vec3 vec34 = convertToVec3(quaternion.transformUnit(left, new Vector3f()));
        Quaternionf quaternion1 = new Quaternionf().setAngleAxis(toRadians(dh.vrSettings.worldRotation), 0.0F, 1.0F, 0.0F)
            .mul(new Quaternionf().setAngleAxis(0.0F, 1.0F, 0.0F, 0.0F), new Quaternionf())
            .mul(new Quaternionf().setAngleAxis(0.0F, 0.0F, 0.0F, 1.0F), new Quaternionf());
        Vec3 vec35 = dh.vrPlayer.roomOrigin.add(convertToVec3(quaternion1.transformUnit(dh.vr.controllerHistory[1].latest(new Vector3f()), new Vector3f())));
        Vec3 vec36 = dh.vrPlayer.roomOrigin.add(convertToVec3(quaternion1.transformUnit(dh.vr.controllerHistory[0].latest(new Vector3f()), new Vector3f())));
        double d1 = vec35.subtract(this.info.leftReinPos).dot(vec32) + vec35.subtract(this.info.leftReinPos).dot(vec33);
        double d2 = vec36.subtract(this.info.rightReinPos).dot(vec32) + vec36.subtract(this.info.rightReinPos).dot(vec34);

        if (this.speedLevel < 0) {
            this.speedLevel = 0;
        }

        if (d1 > this.pullTrigger + 0.3D && d2 > this.pullTrigger + 0.3D && abs(d2 - d1) < 0.1D) {
            if (this.speedLevel <= 0 && System.currentTimeMillis() > this.lastBoostMillis + (long) this.coolDownMillis) {
                this.speedLevel = -1;
            } else {
                this.doBreak();
            }
        } else {
            double d3 = 0.0D;
            double d4 = 0.0D;

            if (d1 > this.pullTrigger) {
                d3 = d1 - this.pullTrigger;
            }

            if (d2 > this.pullTrigger) {
                d4 = d2 - this.pullTrigger;
            }

            this.horse.setYRot((float) ((double) f + (d4 - d3) * this.turnspeed));
        }

        this.horse.yBodyRot = (float) (abs(f - f1) < 360.0D / 2.0D ?
                                       f1 + (f - f1) * this.bodyturnspeed :
                                       f1 + (f - f1 - signum(f - f1) * 360.0D) * this.bodyturnspeed
        );
        this.horse.yHeadRot = f;
        Vector3f vec37 = quaternion.transformUnit(new Vector3f(0.0F, 0.0F, this.speedLevel * this.baseSpeed));
        this.horse.setDeltaMovement(vec37.x, this.horse.getDeltaMovement().y, vec37.z);
    }

    boolean boost() {
        if (this.speedLevel >= this.maxSpeedLevel) {
            return false;
        } else if (System.currentTimeMillis() < this.lastBoostMillis + (long) this.coolDownMillis) {
            return false;
        } else {
            ++this.speedLevel;
            this.lastBoostMillis = System.currentTimeMillis();
            return true;
        }
    }

    boolean doBreak() {
        if (this.speedLevel <= 0) {
            return false;
        } else if (System.currentTimeMillis() < this.lastBoostMillis + (long) this.coolDownMillis) {
            return false;
        } else {
            logger.info("Breaking");
            --this.speedLevel;
            this.lastBoostMillis = System.currentTimeMillis();
            return true;
        }
    }

    public ModelInfo getModelInfo() {
        return this.info;
    }

    public class ModelInfo {
        public Vec3 leftReinPos = Vec3.ZERO;
        public Vec3 rightReinPos = Vec3.ZERO;
    }
}
