package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.render.RenderPass;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class CameraTracker extends Tracker
{
    public static final ModelResourceLocation cameraModel = new ModelResourceLocation("vivecraft", "camera", "");
    public static final ModelResourceLocation cameraDisplayModel = new ModelResourceLocation("vivecraft", "camera_display", "");
    private boolean visible = false;
    private Vec3 position = new Vec3(0.0D, 0.0D, 0.0D);
    private Quaternionf rotation = new Quaternionf();
    private int startController;
    private VRDevicePose startControllerPose;
    private Vec3 startPosition;
    private Quaternionf startRotation;
    private boolean quickMode;

    @Override
    public boolean isActive()
    {
        if (mc.gameMode == null)
        {
            return false;
        }
        else if (dh.vrSettings.seated)
        {
            return false;
        }
        else
        {
            return this.isVisible();
        }
    }

    @Override
    public void doProcess()
    {
        if (this.startControllerPose != null)
        {
            VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_world_render.getController(this.startController);
            Vec3 vec3 = this.startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = vrdata$vrdevicepose.getMatrix().mul0(this.startControllerPose.getMatrix().invertAffine(), new Matrix4f());
            Vector3f vector3 = new Vector3f((float)this.startPosition.x - (float)vec3.x, (float)this.startPosition.y - (float)vec3.y, (float)this.startPosition.z - (float)vec3.z);
            Vector3f vector31 = vector3.mulProject(matrix4f, new Vector3f());
            this.position = new Vec3(this.startPosition.x + (double)((float)vec31.x) + (double)(vector31.x - vector3.x), this.startPosition.y + (double)((float)vec31.y) + (double)(vector31.y - vector3.y), this.startPosition.z + (double)((float)vec31.z) + (double)(vector31.z - vector3.z));
            this.rotation.setFromNormalized(matrix4f);
            this.startRotation.mul(this.rotation, this.rotation);
        }

        if (this.quickMode && !this.isMoving() && !dh.grabScreenShot)
        {
            this.visible = false;
        }

        if (dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().distanceTo(this.position) > (double)(mc.options.getEffectiveRenderDistance() * 12))
        {
            this.visible = false;
        }
    }

    @Override
    public void reset()
    {
        this.visible = false;
        this.quickMode = false;
        this.stopMoving();
    }

    @Override
    public EntryPoint getEntryPoint()
    {
        return EntryPoint.SPECIAL_ITEMS;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void toggleVisibility()
    {
        this.visible = !this.visible;
    }

    public Vec3 getPosition()
    {
        return this.position;
    }

    public void setPosition(Vec3 position)
    {
        this.position = position;
    }

    public Quaternionf getRotation()
    {
        return this.rotation;
    }

    public void setRotation(Quaternionf rotation)
    {
        this.rotation = rotation;
    }

    public boolean isMoving()
    {
        return this.startControllerPose != null;
    }

    public int getMovingController()
    {
        return this.startController;
    }

    public boolean isQuickMode()
    {
        return this.quickMode;
    }

    public void startMoving(int controller, boolean quickMode)
    {
        this.startController = controller;
        this.startControllerPose = dh.vrPlayer.vrdata_world_pre.getController(controller);
        this.startPosition = this.position;
        this.startRotation = new Quaternionf(this.rotation);
        this.quickMode = quickMode;
    }

    public void startMoving(int controller)
    {
        this.startMoving(controller, false);
    }

    public void stopMoving()
    {
        this.startControllerPose = null;
    }
}
