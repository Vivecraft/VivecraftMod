package org.vivecraft.client_vr.provider.nullvr;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
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

import java.util.List;

public class NullVR extends MCVR {
    protected static NullVR ome;

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
            this.hmdPose.identity();
            this.hmdPose.m13(1.62F);

            // eye offset, 10cm total distance
            this.hmdPoseLeftEye.m03(-0.05F);
            this.hmdPoseRightEye.m03(0.05F);

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

            this.controllerPose[0].setTransposed(this.controllerPose[0].transpose(new org.joml.Matrix4f()).setTranslation(0.3F, 1.2F, -0.5F));

            this.controllerPose[1].setTransposed(this.controllerPose[1].transpose(new org.joml.Matrix4f()).setTranslation(-0.3F, 1.2F, -0.5F));

            this.dh.vrSettings.xSensitivity = xSens;
            this.dh.vrSettings.keyholeX = xKey;


            // point head in cursor direction

            this.hmdRotation.setTransposed(this.hmdRotation.transpose(new org.joml.Matrix4f()).set3x3(this.handRotation[0].transpose(new org.joml.Matrix4f())));

            if (GuiHandler.guiRotation_room != null) {
                // look at screen, so that it's centered
                this.hmdRotation.setTransposed(this.hmdRotation.transpose(new org.joml.Matrix4f()).set3x3(GuiHandler.guiRotation_room.transpose(new org.joml.Matrix4f())));
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
}
