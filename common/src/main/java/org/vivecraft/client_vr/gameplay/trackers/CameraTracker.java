package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.phys.Vec3;

public class CameraTracker extends Tracker
{
    public static final ModelResourceLocation cameraModel = new ModelResourceLocation("vivecraft", "camera", "");
    public static final ModelResourceLocation cameraDisplayModel = new ModelResourceLocation("vivecraft", "camera_display", "");
    private boolean visible = false;
    private Vec3 position = new Vec3(0.0D, 0.0D, 0.0D);
    private Quaternion rotation = new Quaternion();
    private int startController;
    private VRData.VRDevicePose startControllerPose;
    private Vec3 startPosition;
    private Quaternion startRotation;
    private boolean quickMode;

    public CameraTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer player)
    {
        if (this.mc.gameMode == null)
        {
            return false;
        }
        else if (this.dh.vrSettings.seated)
        {
            return false;
        }
        else
        {
            return this.isVisible();
        }
    }

    public void doProcess(LocalPlayer player)
    {
        if (this.startControllerPose != null)
        {
            VRData.VRDevicePose vrdata$vrdevicepose = this.dh.vrPlayer.vrdata_world_render.getController(this.startController);
            Vec3 vec3 = this.startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = Matrix4f.multiply(vrdata$vrdevicepose.getMatrix(), this.startControllerPose.getMatrix().inverted());
            Vector3 vector3 = new Vector3((float)this.startPosition.x - (float)vec3.x, (float)this.startPosition.y - (float)vec3.y, (float)this.startPosition.z - (float)vec3.z);
            Vector3 vector31 = matrix4f.transform(vector3);
            this.position = new Vec3(this.startPosition.x + (double)((float)vec31.x) + (double)(vector31.getX() - vector3.getX()), this.startPosition.y + (double)((float)vec31.y) + (double)(vector31.getY() - vector3.getY()), this.startPosition.z + (double)((float)vec31.z) + (double)(vector31.getZ() - vector3.getZ()));
            this.rotation = this.startRotation.multiply(new Quaternion(Utils.convertOVRMatrix(matrix4f)));
        }

        if (this.quickMode && !this.isMoving() && !this.dh.grabScreenShot)
        {
            this.visible = false;
        }

        if (this.dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().distanceTo(this.position) > (double)(this.mc.options.getEffectiveRenderDistance() * 12))
        {
            this.visible = false;
        }
    }

    public void reset(LocalPlayer player)
    {
        this.visible = false;
        this.quickMode = false;
        this.stopMoving();
    }

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

    public Quaternion getRotation()
    {
        return this.rotation;
    }

    public void setRotation(Quaternion rotation)
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
        this.startControllerPose = this.dh.vrPlayer.vrdata_world_pre.getController(controller);
        this.startPosition = this.position;
        this.startRotation = this.rotation.copy();
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
