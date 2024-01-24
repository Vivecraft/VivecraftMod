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
import org.vivecraft.common.utils.Utils;

public class CameraTracker extends Tracker {
    public static final ModelResourceLocation cameraModel = new ModelResourceLocation("vivecraft", "camera", "");
    public static final ModelResourceLocation cameraDisplayModel = new ModelResourceLocation("vivecraft", "camera_display", "");
    private boolean visible = false;
    private final Vector3f position = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private int startController;
    private VRData.VRDevicePose startControllerPose;
    private final Vector3f startPosition = new Vector3f();
    private final Quaternionf startRotation = new Quaternionf();
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
            VRData.VRDevicePose vrDevPose = this.dh.vrPlayer.vrdata_world_render.getController(this.startController);
            Vector3f vrOldPosition = Utils.convertToVector3f(this.startControllerPose.getPosition(), new Vector3f());
            Vector3f vrDevPosition = Utils.convertToVector3f(vrDevPose.getPosition(), new Vector3f()).sub(vrOldPosition);
            Matrix4f matrix4f = vrDevPose.getMatrix(new Matrix4f()).mul(this.startControllerPose.getMatrix(new Matrix4f()).invert());
            matrix4f.transformProject(this.startPosition.sub(vrOldPosition, vrOldPosition), this.position).sub(vrOldPosition).add(vrDevPosition).add(this.startPosition);
            this.rotation.setFromUnnormalized(matrix4f).mul(this.startRotation);
        }

        if (this.quickMode && !this.isMoving() && !this.dh.grabScreenShot) {
            this.visible = false;
        }

        if (Utils.convertToVector3f(this.dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition(), new Vector3f()).distance(this.position) > (double) (this.mc.options.getEffectiveRenderDistance() * 12)) {
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
        return Utils.convertToVec3(this.position);
    }

    public void setPosition(Vec3 position) {
        Utils.convertToVector3f(position, this.position);
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
        this.startPosition.set(this.position);
        this.startRotation.set(this.rotation);
        this.quickMode = quickMode;
    }

    public void startMoving(int controller) {
        this.startMoving(controller, false);
    }

    public void stopMoving() {
        this.startControllerPose = null;
    }
}
