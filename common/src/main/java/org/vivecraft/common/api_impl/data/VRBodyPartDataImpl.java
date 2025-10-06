package org.vivecraft.common.api_impl.data;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.vivecraft.api.data.VRBodyPartData;

public record VRBodyPartDataImpl(Vec3 pos, Vec3 dir, Quaternionfc rot) implements VRBodyPartData {

    @Override
    public Vec3 getPos() {
        return this.pos;
    }

    @Override
    public Vec3 getDir() {
        return this.dir;
    }

    @Override
    public double getPitch() {
        return Math.asin(this.dir.y / this.dir.length());
    }

    @Override
    public double getYaw() {
        return Math.atan2(-this.dir.x, this.dir.z);
    }

    @Override
    public double getRoll() {
        return -Math.atan2(2.0F * (this.rot.x() * this.rot.y() + this.rot.w() * this.rot.z()),
            this.rot.w() * this.rot.w() - this.rot.x() * this.rot.x() +
                this.rot.y() * this.rot.y() -
                this.rot.z() * this.rot.z());
    }

    @Override
    public Quaternionfc getRotation() {
        return this.rot;
    }

    @Override
    public String toString() {
        return """
            Position: %s
            Direction: %s
            Rotation: %s
            """.formatted(this.pos, this.dir, this.rot);
    }
}
