package org.vivecraft.client_vr;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
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
    public Vector3f origin;
    public float rotation_radians;
    public float worldScale;

    public VRData() {
        this(new Vector3f(), 1.0F, 0.0F);
    }

    public VRData(Vector3fc origin, float worldScale, float rotation_radians) {
        this.origin = new Vector3f(origin);
        this.worldScale = worldScale;
        this.rotation_radians = rotation_radians;
        Vector3fc hmd_raw = dh.vr.getCenterEyePosition();
        Vector3f scaledPos = new Vector3f(hmd_raw.x() * dh.vrSettings.walkMultiplier, hmd_raw.y(), hmd_raw.z() * dh.vrSettings.walkMultiplier);
        this.hmd = new VRDevicePose(this, dh.vr.hmdRotation, scaledPos, dh.vr.getHmdVector());
        this.eye0 = new VRDevicePose(this, dh.vr.getEyeRotation(RenderPass.LEFT), dh.vr.getEyePosition(RenderPass.LEFT).sub(hmd_raw).add(scaledPos), dh.vr.getHmdVector());
        this.eye1 = new VRDevicePose(this, dh.vr.getEyeRotation(RenderPass.RIGHT), dh.vr.getEyePosition(RenderPass.RIGHT).sub(hmd_raw).add(scaledPos), dh.vr.getHmdVector());
        this.c0 = new VRDevicePose(this, dh.vr.getAimRotation(0), dh.vr.getAimSource(0, new Vector3f()).sub(hmd_raw).add(scaledPos), dh.vr.getAimVector(0, new Vector3f()));
        this.c1 = new VRDevicePose(this, dh.vr.getAimRotation(1), dh.vr.getAimSource(1, new Vector3f()).sub(hmd_raw).add(scaledPos), dh.vr.getAimVector(1, new Vector3f()));
//        this.g0 = new VRDevicePose(this, new Matrix4d(), dh.vr.getGesturePosition(0), dh.vr.getGestureVector(0));
//        this.g1 = new VRDevicePose(this, new Matrix4d(), dh.vr.getGesturePosition(1), dh.vr.getGestureVector(1));
        this.h0 = new VRDevicePose(this, dh.vr.getHandRotation(0), dh.vr.getAimSource(0, new Vector3f()).sub(hmd_raw).add(scaledPos), dh.vr.getHandVector(0, new Vector3f()));
        this.h1 = new VRDevicePose(this, dh.vr.getHandRotation(1), dh.vr.getAimSource(1, new Vector3f()).sub(hmd_raw).add(scaledPos), dh.vr.getHandVector(1, new Vector3f()));

        if (dh.vrSettings.seated) {
            this.t0 = eye0;
            this.t1 = eye1;
        } else {
            Matrix4f s0 = this.getSmoothedRotation(0, 0.2F);
            Matrix4f s1 = this.getSmoothedRotation(1, 0.2F);
            this.t0 = new VRDevicePose(this, s0, dh.vr.getAimSource(0, new Vector3f()).sub(hmd_raw).add(scaledPos), s0.transformProject(forward, new Vector3f()));
            this.t1 = new VRDevicePose(this, s1, dh.vr.getAimSource(1, new Vector3f()).sub(hmd_raw).add(scaledPos), s1.transformProject(forward, new Vector3f()));
        }

        Matrix4f camRot = new Matrix4f().rotationY(-rotation_radians).rotate(dh.cameraTracker.getRotation());
        float inverseWorldScale = 1.0F / worldScale;
        this.cam = new VRDevicePose(this, camRot, dh.cameraTracker.getPosition().sub(origin, new Vector3f()).rotateY(-rotation_radians).mul(inverseWorldScale).sub(hmd_raw).add(scaledPos), new Vector3f(forward).mulProject(camRot));

        if (dh.vr.mrMovingCamActive) {
            this.c2 = new VRDevicePose(this, dh.vr.getAimRotation(2), dh.vr.getAimSource(2, new Vector3f()).sub(hmd_raw).add(scaledPos), dh.vr.getAimVector(2, new Vector3f()));
        } else {
            Matrix4f rot = new Matrix4f().set(dh.vrSettings.vrFixedCamrotQuat);
            Vector3f pos = new Vector3f(dh.vrSettings.vrFixedCamposX, dh.vrSettings.vrFixedCamposY, dh.vrSettings.vrFixedCamposZ);
            Vector3f dir = rot.transformProject(forward, new Vector3f());
            this.c2 = new VRDevicePose(this, rot, pos.sub(hmd_raw).add(scaledPos), dir);
        }
    }

    private Matrix4f getSmoothedRotation(int c, float lenSec) {
        // Vector3f pos = dh.vr.controllerHistory[c].averagePosition(lenSec, new Vector3f());
        Vector3f u = dh.vr.controllerForwardHistory[c].averagePosition(lenSec, new Vector3f());
        Vector3f f = dh.vr.controllerUpHistory[c].averagePosition(lenSec, new Vector3f());
        Vector3f r = u.cross(f, new Vector3f());
        return new Matrix4f(new Matrix3f().set(u, f, r).transpose());
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
            Vector3f v = this.c1.getPosition(new Vector3f()).sub(this.c0.getPosition(new Vector3f())).normalize().rotateY((-(float) PI / 2F));
            Vector3f h = this.hmd.getDirection(new Vector3f());

            if (v.dot(h) < 0.0D) {
                v.negate();
            }

            h.lerp(v, 0.7F, v);
            return (float) toDegrees(atan2(-v.x, v.z));
        }
    }

    public float getFacingYaw() {
        if (dh.vrSettings.seated) {
            return this.hmd.getYaw();
        } else {
            Vector3f v = this.c1.getPosition(new Vector3f()).sub(this.c0.getPosition(new Vector3f())).normalize().rotateY((-(float) PI / 2F));
            return (float) toDegrees(dh.vrSettings.reverseHands ? atan2(v.x, -v.z) : atan2(-v.x, v.z));
        }
    }

    public Vector3f getHeadPivot(Vector3f dest) {
        // scale pivot point with world scale, to prevent unwanted player movement
        return dest.set(this.hmd.getMatrix()
            .transformProject(0.0F, -0.1F * worldScale, 0.1F * worldScale, new Vector3f())
            .add(this.hmd.getPosition(new Vector3f()))
        );
    }

    public Vec3 getHeadRear() {
        return convertToVec3(this.hmd.getMatrix()
            .transformProject(0.0F, -0.2F, 0.2F, new Vector3f())
            .add(this.hmd.getPosition(new Vector3f()))
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
    public static class VRDevicePose {
        final VRData data;
        final Vector3fc pos;
        final Vector3fc dir;
        final Matrix4f matrix;

        public VRDevicePose(VRData data, Matrix4f matrix, Vector3fc pos, Vector3fc dir) {
            this.data = data;
            this.matrix = new Matrix4f(matrix);
            this.pos = new Vector3f(pos);
            this.dir = new Vector3f(dir);
        }

        public Vector3f getPosition(@Nonnull Vector3f dest) {
            return this.pos.mul(this.data.worldScale, dest).rotateY(this.data.rotation_radians).add(this.data.origin);
        }

        public Vector3f getDirection(@Nonnull Vector3f dest) {
            return this.dir.rotateY(this.data.rotation_radians, dest);
        }

        public Vector3f getCustomVector(Vector3f axis) {
            return this.matrix.transformProject(axis).rotateY(this.data.rotation_radians);
        }

        public float getYaw() {
            Vector3f dir = this.getDirection(new Vector3f());
            return (float) toDegrees(atan2(-dir.x, dir.z));
        }

        public float getPitch() {
            Vector3f dir = this.getDirection(new Vector3f());
            return (float) toDegrees(asin(dir.y / dir.length()));
        }

        public float getRoll() {
            return (float) (-toDegrees(atan2(this.matrix.m10(), this.matrix.m11())));
        }

        public Matrix4f getMatrix() {
            return new Matrix4f(this.matrix).rotateY(this.data.rotation_radians);
        }

        public String toString() {
            return "Device: pos:" + this.getPosition(new Vector3f()) + " dir: " + this.getDirection(new Vector3f());
        }
    }
}
