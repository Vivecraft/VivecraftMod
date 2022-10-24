package org.vivecraft.api;

import org.vivecraft.ClientDataHolder;
import org.vivecraft.render.RenderPass;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Matrix4f;
import org.vivecraft.utils.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class VRData
{
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

    public VRData(Vec3 origin, float walkMul, float worldScale, float rotation)
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolder dataholder = ClientDataHolder.getInstance();
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotation_radians = rotation;
        Vec3 vec3 = dataholder.vr.getCenterEyePosition();
        Vec3 vec31 = new Vec3(vec3.x * (double)walkMul, vec3.y, vec3.z * (double)walkMul);
        this.hmd = new VRDevicePose(this, dataholder.vr.hmdRotation, vec31, dataholder.vr.getHmdVector());
        this.eye0 = new VRDevicePose(this, dataholder.vr.getEyeRotation(RenderPass.LEFT), dataholder.vr.getEyePosition(RenderPass.LEFT).subtract(vec3).add(vec31), dataholder.vr.getHmdVector());
        this.eye1 = new VRDevicePose(this, dataholder.vr.getEyeRotation(RenderPass.RIGHT), dataholder.vr.getEyePosition(RenderPass.RIGHT).subtract(vec3).add(vec31), dataholder.vr.getHmdVector());
        this.c0 = new VRDevicePose(this, dataholder.vr.getAimRotation(0), dataholder.vr.getAimSource(0).subtract(vec3).add(vec31), dataholder.vr.getAimVector(0));
        this.c1 = new VRDevicePose(this, dataholder.vr.getAimRotation(1), dataholder.vr.getAimSource(1).subtract(vec3).add(vec31), dataholder.vr.getAimVector(1));
        this.h0 = new VRDevicePose(this, dataholder.vr.getHandRotation(0), dataholder.vr.getAimSource(0).subtract(vec3).add(vec31), dataholder.vr.getHandVector(0));
        this.h1 = new VRDevicePose(this, dataholder.vr.getHandRotation(1), dataholder.vr.getAimSource(1).subtract(vec3).add(vec31), dataholder.vr.getHandVector(1));

        if(dataholder.vrSettings.seated) {
        	this.t0 = eye0;
        	this.t1 = eye1;
        } else {
        	Matrix4f matrix4f = this.getSmoothedRotation(0, 0.2F);
        	Matrix4f matrix4f1 = this.getSmoothedRotation(1, 0.2F);
        	this.t0 = new VRDevicePose(this, matrix4f, dataholder.vr.getAimSource(0).subtract(vec3).add(vec31), matrix4f.transform(Vector3.forward()).toVector3d());
        	this.t1 = new VRDevicePose(this, matrix4f1, dataholder.vr.getAimSource(1).subtract(vec3).add(vec31), matrix4f1.transform(Vector3.forward()).toVector3d());
        }
        
        Matrix4f matrix4f2 = Matrix4f.multiply(Matrix4f.rotationY(-rotation), (new Matrix4f(ClientDataHolder.getInstance().cameraTracker.getRotation())).transposed());
        float inverseWorldScale = 1.0F / worldScale;
        this.cam = new VRData.VRDevicePose(this, matrix4f2, ClientDataHolder.getInstance().cameraTracker.getPosition().subtract(origin).yRot(-rotation).multiply(inverseWorldScale,inverseWorldScale,inverseWorldScale).subtract(vec3).add(vec31), matrix4f2.transform(Vector3.forward()).toVector3d());

        if (dataholder.vr.mrMovingCamActive)
        {
            this.c2 = new VRDevicePose(this, dataholder.vr.getAimRotation(2), dataholder.vr.getAimSource(2).subtract(vec3).add(vec31), dataholder.vr.getAimVector(2));
        }
        else
        {
            VRSettings vrsettings = ClientDataHolder.getInstance().vrSettings;
            Matrix4f matrix4f3 = (new Matrix4f(vrsettings.vrFixedCamrotQuat)).transposed();
            Vec3 vec32 = new Vec3((double)vrsettings.vrFixedCamposX, (double)vrsettings.vrFixedCamposY, (double)vrsettings.vrFixedCamposZ);
            Vec3 vec33 = matrix4f3.transform(Vector3.forward()).toVector3d();
            this.c2 = new VRDevicePose(this, matrix4f3, vec32.subtract(vec3).add(vec31), vec33);
        }
    }

    private Matrix4f getSmoothedRotation(int c, float lenSec)
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolder dataholder = ClientDataHolder.getInstance();

        Vec3 vec3 = dataholder.vr.controllerHistory[c].averagePosition((double)lenSec);
        Vec3 vec31 = dataholder.vr.controllerForwardHistory[c].averagePosition((double)lenSec);
        Vec3 vec32 = dataholder.vr.controllerUpHistory[c].averagePosition((double)lenSec);
        Vec3 vec33 = vec31.cross(vec32);
        return new Matrix4f((float)vec33.x, (float)vec31.x, (float)vec32.x, (float)vec33.y, (float)vec31.y, (float)vec32.y, (float)vec33.z, (float)vec31.z, (float)vec32.z);
    }

    public VRDevicePose getController(int c)
    {
        return c == 1 ? this.c1 : (c == 2 ? this.c2 : this.c0);
    }

    public VRDevicePose getHand(int c)
    {
        return c == 0 ? this.h0 : this.h1;
    }

    public float getBodyYaw()
    {
        if (ClientDataHolder.getInstance().vrSettings.seated)
        {
            return this.hmd.getYaw();
        }
        else
        {
            Vec3 vec3 = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float)Math.PI / 2F));
            Vec3 vec31 = this.hmd.getDirection();

            if (vec3.dot(vec31) < 0.0D)
            {
                vec3 = vec3.reverse();
            }

            vec3 = Utils.vecLerp(vec31, vec3, 0.7D);
            return (float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        }
    }

    public float getFacingYaw()
    {
        if (ClientDataHolder.getInstance().vrSettings.seated)
        {
            return this.hmd.getYaw();
        }
        else
        {
            Vec3 vec3 = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float)Math.PI / 2F));
            return ClientDataHolder.getInstance().vrSettings.reverseHands ? (float)Math.toDegrees(Math.atan2(vec3.x, -vec3.z)) : (float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        }
    }

    public Vec3 getHeadPivot()
    {
        Vec3 vec3 = this.hmd.getPosition();
        // scale pivot point with world scale, to prevent unwanted player movement
        Vector3 vector3 = this.hmd.getMatrix().transform(new Vector3(0.0F, -0.1F * worldScale, 0.1F * worldScale));
        return new Vec3((double)vector3.getX() + vec3.x, (double)vector3.getY() + vec3.y, (double)vector3.getZ() + vec3.z);
    }

    public Vec3 getHeadRear()
    {
        Vec3 vec3 = this.hmd.getPosition();
        Vector3 vector3 = this.hmd.getMatrix().transform(new Vector3(0.0F, -0.2F, 0.2F));
        return new Vec3((double)vector3.getX() + vec3.x, (double)vector3.getY() + vec3.y, (double)vector3.getZ() + vec3.z);
    }

    public VRDevicePose getEye(RenderPass pass)
    {
        switch (pass)
        {
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

    public String toString()
    {
        return "data:\r\n \t\t origin: " + this.origin + "\r\n \t\t rotation: " + String.format("%.2f", this.rotation_radians) + "\r\n \t\t scale: " + String.format("%.2f", this.worldScale) + "\r\n \t\t hmd " + this.hmd + "\r\n \t\t c0 " + this.c0 + "\r\n \t\t c1 " + this.c1 + "\r\n \t\t c2 " + this.c2;
    }

    protected Vec3 vecMult(Vec3 in, float factor)
    {
        return new Vec3(in.x * (double)factor, in.y * (double)factor, in.z * (double)factor);
    }

    public class VRDevicePose
    {
        final VRData data;
        final Vec3 pos;
        final Vec3 dir;
        final Matrix4f matrix;

        public VRDevicePose(VRData data, Matrix4f matrix, Vec3 pos, Vec3 dir)
        {
            this.data = data;
            this.matrix = matrix.transposed().transposed();
            this.pos = new Vec3(pos.x, pos.y, pos.z);
            this.dir = new Vec3(dir.x, dir.y, dir.z);
        }

        public Vec3 getPosition()
        {
            Vec3 vec3 = this.pos.scale((double)VRData.this.worldScale);
            vec3 = vec3.yRot(this.data.rotation_radians);
            return vec3.add(this.data.origin.x, this.data.origin.y, this.data.origin.z);
        }

        public Vec3 getDirection()
        {
            return (new Vec3(this.dir.x, this.dir.y, this.dir.z)).yRot(this.data.rotation_radians);
        }

        public Vec3 getCustomVector(Vec3 axis)
        {
            Vector3 vector3 = this.matrix.transform(new Vector3((float)axis.x, (float)axis.y, (float)axis.z));
            return vector3.toVector3d().yRot(this.data.rotation_radians);
        }

        public float getYaw()
        {
            Vec3 vec3 = this.getDirection();
            return (float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        }

        public float getPitch()
        {
            Vec3 vec3 = this.getDirection();
            return (float)Math.toDegrees(Math.asin(vec3.y / vec3.length()));
        }

        public float getRoll()
        {
            return (float)(-Math.toDegrees(Math.atan2((double)this.matrix.M[1][0], (double)this.matrix.M[1][1])));
        }

        public Matrix4f getMatrix()
        {
            Matrix4f matrix4f = Matrix4f.rotationY(VRData.this.rotation_radians);
            return Matrix4f.multiply(matrix4f, this.matrix);
        }

        public String toString()
        {
            return "Device: pos:" + this.getPosition() + " dir: " + this.getDirection();
        }
    }
}
