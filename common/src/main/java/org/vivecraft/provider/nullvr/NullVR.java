package org.vivecraft.provider.nullvr;

import org.vivecraft.ClientDataHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.provider.MCVR;
import org.vivecraft.provider.openvr_jna.VRInputAction;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Matrix4f;

import java.util.*;

public class NullVR extends MCVR
{
    protected static NullVR ome;

    public NullVR(Minecraft mc, ClientDataHolder dh)
    {
        super(mc, dh);
        ome = this;
        this.hapticScheduler = new NullVRHapticScheduler();
    }

    public static NullVR get()
    {
        return ome;
    }


    @Override
    public void destroy()
    {
        this.initialized = false;
    }

    @Override
    public String getID()
    {
        return "nullDriver";
    }

    @Override
    public String getName()
    {
        return "nullDriver";
    }

    @Override
    public float[] getPlayAreaSize()
    {
        return new float[]{2f, 2f};
    }

    @Override
    public boolean init()
    {
        if (!this.initialized)
        {
            this.mc = Minecraft.getInstance();

            // only supports seated mode
            System.out.println("NullDriver. Forcing seated mode.");
            this.dh.vrSettings.seated = true;

            this.dh.vrSettings.keyholeX = 0F;

            this.headIsTracking = false;
            Utils.Matrix4fSetIdentity(this.hmdPose);
            this.hmdPose.M[1][3] = 1.62F;

            // eye offset, 10cm total distance
            this.hmdPoseLeftEye.M[0][3] = -0.05F;
            this.hmdPoseRightEye.M[0][3] = 0.05F;

            this.initialized = true;
            this.initSuccess = true;
        }

        return true;
    }

    @Override
    public void poll(long frameIndex)
    {
        if (this.initialized)
        {
            this.dh.vrSettings.xSensitivity = this.dh.vrSettings.ySensitivity * 1.636F * ((float)mc.getWindow().getScreenWidth() / (float)mc.getWindow().getScreenHeight());
            this.mc.getProfiler().push("updatePose");

            if (mc.screen == null) {
                this.updateAim();
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
            }
            this.mc.getProfiler().popPush("hmdSampling");
            this.hmdSampling();
            
            this.mc.getProfiler().pop();
        }
    }

    @Override
    public void processInputs()
    {
    }

    @Override
    @Deprecated
    protected void triggerBindingHapticPulse(KeyMapping binding, int duration)
    {
    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping binding)
    {
        return null;
    }

    @Override
    public Matrix4f getControllerComponentTransform(int controllerIndex, String componenetName)
    {
        return new Matrix4f();
    }

    @Override
    public String getOriginName(long handle)
    {
        return "NullDriver";
    }

    @Override
    public boolean postinit()
    {
        this.populateInputActions();
        return true;
    }

    @Override
    public boolean hasThirdController()
    {
        return false;
    }

    @Override
    public List<Long> getOrigins(VRInputAction var1) {
        return null;
    }
}
