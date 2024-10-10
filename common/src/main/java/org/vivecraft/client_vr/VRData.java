package org.vivecraft.client_vr;

import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.utils.MathUtils;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Vector3;

public class VRData {
    // headset center
    public VRDevicePose hmd;
    // left eye
    public VRDevicePose eye0;
    // right eye
    public VRDevicePose eye1;
    // main controller aim
    public VRDevicePose c0;
    // offhand controller aim
    public VRDevicePose c1;
    // third person camera
    public VRDevicePose c2;

    // main controller hand
    public VRDevicePose h0;
    // offhand controller hand
    public VRDevicePose h1;

    // main controller telescope
    public VRDevicePose t0;
    // offhand controller telescope
    public VRDevicePose t1;

    // screenshot camera
    public VRDevicePose cam;

    // room origin, all VRDevicePose are relative to that
    public Vec3 origin;
    // room rotation rotated around the origin
    public float rotation_radians;
    // pose positions get scaled by that
    public float worldScale;

    public VRData(Vec3 origin, float walkMul, float worldScale, float rotation) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        MCVR mcVR = dataHolder.vr;

        this.origin = origin;
        this.worldScale = worldScale;
        this.rotation_radians = rotation;

        Vec3 hmd_raw = mcVR.getEyePosition(RenderPass.CENTER);
        Vec3 scaledPos = new Vec3(hmd_raw.x * walkMul, hmd_raw.y, hmd_raw.z * walkMul);

        Vec3 scaleOffset = new Vec3(scaledPos.x - hmd_raw.x, 0.0F, scaledPos.z - hmd_raw.z);

        // headset
        this.hmd = new VRDevicePose(this, mcVR.hmdRotation, scaledPos, mcVR.getHmdVector());

        this.eye0 = new VRDevicePose(this,
            mcVR.getEyeRotation(RenderPass.LEFT),
            mcVR.getEyePosition(RenderPass.LEFT).add(scaleOffset),
            mcVR.getHmdVector());

        this.eye1 = new VRDevicePose(this,
            mcVR.getEyeRotation(RenderPass.RIGHT),
            mcVR.getEyePosition(RenderPass.RIGHT).add(scaleOffset),
            mcVR.getHmdVector());

        // controllers
        this.c0 = new VRDevicePose(this,
            mcVR.getAimRotation(0),
            mcVR.getAimSource(0).add(scaleOffset),
            mcVR.getAimVector(0));
        this.c1 = new VRDevicePose(this,
            mcVR.getAimRotation(1),
            mcVR.getAimSource(1).add(scaleOffset),
            mcVR.getAimVector(1));

        this.h0 = new VRDevicePose(this,
            mcVR.getHandRotation(0),
            mcVR.getAimSource(0).add(scaleOffset),
            mcVR.getHandVector(0));
        this.h1 = new VRDevicePose(this,
            mcVR.getHandRotation(1),
            mcVR.getAimSource(1).add(scaleOffset),
            mcVR.getHandVector(1));

        // telescopes
        if (dataHolder.vrSettings.seated) {
            this.t0 = this.eye0;
            this.t1 = this.eye1;
        } else {
            Matrix4f scopeMain = this.getSmoothedRotation(0, 0.2F);
            Matrix4f scopeOff = this.getSmoothedRotation(1, 0.2F);
            this.t0 = new VRDevicePose(this,
                scopeMain,
                mcVR.getAimSource(0).add(scaleOffset),
                scopeMain.transform(Vector3.forward()).toVector3d());
            this.t1 = new VRDevicePose(this,
                scopeOff,
                mcVR.getAimSource(1).add(scaleOffset),
                scopeOff.transform(Vector3.forward()).toVector3d());
        }

        // screenshot camera
        Matrix4f camRot = Matrix4f.multiply(Matrix4f.rotationY(-rotation),
            new Matrix4f(dataHolder.cameraTracker.getRotation()).transposed());
        float inverseWorldScale = 1.0F / worldScale;
        this.cam = new VRData.VRDevicePose(this,
            camRot,
            dataHolder.cameraTracker.getPosition().subtract(origin).yRot(-rotation).scale(inverseWorldScale)
                .add(scaleOffset),
            camRot.transform(Vector3.forward()).toVector3d());

        // third person camera
        if (mcVR.mrMovingCamActive) {
            this.c2 = new VRDevicePose(this,
                mcVR.getAimRotation(2),
                mcVR.getAimSource(2).add(scaleOffset),
                mcVR.getAimVector(2));
        } else {
            VRSettings vrsettings = dataHolder.vrSettings;
            Matrix4f rot = (new Matrix4f(vrsettings.vrFixedCamrotQuat)).transposed();
            Vec3 pos = new Vec3(vrsettings.vrFixedCamposX, vrsettings.vrFixedCamposY, vrsettings.vrFixedCamposZ);
            Vec3 dir = rot.transform(Vector3.forward()).toVector3d();
            this.c2 = new VRDevicePose(this,
                rot,
                pos.add(scaleOffset),
                dir);
        }
    }

    /**
     * gets the smoothed rotation matrix of the specified controller
     * @param c controller to get
     * @param lenSec time period in seconds
     * @return smoothed rotation
     */
    private Matrix4f getSmoothedRotation(int c, float lenSec) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        Vec3 forward = dataHolder.vr.controllerForwardHistory[c].averagePosition(lenSec);
        Vec3 up = dataHolder.vr.controllerUpHistory[c].averagePosition(lenSec);
        Vec3 right = forward.cross(up);
        return new Matrix4f(
            (float) right.x, (float) forward.x, (float) up.x,
            (float) right.y, (float) forward.y, (float) up.y,
            (float) right.z, (float) forward.z, (float) up.z);
    }

    /**
     * @param c controller index
     * @return the device pose for the specified controller
     */
    public VRDevicePose getController(int c) {
        return c == 1 ? this.c1 : (c == 2 ? this.c2 : this.c0);
    }

    /**
     * @param c controller index
     * @return the hand device pose for the specified controller
     */
    public VRDevicePose getHand(int c) {
        return c == 0 ? this.h0 : this.h1;
    }

    /**
     * @return the yaw direction the player body is facing
     */
    public float getBodyYaw() {
        if (ClientDataHolderVR.getInstance().vrSettings.seated) {
            // body is equal to the headset
            return this.hmd.getYaw();
        } else {
            // body is average of arms and headset direction
            Vec3 arms = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float) Math.PI / 2F));
            Vec3 head = this.hmd.getDirection();

            if (arms.dot(head) < 0.0D) {
                arms = arms.reverse();
            }

            arms = MathUtils.vecLerp(head, arms, 0.7D);
            return (float) Math.toDegrees(Math.atan2(-arms.x, arms.z));
        }
    }

    /**
     * @return the yaw direction the players hands is facing
     */
    public float getFacingYaw() {
        if (ClientDataHolderVR.getInstance().vrSettings.seated) {
            return this.hmd.getYaw();
        } else {
            Vec3 arms = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float) Math.PI / 2F));
            // with reverseHands c0 is the left hand, not right
            if (ClientDataHolderVR.getInstance().vrSettings.reverseHands) {
                return (float) Math.toDegrees(Math.atan2(arms.x, -arms.z));
            } else {
                return (float) Math.toDegrees(Math.atan2(-arms.x, arms.z));
            }
        }
    }

    /**
     * @return estimated pivot point, that the players head rotates around
     */
    public Vec3 getHeadPivot() {
        Vec3 eye = this.hmd.getPosition();
        // scale pivot point with world scale, to prevent unwanted player movement
        Vector3 headPivotOffset = this.hmd.getMatrix()
            .transform(new Vector3(0.0F, -0.1F * this.worldScale, 0.1F * this.worldScale));
        return new Vec3(headPivotOffset.getX() + eye.x, headPivotOffset.getY() + eye.y, headPivotOffset.getZ() + eye.z);
    }

    /**
     * @return estimated point that is behind the players back
     */
    public Vec3 getHeadRear() {
        Vec3 eye = this.hmd.getPosition();
        Vector3 headBackOffset = this.hmd.getMatrix().transform(new Vector3(0.0F, -0.2F, 0.2F));
        return new Vec3(headBackOffset.getX() + eye.x, headBackOffset.getY() + eye.y, headBackOffset.getZ() + eye.z);
    }

    /**
     * @param pass RenderPass to get the VRDevicePose for
     * @return VRDevicePose corresponding to that RenderPass, or HMD if no matching pose is available
     */
    public VRDevicePose getEye(RenderPass pass) {
        return switch (pass) {
            case CENTER -> this.hmd;
            case LEFT -> this.eye0;
            case RIGHT -> this.eye1;
            case THIRD -> this.c2;
            case SCOPER -> this.t0;
            case SCOPEL -> this.t1;
            case CAMERA -> this.cam;
            default -> this.hmd;
        };
    }

    @Override
    public String toString() {
        return """
            VRData:
                origin: %s
                rotation: %.2f
                scale: %.2f
                hmd: %s
                c0: %s
                c1: %s
                c2: %s
            """
            .formatted(
                this.origin,
                this.rotation_radians,
                this.worldScale,
                this.hmd,
                this.c0,
                this.c1,
                this.c2
                );
    }

    public class VRDevicePose {
        // link to the parent, holds the rotation, scale and origin
        final VRData data;
        // in room position
        final Vec3 pos;
        // in room direction
        final Vec3 dir;
        // in room orientation
        final Matrix4f matrix;

        public VRDevicePose(VRData data, Matrix4f matrix, Vec3 pos, Vec3 dir) {
            this.data = data;
            // poor mans copy.
            this.matrix = matrix.transposed().transposed();
            this.pos = new Vec3(pos.x, pos.y, pos.z);
            this.dir = new Vec3(dir.x, dir.y, dir.z);
        }

        /**
         * @return position of this device in world space
         */
        public Vec3 getPosition() {
           return this.pos.scale(VRData.this.worldScale)
               .yRot(this.data.rotation_radians)
               .add(this.data.origin);
        }

        /**
         * @return direction of this device in world space
         */
        public Vec3 getDirection() {
            return this.dir.yRot(this.data.rotation_radians);
        }

        /**
         * transforms the device local vector {@code axis} to world space
         * @param axis local vector to transform
         * @return {@code axis} transformed into world space
         */
        public Vec3 getCustomVector(Vec3 axis) {
            return this.matrix.transform(new Vector3((float) axis.x, (float) axis.y, (float) axis.z))
                .toVector3d()
                .yRot(this.data.rotation_radians);
        }

        /**
         * @return yaw of the device in world space, in degrees
         */
        public float getYaw() {
            return (float) Math.toDegrees(this.getYawRad());
        }

        /**
         * @return yaw of the device in world space, in radians
         */
        public float getYawRad() {
            Vec3 dir = this.getDirection();
            return (float) Math.atan2(-dir.x, dir.z);
        }

        /**
         * @return pitch of the device in world space, in degrees
         */
        public float getPitch() {
            return (float) Math.toDegrees(this.getPitchRad());
        }

        /**
         * @return pitch of the device in world space, in radians
         */
        public float getPitchRad() {
            Vec3 dir = this.getDirection();
            return (float) Math.asin(dir.y / dir.length());
        }

        /**
         * @return roll of the device in world space, in degrees
         */
        public float getRoll() {
            return (float) Math.toDegrees(this.getRollRad());
        }

        /**
         * @return pitch of the device in world space, in radians
         */
        public float getRollRad() {
            return (float) -Math.atan2(this.matrix.M[1][0], this.matrix.M[1][1]);
        }

        /**
         * @return pose matrix of the device in world space
         */
        public Matrix4f getMatrix() {
            Matrix4f rot = Matrix4f.rotationY(VRData.this.rotation_radians);
            return Matrix4f.multiply(rot, this.matrix);
        }

        @Override
        public String toString() {
            return "Device: pos:" + this.getPosition() + ", dir: " + this.getDirection();
        }
    }
}
