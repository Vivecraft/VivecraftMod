package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.Utils;

public class RowTracker extends Tracker {
    Vec3[] lastUWPs = new Vec3[2];
    public double[] forces = new double[]{0.0D, 0.0D};
    double transmissionEfficiency = 0.9D;
    public float LOar;
    public float ROar;
    public float Foar;

    public RowTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p) {
        if (ClientDataHolderVR.getInstance().vrSettings.seated) {
            return false;
        } else if (!ClientDataHolderVR.getInstance().vrSettings.realisticRowEnabled) {
            return false;
        } else if (p != null && p.isAlive()) {
            if (this.mc.gameMode == null) {
                return false;
            } else if (Minecraft.getInstance().options.keyUp.isDown()) {
                return false;
            } else if (!(p.getVehicle() instanceof Boat)) {
                return false;
            } else {
                return !ClientDataHolderVR.getInstance().bowTracker.isNotched();
            }
        } else {
            return false;
        }
    }

    public boolean isRowing() {
        return this.ROar + this.LOar + this.Foar > 0.0F;
    }

    public void reset(LocalPlayer player) {
        this.LOar = 0.0F;
        this.ROar = 0.0F;
        this.Foar = 0.0F;
    }

    public void doProcess(LocalPlayer player) {
        double d0 = this.dh.vr.controllerHistory[0].averageSpeed(0.5D);
        double d1 = this.dh.vr.controllerHistory[1].averageSpeed(0.5D);
        float f = 0.5F;
        float f1 = 2.0F;
        this.ROar = (float) Math.max(d0 - (double) f, 0.0D);
        this.LOar = (float) Math.max(d1 - (double) f, 0.0D);
        this.Foar = this.ROar > 0.0F && this.LOar > 0.0F ? (this.ROar + this.LOar) / 2.0F : 0.0F;

        if (this.Foar > f1) {
            this.Foar = f1;
        }

        if (this.ROar > f1) {
            this.ROar = f1;
        }

        if (this.LOar > f1) {
            this.LOar = f1;
        }
    }

    public void doProcessFinaltransmithastofixthis(LocalPlayer player) {
        Boat boat = (Boat) player.getVehicle();
        Quaternionf quaternion4 = new Quaternionf();
        float pitch = Math.toRadians(boat.getXRot());
        float yaw = Math.toRadians(-(boat.getYRot() % 360.0F));
        Quaternionf quaternion21 = new Quaternionf().fromAxisAngleRad(Utils.PITCH, pitch);
        Quaternionf quaternion11 = new Quaternionf().fromAxisAngleRad(Utils.YAW, yaw);
        Quaternionf quaternion2 = new Quaternionf().fromAxisAngleRad(Utils.ROLL, 0.0F);
        Quaternionf quaternion3 = quaternion11.mul(quaternion21, new Quaternionf()).mul(quaternion2, new Quaternionf());
        Quaternionf quaternion1 = (quaternion4.set(quaternion3));
        Quaternionf dest = new Quaternionf();
        float f4 = Math.sqrt(quaternion1.lengthSquared());

        if (f4 > 0.0F) {
            dest.normalize();
        } else {
            dest.identity();
        }

        Quaternionf quaternion = dest;

        for (int i = 0; i <= 1; ++i) {
            if (!this.isPaddleUnderWater(i, boat)) {
                this.forces[i] = 0.0D;
                this.lastUWPs[i] = null;
            } else {
                Vec3 vec3 = this.getArmToPaddleVector(i, boat);
                Vec3 vec31 = this.getAttachmentPoint(i, boat);
                Vec3 vec32 = vec31.add(vec3.normalize()).subtract(boat.position());

                if (this.lastUWPs[i] != null) {
                    Vec3 vec33 = this.lastUWPs[i].subtract(vec32);
                    vec33 = vec33.subtract(boat.getDeltaMovement());
                    Vec3 vec = new Vec3(0.0D, 0.0D, 1.0D);
                    Vec3 vec34 = Utils.convertToVec3(quaternion.transformUnit(new Vector3f((float) vec.x, (float) vec.y, (float) vec.z), new Vector3f()));
                    double d0 = vec33.dot(vec34) * this.transmissionEfficiency / 5.0D;

                    if ((!(d0 < 0.0D) || !(this.forces[i] > 0.0D)) && (!(d0 > 0.0D) || !(this.forces[i] < 0.0D))) {
                        this.forces[i] = Math.min(Math.max(d0, -0.1D), 0.1D);
                    } else {
                        this.forces[i] = 0.0D;
                    }
                }

                this.lastUWPs[i] = vec32;
            }
        }
    }

    Vec3 getArmToPaddleVector(int paddle, Boat boat) {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getAbsArmPos(paddle == 0 ? 1 : 0);
        return vec3.subtract(vec31);
    }

    Vec3 getAttachmentPoint(int paddle, Boat boat) {
        Vec3 vec3 = new Vec3((paddle == 0 ? 9.0F : -9.0F) / 16.0F, 0.625D, 0.1875D);
        Quaternionf quaternion4 = new Quaternionf();
        float pitch = Math.toRadians(boat.getXRot());
        float yaw = Math.toRadians(-(boat.getYRot() % 360.0F));
        Quaternionf quaternion21 = new Quaternionf().fromAxisAngleRad(Utils.PITCH, pitch);
        Quaternionf quaternion11 = new Quaternionf().fromAxisAngleRad(Utils.YAW, yaw);
        Quaternionf quaternion2 = new Quaternionf().fromAxisAngleRad(Utils.ROLL, 0.0F);
        Quaternionf quaternion3 = quaternion11.mul(quaternion21, new Quaternionf()).mul(quaternion2, new Quaternionf());
        Quaternionf quaternion1 = (quaternion4.set(quaternion3));
        Quaternionf dest = new Quaternionf();
        float f4 = Math.sqrt(quaternion1.lengthSquared());

        if (f4 > 0.0F) {
            dest.normalize();
        } else {
            dest.identity();
        }

        Quaternionf quaternion = dest;
        return boat.position().add(Utils.convertToVec3(quaternion.transformUnit(new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z), new Vector3f())));
    }

    Vec3 getAbsArmPos(int side) {
        Vec3 vec3 = this.dh.vr.controllerHistory[side].averagePosition(0.1D);
        Quaternionf quaternion4 = new Quaternionf();
        Quaternionf quaternion11 = new Quaternionf().fromAxisAngleRad(Utils.PITCH, 0.0F);
        Quaternionf quaternion1 = new Quaternionf().fromAxisAngleRad(Utils.YAW, Math.toRadians(VRSettings.inst.worldRotation));
        Quaternionf quaternion2 = new Quaternionf().fromAxisAngleRad(Utils.ROLL, 0.0F);
        Quaternionf quaternion3 = quaternion1.mul(quaternion11, new Quaternionf()).mul(quaternion2, new Quaternionf());
        Quaternionf quaternion = quaternion4.set(quaternion3);
        return VRPlayer.get().roomOrigin.add(Utils.convertToVec3(quaternion.transformUnit(new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z), new Vector3f())));
    }

    boolean isPaddleUnderWater(int paddle, Boat boat) {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getArmToPaddleVector(paddle, boat).normalize();
        BlockPos blockpos = BlockPos.containing(vec3.add(vec31));
        // TODO: liquid is deprecated
        return boat.level().getBlockState(blockpos).liquid();
    }
}
