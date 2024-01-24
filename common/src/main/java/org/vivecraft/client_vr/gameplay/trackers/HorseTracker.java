package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.Utils;

public class HorseTracker extends Tracker {
    double boostTrigger = 1.4D;
    double pullTrigger = 0.8D;
    int speedLevel = 0;
    int maxSpeedLevel = 3;
    int coolDownMillis = 500;
    long lastBoostMillis = -1L;
    double turnspeed = 6.0D;
    double bodyturnspeed = 0.2D;
    double baseSpeed = 0.2D;
    Horse horse = null;
    ModelInfo info = new ModelInfo();

    public HorseTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p) {
        return false;
    }

    public void reset(LocalPlayer player) {
        super.reset(player);

        if (this.horse != null) {
            this.horse.setNoAi(false);
        }
    }

    public void doProcess(LocalPlayer player) {
        this.horse = (Horse) player.getVehicle();
        this.horse.setNoAi(true);
        float f = (this.horse.getYRot() + 360.0F) % 360.0F;
        float f1 = (this.horse.yBodyRot + 360.0F) % 360.0F;
        Vec3 vec3 = this.dh.vr.controllerHistory[1].netMovement(0.1D).scale(10.0D);
        Vec3 vec31 = this.dh.vr.controllerHistory[0].netMovement(0.1D).scale(10.0D);
        double d0 = Math.min(-vec3.y, -vec31.y);

        if (d0 > this.boostTrigger) {
            this.boost();
        }

        Quaternionf quaternion41 = new Quaternionf();
        float yaw = Math.toRadians(-this.horse.yBodyRot);
        Quaternionf quaternion5 = new Quaternionf().fromAxisAngleRad(Utils.PITCH, 0.0F);
        Quaternionf quaternion12 = new Quaternionf().fromAxisAngleRad(Utils.YAW, yaw);
        Quaternionf quaternion22 = new Quaternionf().fromAxisAngleRad(Utils.ROLL, 0.0F);
        Quaternionf quaternion31 = quaternion12.mul(quaternion5, new Quaternionf()).mul(quaternion22, new Quaternionf());
        Quaternionf quaternion = quaternion41.set(quaternion31);
        Vec3 vec32 = Utils.convertToVec3(quaternion.transformUnit(Utils.forward, new Vector3f()));
        Vec3 vec33 = Utils.convertToVec3(quaternion.transformUnit(Utils.right, new Vector3f()));
        Vec3 vec34 = Utils.convertToVec3(quaternion.transformUnit(Utils.left, new Vector3f()));
        Quaternionf quaternion4 = new Quaternionf();
        Quaternionf quaternion21 = new Quaternionf().fromAxisAngleRad(Utils.PITCH, 0.0F);
        Quaternionf quaternion11 = new Quaternionf().fromAxisAngleRad(Utils.YAW, Math.toRadians(VRSettings.inst.worldRotation));
        Quaternionf quaternion2 = new Quaternionf().fromAxisAngleRad(Utils.ROLL, 0.0F);
        Quaternionf quaternion3 = quaternion11.mul(quaternion21, new Quaternionf()).mul(quaternion2, new Quaternionf());
        Quaternionf quaternion1 = quaternion4.set(quaternion3);
        Vec3 vec2 = this.dh.vr.controllerHistory[1].latest();
        Vec3 vec35 = VRPlayer.get().roomOrigin.add(Utils.convertToVec3(quaternion1.transformUnit(new Vector3f((float) vec2.x, (float) vec2.y, (float) vec2.z), new Vector3f())));
        Vec3 vec1 = this.dh.vr.controllerHistory[0].latest();
        Vec3 vec36 = VRPlayer.get().roomOrigin.add(Utils.convertToVec3(quaternion1.transformUnit(new Vector3f((float) vec1.x, (float) vec1.y, (float) vec1.z), new Vector3f())));
        double d1 = vec35.subtract(this.info.leftReinPos).dot(vec32) + vec35.subtract(this.info.leftReinPos).dot(vec33);
        double d2 = vec36.subtract(this.info.rightReinPos).dot(vec32) + vec36.subtract(this.info.rightReinPos).dot(vec34);

        if (this.speedLevel < 0) {
            this.speedLevel = 0;
        }

        if (d1 > this.pullTrigger + 0.3D && d2 > this.pullTrigger + 0.3D && Math.abs(d2 - d1) < 0.1D) {
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

        this.horse.yBodyRot = (float) (Math.abs((double) f - (double) f1) < 360.0D / 2.0D ? (double) f1 + ((double) f - (double) f1) * this.bodyturnspeed : (double) f1 + ((double) f - (double) f1 - Math.signum((double) f - (double) f1) * 360.0D) * this.bodyturnspeed);
        this.horse.yHeadRot = f;
        Vec3 vec = new Vec3(0.0D, 0.0D, (double) this.speedLevel * this.baseSpeed);
        Vec3 vec37 = Utils.convertToVec3(quaternion.transformUnit(new Vector3f((float) vec.x, (float) vec.y, (float) vec.z), new Vector3f()));
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
            System.out.println("Breaking");
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
