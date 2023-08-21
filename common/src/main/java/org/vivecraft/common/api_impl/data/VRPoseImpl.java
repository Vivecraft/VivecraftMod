package org.vivecraft.common.api_impl.data;

import net.minecraft.world.phys.Vec3;
import org.vivecraft.api_beta.data.VRPose;

public class VRPoseImpl implements VRPose {

    private final Vec3 pos;
    private final Vec3 rot;
    private final double rollDeg;

    public VRPoseImpl(Vec3 pos, Vec3 rot, double rollDeg) {
        this.pos = pos;
        this.rot = rot;
        this.rollDeg = rollDeg;
    }

    @Override
    public Vec3 getPos() {
        return this.pos;
    }

    @Override
    public Vec3 getRot() {
        return this.rot;
    }

    @Override
    public double getPitchRad() {
        return Math.asin(this.rot.y / this.rot.length());
    }

    @Override
    public double getYawRad() {
        return Math.atan2(-this.rot.x, this.rot.z);
    }

    @Override
    public double getRollDeg() {
        return this.rollDeg;
    }

    @Override
    public String toString() {
        return "Position: " + getPos() + "\tRotation: " + getRot();
    }
}
