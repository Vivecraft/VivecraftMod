package org.vivecraft.common.api_impl.data;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.vivecraft.api.data.VRPose;

public class VRPoseImpl implements VRPose {

    private final Vec3 pos;
    private final Vec3 rot;
    private final Quaternionf quaternion;

    public VRPoseImpl(Vec3 pos, Vec3 rot, Quaternionf quaternion) {
        this.pos = pos;
        this.rot = rot;
        this.quaternion = quaternion;
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
    public double getPitch() {
        return Math.asin(this.rot.y / this.rot.length());
    }

    @Override
    public double getYaw() {
        return Math.atan2(-this.rot.x, this.rot.z);
    }

    @Override
    public double getRoll() {
        return -Math.atan2(2.0F * (quaternion.x * quaternion.y + quaternion.w * quaternion.z),
            quaternion.w * quaternion.w - quaternion.x * quaternion.x + quaternion.y * quaternion.y - quaternion.z * quaternion.z);
    }

    @Override
    public Quaternionf getQuaternion() {
        return this.quaternion;
    }

    @Override
    public String toString() {
        return "Position: " + getPos() + "\tRotation: " + getRot();
    }
}
