package org.vivecraft.client_vr.gameplay.trackers;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.backward;
import static org.vivecraft.common.utils.Utils.convertToVec3;

import static org.joml.Math.*;

public class RowTracker extends Tracker
{
    Vec3[] lastUWPs = new Vec3[2];
    public double[] forces = new double[] {0.0D, 0.0D};
    double transmissionEfficiency = 0.9D;
    public float LOar;
    public float ROar;
    public float FOar;

    public boolean isActive()
    {
        if (dh.vrSettings.seated)
        {
            return false;
        }
        else if (!dh.vrSettings.realisticRowEnabled)
        {
            return false;
        }
        else if (mc.player != null && mc.player.isAlive())
        {
            if (mc.gameMode == null)
            {
                return false;
            }
            else if (mc.options.keyUp.isDown())
            {
                return false;
            }
            else if (!(mc.player.getVehicle() instanceof Boat))
            {
                return false;
            }
            else
            {
                return !dh.bowTracker.isNotched();
            }
        }
        else
        {
            return false;
        }
    }

    public boolean isRowing()
    {
        return this.ROar + this.LOar + this.FOar > 0.0F;
    }

    public void reset()
    {
        this.LOar = 0.0F;
        this.ROar = 0.0F;
        this.FOar = 0.0F;
    }

    public void doProcess()
    {
        double d0 = dh.vr.controllerHistory[0].averageSpeed(0.5D);
        double d1 = dh.vr.controllerHistory[1].averageSpeed(0.5D);
        float f = 0.5F;
        float f1 = 2.0F;
        this.ROar = (float) max(d0 - (double) f, 0.0D);
        this.LOar = (float) max(d1 - (double) f, 0.0D);
        this.FOar = this.ROar > 0.0F && this.LOar > 0.0F ? (this.ROar + this.LOar) / 2.0F : 0.0F;

        if (this.FOar > f1)
        {
            this.FOar = f1;
        }

        if (this.ROar > f1)
        {
            this.ROar = f1;
        }

        if (this.LOar > f1)
        {
            this.LOar = f1;
        }
    }

    public void doProcessFinaltransmithastofixthis()
    {
        Boat boat = (Boat)mc.player.getVehicle();
        Quaternionf quaternion = new Quaternionf().setAngleAxis(-(boat.getYRot() % 360.0F), 0.0F, 1.0F, 0.0F)
            .mul(new Quaternionf().setAngleAxis(boat.getXRot(), 1.0F, 0.0F, 0.0F))
            .mul(new Quaternionf().setAngleAxis(0.0F, 0.0F, 0.0F, 1.0F));
        if (sqrt(quaternion.lengthSquared()) > 0.0F)
        {
            quaternion.normalize();
        }
        else
        {
            quaternion.identity();
        }

        for (int i = 0; i <= 1; ++i)
        {
            if (!this.isPaddleUnderWater(i, boat))
            {
                this.forces[i] = 0.0D;
                this.lastUWPs[i] = null;
            }
            else
            {
                Vec3 vec3 = this.getArmToPaddleVector(i, boat);
                Vec3 vec31 = this.getAttachmentPoint(i, boat);
                Vec3 vec32 = vec31.add(vec3.normalize()).subtract(boat.position());

                if (this.lastUWPs[i] != null)
                {
                    Vec3 vec33 = this.lastUWPs[i].subtract(vec32);
                    vec33 = vec33.subtract(boat.getDeltaMovement());
                    Vec3 vec34 = convertToVec3(quaternion.transformUnit(new Vector3f(backward)));
                    double d0 = vec33.dot(vec34) * this.transmissionEfficiency / 5.0D;

                    if ((!(d0 < 0.0D) || !(this.forces[i] > 0.0D)) && (!(d0 > 0.0D) || !(this.forces[i] < 0.0D)))
                    {
                        this.forces[i] = min(max(d0, -0.1D), 0.1D);
                    }
                    else
                    {
                        this.forces[i] = 0.0D;
                    }
                }

                this.lastUWPs[i] = vec32;
            }
        }
    }

    Vec3 getArmToPaddleVector(int paddle, Boat boat)
    {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getAbsArmPos(paddle == 0 ? 1 : 0);
        return vec3.subtract(vec31);
    }

    Vec3 getAttachmentPoint(int paddle, Boat boat)
    {
        Quaternionf quaternion = new Quaternionf().setAngleAxis(-(boat.getYRot() % 360.0F), 0.0F, 1.0F, 0.0F)
            .mul(new Quaternionf().setAngleAxis(boat.getXRot(), 1.0F, 0.0F, 0.0F))
            .mul(new Quaternionf().setAngleAxis(0.0F, 0.0F, 0.0F, 1.0F));
        if (sqrt(quaternion.lengthSquared()) > 0.0F)
        {
            quaternion.normalize();
        }
        else
        {
            quaternion.identity();
        }
        return boat.position().add(convertToVec3(quaternion.transformUnit(new Vector3f(
            (paddle == 0 ? 9.0F : -9.0F) / 16.0F, 0.625F, 0.1875F)
        )));
    }

    Vec3 getAbsArmPos(int side)
    {
        return dh.vrPlayer.roomOrigin.add(convertToVec3(
            new Quaternionf().setAngleAxis(toRadians(dh.vrSettings.worldRotation), 0.0F, 1.0F, 0.0F)
                .mul(new Quaternionf().setAngleAxis(0.0F, 1.0F, 0.0F, 0.0F))
                .mul(new Quaternionf().setAngleAxis(0.0F, 0.0F, 0.0F, 1.0F))
                .transformUnit(new Vector3f().set(dh.vr.controllerHistory[side].averagePosition(0.1D, new Vector3d())))
        ));
    }

    boolean isPaddleUnderWater(int paddle, Boat boat)
    {
        Vec3 vec3 = this.getAttachmentPoint(paddle, boat);
        Vec3 vec31 = this.getArmToPaddleVector(paddle, boat).normalize();
        BlockPos blockpos = BlockPos.containing(vec3.add(vec31));
        // TODO: liquid is deprecated
        return boat.level().getBlockState(blockpos).liquid();
    }
}
