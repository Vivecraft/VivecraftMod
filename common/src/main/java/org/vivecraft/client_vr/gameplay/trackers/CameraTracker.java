package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.RenderPass;

public class CameraTracker extends Tracker {
    public static final ModelResourceLocation cameraModel = new ModelResourceLocation("vivecraft", "camera", "");
    public static final ModelResourceLocation cameraDisplayModel = new ModelResourceLocation("vivecraft", "camera_display", "");
    private boolean visible = false;
    private Vec3 position = new Vec3(0.0D, 0.0D, 0.0D);
    private Quaternionf rotation = new Quaternionf();
    private int startController;
    private VRData.VRDevicePose startControllerPose;
    private Vec3 startPosition;
    private Quaternionf startRotation;
    private boolean quickMode;

    public CameraTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer player) {
        if (this.mc.gameMode == null) {
            return false;
        } else if (this.dh.vrSettings.seated) {
            return false;
        } else {
            return this.isVisible();
        }
    }

    public void doProcess(LocalPlayer player) {
        if (this.startControllerPose != null) {
            VRData.VRDevicePose vrdata$vrdevicepose = this.dh.vrPlayer.vrdata_world_render.getController(this.startController);
            Vec3 vec3 = this.startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f1 = vrdata$vrdevicepose.getMatrix();
            Matrix4f matrix4f2 = this.startControllerPose.getMatrix();
            Matrix4f b = matrix4f2.setTransposed(matrix4f2.transpose(new org.joml.Matrix4f()).invert());
            Matrix4f dest = new Matrix4f();
            Matrix4f matrix4f = dest.setTransposed(matrix4f1.transpose(new org.joml.Matrix4f()).mul0(b.transpose(new org.joml.Matrix4f())));
            Vector3f vector3 = new Vector3f((float) this.startPosition.x - (float) vec3.x, (float) this.startPosition.y - (float) vec3.y, (float) this.startPosition.z - (float) vec3.z);
            Vector3f vector31 = matrix4f.transpose(new org.joml.Matrix4f()).transformProject(vector3, new Vector3f());
            this.position = new Vec3(this.startPosition.x + (double) ((float) vec31.x) + (double) (vector31.x() - vector3.x()), this.startPosition.y + (double) ((float) vec31.y) + (double) (vector31.y() - vector3.y()), this.startPosition.z + (double) ((float) vec31.z) + (double) (vector31.z() - vector3.z()));
            this.startRotation.mul(new Quaternionf().setFromNormalized(matrix4f.transpose(new org.joml.Matrix4f())), this.rotation);
        }

        if (this.quickMode && !this.isMoving() && !this.dh.grabScreenShot) {
            this.visible = false;
        }

        if (this.dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition().distanceTo(this.position) > (double) (this.mc.options.getEffectiveRenderDistance() * 12)) {
            this.visible = false;
        }
    }

    public void reset(LocalPlayer player) {
        this.visible = false;
        this.quickMode = false;
        this.stopMoving();
    }

    public EntryPoint getEntryPoint() {
        return EntryPoint.SPECIAL_ITEMS;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void toggleVisibility() {
        this.visible = !this.visible;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation.set(rotation);
    }

    public boolean isMoving() {
        return this.startControllerPose != null;
    }

    public int getMovingController() {
        return this.startController;
    }

    public boolean isQuickMode() {
        return this.quickMode;
    }

    public void startMoving(int controller, boolean quickMode) {
        this.startController = controller;
        this.startControllerPose = this.dh.vrPlayer.vrdata_world_pre.getController(controller);
        this.startPosition = this.position;
        this.startRotation = new Quaternionf(this.rotation);
        this.quickMode = quickMode;
    }

    public void startMoving(int controller) {
        this.startMoving(controller, false);
    }

    public void stopMoving() {
        this.startControllerPose = null;
    }
}
