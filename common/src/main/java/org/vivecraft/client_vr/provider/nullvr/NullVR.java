package org.vivecraft.client_vr.provider.nullvr;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.common.utils.math.Matrix4f;

import java.util.List;

public class NullVR extends MCVR {
    protected static NullVR ome;

    private static final float ipd = 0.1F;

    private boolean vrActive = true;
    private boolean vrActiveChangedLastFrame = false;

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
    public String getID() {
        return "nullDriver";
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
            System.out.println("NullDriver. Forcing seated mode.");
            this.dh.vrSettings.seated = true;

            this.headIsTracking = false;
            Utils.Matrix4fSetIdentity(this.hmdPose);
            this.hmdPose.M[1][3] = 1.62F;

            // eye offset, 10cm total distance
            this.hmdPoseLeftEye.M[0][3] = -ipd * 0.5F;
            this.hmdPoseRightEye.M[0][3] = ipd * 0.5F;

            this.initialized = true;
            this.initSuccess = true;
        }

        return true;
    }

    @Override
    public void poll(long frameIndex) {
        if (this.initialized) {

            this.mc.getProfiler().push("updatePose");

            // don't permanently change the sensitivity
            float xSens = this.dh.vrSettings.xSensitivity;
            float xKey = this.dh.vrSettings.keyholeX;

            this.dh.vrSettings.xSensitivity = this.dh.vrSettings.ySensitivity * 1.636F * ((float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getScreenHeight());
            this.dh.vrSettings.keyholeX = 1;

            this.updateAim();

            this.controllerPose[0].M[0][3] = 0.3F;
            this.controllerPose[0].M[1][3] = 1.2F;
            this.controllerPose[0].M[2][3] = -0.5F;

            this.controllerPose[1].M[0][3] = -0.3F;
            this.controllerPose[1].M[1][3] = 1.2F;
            this.controllerPose[1].M[2][3] = -0.5F;

            this.dh.vrSettings.xSensitivity = xSens;
            this.dh.vrSettings.keyholeX = xKey;


            // point head in cursor direction
            hmdRotation.M[0][0] = handRotation[0].M[0][0];
            hmdRotation.M[0][1] = handRotation[0].M[0][1];
            hmdRotation.M[0][2] = handRotation[0].M[0][2];
            hmdRotation.M[1][0] = handRotation[0].M[1][0];
            hmdRotation.M[1][1] = handRotation[0].M[1][1];
            hmdRotation.M[1][2] = handRotation[0].M[1][2];
            hmdRotation.M[2][0] = handRotation[0].M[2][0];
            hmdRotation.M[2][1] = handRotation[0].M[2][1];
            hmdRotation.M[2][2] = handRotation[0].M[2][2];

            if (GuiHandler.guiRotation_room != null) {
                // look at screen, so that it's centered
                hmdRotation.M[0][0] = GuiHandler.guiRotation_room.M[0][0];
                hmdRotation.M[0][1] = GuiHandler.guiRotation_room.M[0][1];
                hmdRotation.M[0][2] = GuiHandler.guiRotation_room.M[0][2];
                hmdRotation.M[1][0] = GuiHandler.guiRotation_room.M[1][0];
                hmdRotation.M[1][1] = GuiHandler.guiRotation_room.M[1][1];
                hmdRotation.M[1][2] = GuiHandler.guiRotation_room.M[1][2];
                hmdRotation.M[2][0] = GuiHandler.guiRotation_room.M[2][0];
                hmdRotation.M[2][1] = GuiHandler.guiRotation_room.M[2][1];
                hmdRotation.M[2][2] = GuiHandler.guiRotation_room.M[2][2];
            }
            this.mc.getProfiler().popPush("hmdSampling");
            this.hmdSampling();

            this.mc.getProfiler().pop();
        }
    }

    @Override
    public void processInputs() {
    }

    @Override
    @Deprecated
    protected void triggerBindingHapticPulse(KeyMapping binding, int duration) {
    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping binding) {
        return null;
    }

    @Override
    public Matrix4f getControllerComponentTransform(int controllerIndex, String componenetName) {
        return new Matrix4f();
    }

    @Override
    public String getOriginName(long handle) {
        return "NullDriver";
    }

    @Override
    public boolean postinit() {
        this.populateInputActions();
        return true;
    }

    @Override
    public boolean hasThirdController() {
        return false;
    }

    @Override
    public List<Long> getOrigins(VRInputAction var1) {
        return null;
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new NullVRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) && MethodHolder.isKeyDown(GLFW.GLFW_KEY_F6)) {
            if (!vrActiveChangedLastFrame) {
                vrActive = !vrActive;
                vrActiveChangedLastFrame = true;
            }
        } else {
            vrActiveChangedLastFrame = false;
        }
        return vrActive;
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
}
