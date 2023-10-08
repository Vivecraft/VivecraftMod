package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.resources.model.ModelResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.render.RenderPass;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class CameraTracker extends Tracker {
    public static final ModelResourceLocation cameraModel = new ModelResourceLocation("vivecraft", "camera", "");
    public static final ModelResourceLocation cameraDisplayModel = new ModelResourceLocation("vivecraft", "camera_display", "");
    private boolean visible = false;
    private final Vector3f position = new Vector3f();
    private Quaternionf rotation = new Quaternionf();
    private int startController;
    private VRDevicePose startControllerPose;
    private final Vector3f startPosition = new Vector3f();
    private Quaternionf startRotation;
    private boolean quickMode;

    @Override
    public boolean isActive() {
        if (mc.gameMode == null) {
            return false;
        } else if (dh.vrSettings.seated) {
            return false;
        } else {
            return this.isVisible();
        }
    }

    @Override
    public void doProcess() {
        if (this.startControllerPose != null) {
            VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_world_render.getController(this.startController);
            Vector3f vec3 = this.startControllerPose.getPosition(new Vector3f());
            Vector3f vec31 = vrdata$vrdevicepose.getPosition(new Vector3f()).sub(vec3);
            Matrix4f matrix4f = vrdata$vrdevicepose.getMatrix().mul0(this.startControllerPose.getMatrix().invertAffine(), new Matrix4f());
            Vector3f vector3 = new Vector3f(this.startPosition.x - vec3.x, this.startPosition.y - vec3.y, this.startPosition.z - vec3.z);
            Vector3f vector31 = matrix4f.transformProject(vector3, new Vector3f());
            this.position.set(this.startPosition.x + vec31.x + vector31.x - vector3.x, this.startPosition.y + vec31.y + vector31.y - vector3.y, this.startPosition.z + vec31.z + vector31.z - vector3.z);
            this.rotation.setFromNormalized(matrix4f);
            this.startRotation.mul(this.rotation, this.rotation);
        }

        if (this.quickMode && !this.isMoving() && !dh.grabScreenShot) {
            this.visible = false;
        }

        if (dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER).getPosition(new Vector3f()).distance(this.position) > (double) (mc.options.getEffectiveRenderDistance() * 12)) {
            this.visible = false;
        }
    }

    @Override
    public void reset() {
        this.visible = false;
        this.quickMode = false;
        this.stopMoving();
    }

    @Override
    public EntryPoint getEntryPoint() {
        return EntryPoint.SPECIAL_ITEMS;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void toggleVisibility() {
        this.visible = !this.visible;
    }

    public Vector3fc getPosition() {
        return this.position;
    }

    public void setPosition(Vector3fc position) {
        this.position.set(position);
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
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
        this.startControllerPose = dh.vrPlayer.vrdata_world_pre.getController(controller);
        this.startPosition.set(this.position);
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
