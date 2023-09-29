package org.vivecraft.client_vr;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.client_vr.render.RenderPass;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.common.utils.Utils.*;

@ParametersAreNonnullByDefault
public class VRData {
    @Nonnull
    public VRDevicePose hmd;
    @Nonnull
    public VRDevicePose eye0;
    @Nonnull
    public VRDevicePose eye1;
    @Nonnull
    public VRDevicePose c0;
    @Nonnull
    public VRDevicePose c1;
    @Nonnull
    public VRDevicePose c2;
    @Nonnull
    public VRDevicePose h0;
    @Nonnull
    public VRDevicePose h1;
    //    @Nonnull
//    public VRDevicePose g0;
//    @Nonnull
//    public VRDevicePose g1;
    @Nonnull
    public VRDevicePose t0;
    @Nonnull
    public VRDevicePose t1;
    @Nonnull
    public VRDevicePose cam;
    @Nonnull
    public Vec3 origin;
    public float rotation_radians;
    public float worldScale;

    public VRData() {
        this(new Vec3(0.0D, 0.0D, 0.0D), 1.0F, 0.0F);
    }

    public VRData(Vec3 origin, float worldScale, float rotation_radians) {
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotation_radians = rotation_radians;
        Vec3 vec3 = convertToVec3(dh.vr.getCenterEyePosition());
        Vec3 vec31 = new Vec3(vec3.x * (double) dh.vrSettings.walkMultiplier, vec3.y, vec3.z * (double) dh.vrSettings.walkMultiplier);
        this.hmd = new VRDevicePose(this, dh.vr.hmdRotation, vec31, convertToVec3(dh.vr.getHmdVector()));
        this.eye0 = new VRDevicePose(this, dh.vr.getEyeRotation(RenderPass.LEFT), dh.vr.getEyePosition(RenderPass.LEFT).subtract(vec3).add(vec31), convertToVec3(dh.vr.getHmdVector()));
        this.eye1 = new VRDevicePose(this, dh.vr.getEyeRotation(RenderPass.RIGHT), dh.vr.getEyePosition(RenderPass.RIGHT).subtract(vec3).add(vec31), convertToVec3(dh.vr.getHmdVector()));
        this.c0 = new VRDevicePose(this, dh.vr.getAimRotation(0), dh.vr.getAimSource(0).subtract(vec3).add(vec31), dh.vr.getAimVector(0));
        this.c1 = new VRDevicePose(this, dh.vr.getAimRotation(1), dh.vr.getAimSource(1).subtract(vec3).add(vec31), dh.vr.getAimVector(1));
//        this.g0 = new VRDevicePose(this, new Matrix4d(), dh.vr.getGesturePosition(0), dh.vr.getGestureVector(0));
//        this.g1 = new VRDevicePose(this, new Matrix4d(), dh.vr.getGesturePosition(1), dh.vr.getGestureVector(1));
        this.h0 = new VRDevicePose(this, dh.vr.getHandRotation(0), dh.vr.getAimSource(0).subtract(vec3).add(vec31), dh.vr.getHandVector(0));
        this.h1 = new VRDevicePose(this, dh.vr.getHandRotation(1), dh.vr.getAimSource(1).subtract(vec3).add(vec31), dh.vr.getHandVector(1));

        if (dh.vrSettings.seated) {
            this.t0 = eye0;
            this.t1 = eye1;
        } else {
            Matrix4f matrix4f = this.getSmoothedRotation(0, 0.2F);
            Matrix4f matrix4f1 = this.getSmoothedRotation(1, 0.2F);
            this.t0 = new VRDevicePose(this, matrix4f, dh.vr.getAimSource(0).subtract(vec3).add(vec31), convertToVec3(matrix4f.transformProject(forward, new Vector3f())));
            this.t1 = new VRDevicePose(this, matrix4f1, dh.vr.getAimSource(1).subtract(vec3).add(vec31), convertToVec3(matrix4f1.transformProject(forward, new Vector3f())));
        }

        Matrix4f matrix4f2 = new Matrix4f().rotationY(-rotation_radians).rotate(dh.cameraTracker.getRotation());
        float inverseWorldScale = 1.0F / worldScale;
        this.cam = new VRDevicePose(this, matrix4f2, dh.cameraTracker.getPosition().subtract(origin).yRot(-rotation_radians).multiply(inverseWorldScale, inverseWorldScale, inverseWorldScale).subtract(vec3).add(vec31), convertToVec3(new Vector3f(forward).mulProject(matrix4f2)));

        if (dh.vr.mrMovingCamActive) {
            this.c2 = new VRDevicePose(this, dh.vr.getAimRotation(2), dh.vr.getAimSource(2).subtract(vec3).add(vec31), dh.vr.getAimVector(2));
        } else {
            Matrix4f matrix4f3 = new Matrix4f().set(dh.vrSettings.vrFixedCamrotQuat);
            Vec3 vec32 = new Vec3(dh.vrSettings.vrFixedCamposX, dh.vrSettings.vrFixedCamposY, dh.vrSettings.vrFixedCamposZ);
            Vec3 vec33 = convertToVec3(matrix4f3.transformProject(forward, new Vector3f()));
            this.c2 = new VRDevicePose(this, matrix4f3, vec32.subtract(vec3).add(vec31), vec33);
        }
    }

    private Matrix4f getSmoothedRotation(int c, float lenSec) {
        // Vector3f vec3 = dh.vr.controllerHistory[c].averagePosition(lenSec, new Vector3f());
        Vector3f vec31 = dh.vr.controllerForwardHistory[c].averagePosition(lenSec, new Vector3f());
        Vector3f vec32 = dh.vr.controllerUpHistory[c].averagePosition(lenSec, new Vector3f());
        Vector3f vec33 = vec31.cross(vec32, new Vector3f());
        return new Matrix4f(new Matrix3f().set(vec31, vec32, vec33).transpose());
    }

    public VRDevicePose getController(int c) {
        return c == 1 ? this.c1 : (c == 2 ? this.c2 : this.c0);
    }

    public VRDevicePose getHand(int c) {
        return c == 0 ? this.h0 : this.h1;
    }

//    public VRDevicePose getGesture(int c)
//    {
//        return c == 0 ? this.g0 : this.g1;
//    }

    public float getBodyYaw() {
        if (dh.vrSettings.seated) {
            return this.hmd.getYaw();
        } else {
            Vector3f vec3 = convertToVector3f(this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float) PI / 2F)), new Vector3f());
            Vector3f vec31 = convertToVector3f(this.hmd.getDirection(), new Vector3f());

            if (vec3.dot(vec31) < 0.0D) {
                vec3.negate();
            }

            vec31.lerp(vec3, 0.7F, vec3);
            return (float) toDegrees(atan2(-vec3.x, vec3.z));
        }
    }

    public float getFacingYaw() {
        if (dh.vrSettings.seated) {
            return this.hmd.getYaw();
        } else {
            Vec3 vec3 = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float) PI / 2F));
            return (float) toDegrees(dh.vrSettings.reverseHands ? atan2(vec3.x, -vec3.z) : atan2(-vec3.x, vec3.z));
        }
    }

    public Vec3 getHeadPivot() {
        // scale pivot point with world scale, to prevent unwanted player movement
        return convertToVec3(this.hmd.getMatrix()
            .transformProject(0.0F, -0.1F * worldScale, 0.1F * worldScale, new Vector3f())
            .add(this.hmd.getPosition().toVector3f())
        );
    }

    public Vec3 getHeadRear() {
        return convertToVec3(this.hmd.getMatrix()
            .transformProject(0.0F, -0.2F, 0.2F, new Vector3f())
            .add(this.hmd.getPosition().toVector3f())
        );
    }

    public VRDevicePose getEye(RenderPass pass) {
        return switch (pass) {
            case LEFT -> {
                yield this.eye0;
            }
            case RIGHT -> {
                yield this.eye1;
            }
            case THIRD -> {
                yield this.c2;
            }
            case SCOPER -> {
                yield this.t0;
            }
            case SCOPEL -> {
                yield this.t1;
            }
            case CAMERA -> {
                yield this.cam;
            }
            default -> {
                yield this.hmd;
            }
        };
    }

    public String toString() {
        return "data:\r\n \t\t origin: " + this.origin + "\r\n \t\t rotation: " + String.format("%.2f", this.rotation_radians) + "\r\n \t\t scale: " + String.format("%.2f", this.worldScale) + "\r\n \t\t hmd " + this.hmd + "\r\n \t\t c0 " + this.c0 + "\r\n \t\t c1 " + this.c1 + "\r\n \t\t c2 " + this.c2;
    }

    @ParametersAreNonnullByDefault
    public class VRDevicePose {
        final VRData data;
        final Vec3 pos;
        final Vec3 dir;
        final Matrix4f matrix;

        public VRDevicePose(VRData data, Matrix4f matrix, Vec3 pos, Vec3 dir) {
            this.data = data;
            this.matrix = new Matrix4f(matrix);
            this.pos = new Vec3(pos.x, pos.y, pos.z);
            this.dir = new Vec3(dir.x, dir.y, dir.z);
        }

        public Vec3 getPosition() {
            return this.pos.scale(this.data.worldScale).yRot(this.data.rotation_radians).add(this.data.origin);
        }

        public Vec3 getDirection() {
            return this.dir.yRot(this.data.rotation_radians);
        }

        public Vec3 getCustomVector(Vec3 axis) {
            return convertToVec3(
                this.matrix.transformProject(axis.toVector3f()).rotateY(this.data.rotation_radians)
            );
        }

        public float getYaw() {
            Vec3 vec3 = this.getDirection();
            return (float) toDegrees(atan2(-vec3.x, vec3.z));
        }

        public float getPitch() {
            Vec3 vec3 = this.getDirection();
            return (float) toDegrees(asin(vec3.y / vec3.length()));
        }

        public float getRoll() {
            return (float) (-toDegrees(atan2(this.matrix.m10(), this.matrix.m11())));
        }

        public Matrix4f getMatrix() {
            return new Matrix4f(this.matrix).rotateY(this.data.rotation_radians);
        }

        public String toString() {
            return "Device: pos:" + this.getPosition() + " dir: " + this.getDirection();
        }
    }
}
