package org.vivecraft.client_vr;

import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.joml.Math;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.common.utils.Utils;

public class VRData {
    public VRDevicePose hmd;
    public VRDevicePose eye0;
    public VRDevicePose eye1;
    public VRDevicePose c0;
    public VRDevicePose c1;
    public VRDevicePose c2;
    public VRDevicePose h0;
    public VRDevicePose h1;
    public VRDevicePose t0;
    public VRDevicePose t1;
    public VRDevicePose cam;
    public Vec3 origin;
    public float rotation_radians;
    public float worldScale;

    public VRData(Vec3 origin, float walkMul, float worldScale, float rotation) {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotation_radians = rotation;
        Vec3 vec3 = dataholder.vr.getCenterEyePosition();
        Vec3 vec31 = new Vec3(vec3.x * (double) walkMul, vec3.y, vec3.z * (double) walkMul);
        this.hmd = new VRDevicePose(this, dataholder.vr.hmdRotation, vec31, dataholder.vr.getHmdVector());
        this.eye0 = new VRDevicePose(this, dataholder.vr.getEyeRotation(RenderPass.LEFT, new Matrix4f()), dataholder.vr.getEyePosition(RenderPass.LEFT).subtract(vec3).add(vec31), dataholder.vr.getHmdVector());
        this.eye1 = new VRDevicePose(this, dataholder.vr.getEyeRotation(RenderPass.RIGHT, new Matrix4f()), dataholder.vr.getEyePosition(RenderPass.RIGHT).subtract(vec3).add(vec31), dataholder.vr.getHmdVector());
        this.c0 = new VRDevicePose(this, dataholder.vr.getAimRotation(0, new Matrix4f()), dataholder.vr.getAimSource(0).subtract(vec3).add(vec31), dataholder.vr.getAimVector(0));
        this.c1 = new VRDevicePose(this, dataholder.vr.getAimRotation(1, new Matrix4f()), dataholder.vr.getAimSource(1).subtract(vec3).add(vec31), dataholder.vr.getAimVector(1));
        this.h0 = new VRDevicePose(this, dataholder.vr.getHandRotation(0, new Matrix4f()), dataholder.vr.getAimSource(0).subtract(vec3).add(vec31), dataholder.vr.getHandVector(0));
        this.h1 = new VRDevicePose(this, dataholder.vr.getHandRotation(1, new Matrix4f()), dataholder.vr.getAimSource(1).subtract(vec3).add(vec31), dataholder.vr.getHandVector(1));

        if (dataholder.vrSettings.seated) {
            this.t0 = eye0;
            this.t1 = eye1;
        } else {
            Matrix4f matrix4f = this.getSmoothedRotation(0, 0.2F, new Matrix4f());
            Matrix4f matrix4f1 = this.getSmoothedRotation(1, 0.2F, new Matrix4f());
            this.t0 = new VRDevicePose(this, matrix4f, dataholder.vr.getAimSource(0).subtract(vec3).add(vec31), Utils.convertToVec3(matrix4f.transformProject(Utils.forward, new Vector3f())));
            this.t1 = new VRDevicePose(this, matrix4f1, dataholder.vr.getAimSource(1).subtract(vec3).add(vec31), Utils.convertToVec3(matrix4f1.transformProject(Utils.forward, new Vector3f())));
        }

        Matrix4f matrix4f2 = new Matrix4f().rotation(dataholder.cameraTracker.getRotation()).rotateLocalY(-rotation);
        float inverseWorldScale = 1.0F / worldScale;
        this.cam = new VRDevicePose(this, matrix4f2, dataholder.cameraTracker.getPosition().subtract(origin).yRot(-rotation).multiply(inverseWorldScale, inverseWorldScale, inverseWorldScale).subtract(vec3).add(vec31), Utils.convertToVec3(matrix4f2.transformProject(Utils.forward, new Vector3f())));

        if (dataholder.vr.mrMovingCamActive) {
            this.c2 = new VRDevicePose(this, dataholder.vr.getAimRotation(2, new Matrix4f()), dataholder.vr.getAimSource(2).subtract(vec3).add(vec31), dataholder.vr.getAimVector(2));
        } else {
            Matrix4f matrix4f3 = new Matrix4f().rotation(dataholder.vrSettings.vrFixedCamrotQuat);
            Vec3 vec32 = Utils.convertToVec3(dataholder.vrSettings.vrFixedCampos);
            Vec3 vec33 = Utils.convertToVec3(matrix4f3.transformProject(Utils.forward, new Vector3f()));
            this.c2 = new VRDevicePose(this, matrix4f3, vec32.subtract(vec3).add(vec31), vec33);
        }
    }

    private Matrix4f getSmoothedRotation(int c, float lenSec, Matrix4f dest) {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        Vec3 vec3 = dataholder.vr.controllerHistory[c].averagePosition(lenSec);
        Vec3 vec31 = dataholder.vr.controllerForwardHistory[c].averagePosition(lenSec);
        Vec3 vec32 = dataholder.vr.controllerUpHistory[c].averagePosition(lenSec);
        Vec3 vec33 = vec31.cross(vec32);
        return dest.set(
            (float) vec33.x, (float) vec33.y, (float) vec33.z, 0,
            (float) vec31.x, (float) vec31.y, (float) vec31.z, 0,
            (float) vec32.x, (float) vec32.y, (float) vec32.z, 0,
            0, 0, 0, 1
        );
    }

    public VRDevicePose getController(int c) {
        return c == 1 ? this.c1 : (c == 2 ? this.c2 : this.c0);
    }

    public VRDevicePose getHand(int c) {
        return c == 0 ? this.h0 : this.h1;
    }

    public float getBodyYaw() {
        if (ClientDataHolderVR.getInstance().vrSettings.seated) {
            return this.hmd.getYaw();
        } else {
            Vec3 vec3 = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float) Math.PI / 2F));
            Vec3 vec31 = this.hmd.getDirection();

            if (vec3.dot(vec31) < 0.0D) {
                vec3 = vec3.reverse();
            }

            vec3 = Utils.vecLerp(vec31, vec3, 0.7D);
            return (float) Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        }
    }

    public float getFacingYaw() {
        if (ClientDataHolderVR.getInstance().vrSettings.seated) {
            return this.hmd.getYaw();
        } else {
            Vec3 vec3 = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float) Math.PI / 2F));
            return ClientDataHolderVR.getInstance().vrSettings.reverseHands ? (float) Math.toDegrees(Math.atan2(vec3.x, -vec3.z)) : (float) Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        }
    }

    public Vec3 getHeadPivot() {
        Vec3 vec3 = this.hmd.getPosition();
        // scale pivot point with world scale, to prevent unwanted player movement
        Matrix4f matrix4f = this.hmd.getMatrix(new Matrix4f());
        Vector3f vector3 = matrix4f.transformProject(new Vector3f(0.0F, -0.1F * worldScale, 0.1F * worldScale), new Vector3f());
        return new Vec3((double) vector3.x() + vec3.x, (double) vector3.y() + vec3.y, (double) vector3.z() + vec3.z);
    }

    public Vec3 getHeadRear() {
        Vec3 vec3 = this.hmd.getPosition();
        Matrix4f matrix4f = this.hmd.getMatrix(new Matrix4f());
        Vector3f vector3 = matrix4f.transformProject(new Vector3f(0.0F, -0.2F, 0.2F), new Vector3f());
        return new Vec3((double) vector3.x() + vec3.x, (double) vector3.y() + vec3.y, (double) vector3.z() + vec3.z);
    }

    public VRDevicePose getEye(RenderPass pass) {
        switch (pass) {
            case CENTER:
                return this.hmd;

            case LEFT:
                return this.eye0;

            case RIGHT:
                return this.eye1;

            case THIRD:
                return this.c2;

            case SCOPER:
                return this.t0;

            case SCOPEL:
                return this.t1;

            case CAMERA:
                return this.cam;

            default:
                return this.hmd;
        }
    }

    public String toString() {
        return "data:\r\n \t\t origin: " + this.origin + "\r\n \t\t rotation: " + String.format("%.2f", this.rotation_radians) + "\r\n \t\t scale: " + String.format("%.2f", this.worldScale) + "\r\n \t\t hmd " + this.hmd + "\r\n \t\t c0 " + this.c0 + "\r\n \t\t c1 " + this.c1 + "\r\n \t\t c2 " + this.c2;
    }

    public class VRDevicePose {
        public final VRData data;
        public final Vec3 pos;
        public final Vec3 dir;
        public final Matrix4fc matrix;

        public VRDevicePose(VRData data, Matrix4f matrix, Vec3 pos, Vec3 dir) {
            this.data = data;
            this.matrix = matrix;
            this.pos = new Vec3(pos.x, pos.y, pos.z);
            this.dir = new Vec3(dir.x, dir.y, dir.z);
        }

        public Vec3 getPosition() {
            Vec3 vec3 = this.pos.scale(this.data.worldScale);
            vec3 = vec3.yRot(this.data.rotation_radians);
            return vec3.add(this.data.origin.x, this.data.origin.y, this.data.origin.z);
        }

        public Vec3 getDirection() {
            return (new Vec3(this.dir.x, this.dir.y, this.dir.z)).yRot(this.data.rotation_radians);
        }

        public Vec3 getCustomVector(Vec3 axis) {
            return Utils.convertToVec3(this.matrix.transformProject(new Vector3f((float) axis.x(), (float) axis.y(), (float) axis.z())).rotateY(this.data.rotation_radians));
        }

        public float getYaw() {
            Vec3 vec3 = this.getDirection();
            return (float) Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        }

        public float getPitch() {
            Vec3 vec3 = this.getDirection();
            return (float) Math.toDegrees(Math.asin(vec3.y / vec3.length()));
        }

        public float getRoll() {
            return (float) (-Math.toDegrees(Math.atan2(this.matrix.m01(), this.matrix.m11())));
        }

        public Matrix4f getMatrix(Matrix4f dest) {
            return this.matrix.rotateLocalY(this.data.rotation_radians, dest);
        }

        public String toString() {
            return "Device: pos:" + this.getPosition() + " dir: " + this.getDirection();
        }
    }
}
