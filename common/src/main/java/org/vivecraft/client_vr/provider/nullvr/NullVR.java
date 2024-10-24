package org.vivecraft.client_vr.provider.nullvr;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.math.Matrix4f;

import java.util.List;

/**
 * MCVR implementation that does not interact with any runtime.
 */
public class NullVR extends MCVR {
    protected static NullVR ome;

    private static final float ipd = 0.1F;

    private boolean vrActive = true;

    public NullVR(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh, VivecraftVRMod.INSTANCE);
        ome = this;
        this.hapticScheduler = new NullVRHapticScheduler();
    }

    public static NullVR get() {
        return ome;
    }


    @Override
    public void destroy() {
        this.initialized = false;
    }

    @Override
    public String getName() {
        return "nullDriver";
    }

    @Override
    public Vector2f getPlayAreaSize() {
        return new Vector2f(2);
    }

    @Override
    public boolean init() {
        if (!this.initialized) {
            this.mc = Minecraft.getInstance();

            // only supports seated mode
            VRSettings.logger.info("Vivecraft: NullDriver. Forcing seated mode.");
            this.dh.vrSettings.seated = true;

            this.headIsTracking = true;
            this.hmdPose.SetIdentity();
            this.hmdPose.M[1][3] = 1.62F;

            // eye offset, 10cm total distance
            this.hmdPoseLeftEye.M[0][3] = -ipd * 0.5F;
            this.hmdPoseRightEye.M[0][3] = ipd * 0.5F;

            this.populateInputActions();

            this.initialized = true;
            this.initSuccess = true;
        }

        return this.initialized;
    }

    @Override
    public void poll(long frameIndex) {
        if (this.initialized) {

            this.mc.getProfiler().push("updatePose");

            // don't permanently change the sensitivity
            float xSens = this.dh.vrSettings.xSensitivity;
            float xKey = this.dh.vrSettings.keyholeX;

            this.dh.vrSettings.xSensitivity = this.dh.vrSettings.ySensitivity * 1.636F *
                ((float) this.mc.getWindow().getScreenWidth() / (float) this.mc.getWindow().getScreenHeight());
            this.dh.vrSettings.keyholeX = 1;

            this.updateAim();

            this.controllerPose[0].M[0][3] = this.dh.vrSettings.reverseHands ? -0.3F : 0.3F;
            this.controllerPose[0].M[1][3] = 1.2F;
            this.controllerPose[0].M[2][3] = -0.5F;

            this.controllerPose[1].M[0][3] =  this.dh.vrSettings.reverseHands ? 0.3F : -0.3F;
            this.controllerPose[1].M[1][3] = 1.2F;
            this.controllerPose[1].M[2][3] = -0.5F;

            this.dh.vrSettings.xSensitivity = xSens;
            this.dh.vrSettings.keyholeX = xKey;


            // point head in cursor direction
            this.hmdRotation.M[0][0] = this.handRotation[0].M[0][0];
            this.hmdRotation.M[0][1] = this.handRotation[0].M[0][1];
            this.hmdRotation.M[0][2] = this.handRotation[0].M[0][2];
            this.hmdRotation.M[1][0] = this.handRotation[0].M[1][0];
            this.hmdRotation.M[1][1] = this.handRotation[0].M[1][1];
            this.hmdRotation.M[1][2] = this.handRotation[0].M[1][2];
            this.hmdRotation.M[2][0] = this.handRotation[0].M[2][0];
            this.hmdRotation.M[2][1] = this.handRotation[0].M[2][1];
            this.hmdRotation.M[2][2] = this.handRotation[0].M[2][2];

            if (GuiHandler.guiRotation_room != null) {
                // look at screen, so that it's centered
                this.hmdRotation.M[0][0] = GuiHandler.guiRotation_room.M[0][0];
                this.hmdRotation.M[0][1] = GuiHandler.guiRotation_room.M[0][1];
                this.hmdRotation.M[0][2] = GuiHandler.guiRotation_room.M[0][2];
                this.hmdRotation.M[1][0] = GuiHandler.guiRotation_room.M[1][0];
                this.hmdRotation.M[1][1] = GuiHandler.guiRotation_room.M[1][1];
                this.hmdRotation.M[1][2] = GuiHandler.guiRotation_room.M[1][2];
                this.hmdRotation.M[2][0] = GuiHandler.guiRotation_room.M[2][0];
                this.hmdRotation.M[2][1] = GuiHandler.guiRotation_room.M[2][1];
                this.hmdRotation.M[2][2] = GuiHandler.guiRotation_room.M[2][2];
            }
            this.mc.getProfiler().popPush("hmdSampling");
            this.hmdSampling();

            this.mc.getProfiler().pop();
        }
    }

    @Override
    public void processInputs() {}

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping keyMapping) {
        return null;
    }

    @Override
    public Matrix4f getControllerComponentTransform(int controllerIndex, String componentName) {
        return new Matrix4f();
    }

    @Override
    public String getOriginName(long origin) {
        return "NullDriver";
    }

    @Override
    public boolean hasCameraTracker() {
        return false;
    }

    @Override
    public List<Long> getOrigins(VRInputAction action) {
        return null;
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new NullVRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        return this.vrActive;
    }

    @Override
    public boolean capFPS() {
        return true;
    }

    @Override
    public float getIPD() {
        return ipd;
    }

    @Override
    public String getRuntimeName() {
        return "Null";
    }

    @Override
    public boolean handleKeyboardInputs(int key, int scanCode, int action, int modifiers) {
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_F6 && MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            this.vrActive = !this.vrActive;
            return true;
        }
        return false;
    }
}
