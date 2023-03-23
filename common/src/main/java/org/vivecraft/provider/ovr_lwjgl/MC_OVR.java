//package org.vivecraft.provider.ovr_lwjgl;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.lwjgl.BufferUtils;
//import org.lwjgl.PointerBuffer;
//import org.lwjgl.ovr.OVR;
//import org.lwjgl.ovr.OVREyeRenderDesc;
//import org.lwjgl.ovr.OVRGraphicsLuid;
//import org.lwjgl.ovr.OVRHmdDesc;
//import org.lwjgl.ovr.OVRInitParams;
//import org.lwjgl.ovr.OVRInputState;
//import org.lwjgl.ovr.OVRLayerEyeFov;
//import org.lwjgl.ovr.OVRPoseStatef;
//import org.lwjgl.ovr.OVRPosef;
//import org.lwjgl.ovr.OVRPosef.Buffer;
//import org.lwjgl.ovr.OVRTrackingState;
//import org.lwjgl.ovr.OVRUtil;
//import org.lwjgl.ovr.OVRVector3f;
//import org.vivecraft.provider.ControllerType;
//import org.vivecraft.provider.MCVR;
//import org.vivecraft.provider.openvr_jna.VRInputAction;
//import org.vivecraft.utils.math.Matrix4f;
//
//import org.vivecraft.DataHolder;
//
//import net.minecraft.client.KeyMapping;
//import net.minecraft.client.Minecraft;
//
//public class MC_OVR extends MCVR
//{
//    PointerBuffer session;
//    OVRGraphicsLuid luid;
//    OVREyeRenderDesc eyeRenderDesc0;
//    OVREyeRenderDesc eyeRenderDesc1;
//    Buffer hmdToEyeViewPose;
//    OVRHmdDesc hmdDesc;
//    OVRLayerEyeFov layer;
//    PointerBuffer textureSwapChainL;
//    PointerBuffer textureSwapChainR;
//    OVRTrackingState trackingState;
//    protected static MC_OVR ome;
//    OVRVector3f guardian;
//    OVRInputState inputs;
//    private boolean inputInitialized;
//
//    public MC_OVR(Minecraft mc, DataHolder dh)
//    {
//        super(mc, dh);
//        ome = this;
//        this.hapticScheduler = new OVR_HapticScheduler();
//        this.trackingState = OVRTrackingState.malloc();
//        this.eyeRenderDesc0 = OVREyeRenderDesc.malloc();
//        this.eyeRenderDesc1 = OVREyeRenderDesc.malloc();
//        this.hmdToEyeViewPose = OVRPosef.create(2);
//        this.hmdDesc = OVRHmdDesc.malloc();
//        this.inputs = OVRInputState.malloc();
//        this.guardian = OVRVector3f.malloc();
//        this.layer = OVRLayerEyeFov.malloc();
//    }
//
//    public static MC_OVR get()
//    {
//        return ome;
//    }
//
//    public String getName()
//    {
//        return "Oculus_LWJGL";
//    }
//
//    public String getID()
//    {
//        return "oculus_lwjgl";
//    }
//
//    public void processInputs()
//    {
//        if (!this.dh.vrSettings.seated && !DataHolder.viewonly && this.inputInitialized)
//        {
//            OVR.ovr_GetInputState(this.session.get(0), 3, this.inputs);
//            this.processInputAction(this.getInputAction(this.mc.options.keyAttack), this.inputs.IndexTrigger(0) > 0.5F);
//            this.processInputAction(this.getInputAction(this.mc.options.keyUse), (this.inputs.Buttons() & 1) == 1);
//            this.processInputAction(this.getInputAction(this.keyRadialMenu), (this.inputs.Buttons() & 2) == 2);
//            this.processInputAction(this.getInputAction(this.keyVRInteract), this.inputs.IndexTrigger(0) > 0.5F || this.inputs.IndexTrigger(0) > 0.5F || this.inputs.IndexTrigger(1) > 0.5F || this.inputs.HandTrigger(1) > 0.5F);
//            this.processInputAction(this.getInputAction(this.mc.options.keyInventory), (this.inputs.Buttons() & 256) == 256);
//            this.processInputAction(this.getInputAction(this.keyMenuButton), (this.inputs.Buttons() & 512) == 512);
//            this.processInputAction(this.getInputAction(this.mc.options.keyShift), (this.inputs.Buttons() & 4) == 4);
//            this.processInputAction(this.getInputAction(this.mc.options.keyJump), (this.inputs.Buttons() & 1024) == 1024);
//            this.processInputAction(this.getInputAction(this.keyHotbarNext), this.inputs.HandTrigger(0) > 0.5F);
//            this.processInputAction(this.getInputAction(this.keyHotbarPrev), this.inputs.HandTrigger(1) > 0.5F);
//            this.processInputAction(this.getInputAction(this.keyTeleport), this.inputs.IndexTrigger(1) > 0.5F);
//            this.processInputAction(this.getInputAction(this.keyFreeMoveStrafe), this.inputs.Thumbstick(1).y() > 0.5F);
//            this.getInputAction(this.keyRotateAxis).analogData[0].x = this.inputs.Thumbstick(0).x();
//            this.getInputAction(this.keyFreeMoveStrafe).analogData[0].x = this.inputs.Thumbstick(1).x();
//            this.getInputAction(this.keyFreeMoveStrafe).analogData[0].y = this.inputs.Thumbstick(1).y();
//            this.ignorePressesNextFrame = false;
//        }
//    }
//
//    private void processInputAction(VRInputAction action, boolean buttonstate)
//    {
//        if (action.isActive() && action.isEnabledRaw())
//        {
//            if (buttonstate && action.isEnabled())
//            {
//                if (!this.ignorePressesNextFrame)
//                {
//                    action.pressBinding();
//                }
//            }
//            else
//            {
//                action.unpressBinding();
//            }
//        }
//        else
//        {
//            action.unpressBinding();
//        }
//    }
//
//    public void destroy()
//    {
//        OVR.ovr_DestroyTextureSwapChain(this.session.get(0), this.textureSwapChainL.get(0));
//        OVR.ovr_DestroyTextureSwapChain(this.session.get(0), this.textureSwapChainR.get(0));
//        OVR.ovr_Destroy(this.session.get(0));
//        OVR.ovr_Shutdown();
//    }
//
//    public void poll(long frameIndex)
//    {
//        if (this.initialized)
//        {
//            OVR.ovr_WaitToBeginFrame(this.session.get(0), 0L);
//            OVR.ovr_BeginFrame(this.session.get(0), 0L);
//            OVR.ovr_GetTrackingState(this.session.get(0), OVR.novr_GetPredictedDisplayTime(this.session.get(0), 0L), true, this.trackingState);
//            OVRPoseStatef ovrposestatef = this.trackingState.HeadPose();
//            OVRPosef ovrposef = ovrposestatef.ThePose();
//            this.headIsTracking = (this.trackingState.StatusFlags() & 2) == 2;
//            OVRUtil.ovr_CalcEyePoses(ovrposef, this.hmdToEyeViewPose, this.layer.RenderPose());
//            this.hmdPoseLeftEye = OVRUtils.ovrPoseToMatrix(this.layer.RenderPose(0));
//            this.hmdPoseRightEye = OVRUtils.ovrPoseToMatrix(this.layer.RenderPose(1));
//
//            if (this.headIsTracking)
//            {
//                this.hmdPose = OVRUtils.ovrPoseToMatrix(ovrposef);
//            }
//            else
//            {
//                this.hmdPose.SetIdentity();
//                this.hmdPose.M[1][3] = 1.62F;
//            }
//
//            OVRPoseStatef ovrposestatef1 = this.trackingState.HandPoses(0);
//            OVRPosef ovrposef1 = ovrposestatef1.ThePose();
//            OVRPoseStatef ovrposestatef2 = this.trackingState.HandPoses(1);
//            OVRPosef ovrposef2 = ovrposestatef2.ThePose();
//            this.controllerPose[0] = OVRUtils.ovrPoseToMatrix(ovrposef1);
//            this.controllerPose[1] = OVRUtils.ovrPoseToMatrix(ovrposef2);
//            this.controllerTracking[0] = (this.trackingState.HandStatusFlags(0) & 2) == 2;
//            this.controllerTracking[1] = (this.trackingState.HandStatusFlags(1) & 2) == 2;
//            this.updateAim();
//            this.processInputs();
//        }
//    }
//
//    public float[] getPlayAreaSize()
//    {
//        OVR.ovr_GetBoundaryDimensions(this.session.get(0), 256, this.guardian);
//        return new float[] {this.guardian.x(), this.guardian.z()};
//    }
//
//    public boolean init()
//    {
//        if (this.initialized)
//        {
//            return true;
//        }
//        else
//        {
//            OVR.ovr_Initialize((OVRInitParams)null);
//            this.session = BufferUtils.createPointerBuffer(1);
//            this.luid = OVRGraphicsLuid.create();
//
//            if (OVR.ovr_Create(this.session, this.luid) != 0)
//            {
//                this.initStatus = "Couldn't create OVR!";
//                System.err.println(this.initStatus);
//                return false;
//            }
//            else
//            {
//                System.out.println("Oculus OVR loaded.");
//                OVR.ovr_GetHmdDesc(this.session.get(0), this.hmdDesc);
//                System.out.println("Oculus hmd res: " + this.hmdDesc.Resolution().w() + " x " + this.hmdDesc.Resolution().h());
//                OVR.ovr_GetRenderDesc(this.session.get(0), 0, this.hmdDesc.DefaultEyeFov(0), this.eyeRenderDesc0);
//                OVR.ovr_GetRenderDesc(this.session.get(0), 1, this.hmdDesc.DefaultEyeFov(1), this.eyeRenderDesc1);
//                this.hmdToEyeViewPose.put(0, this.eyeRenderDesc0.HmdToEyePose());
//                this.hmdToEyeViewPose.put(1, this.eyeRenderDesc1.HmdToEyePose());
//                OVR.ovr_SetTrackingOriginType(this.session.get(0), 1);
//                this.initialized = true;
//                this.initSuccess = true;
//                return true;
//            }
//        }
//    }
//
//    public boolean postinit()
//    {
//        this.populateInputActions();
//        System.out.println("Oculus Keybinds loaded.");
//        this.inputInitialized = true;
//        return this.inputInitialized;
//    }
//
//    public Matrix4f getControllerComponentTransform(int c, String name)
//    {
//        return new Matrix4f();
//    }
//
//    public boolean hasThirdController()
//    {
//        return false;
//    }
//
//    public List<Long> getOrigins(VRInputAction vrInputAction)
//    {
//        return new ArrayList<>();
//    }
//
//    protected void triggerBindingHapticPulse(KeyMapping key, int i)
//    {
//    }
//
//    protected ControllerType findActiveBindingControllerType(KeyMapping key)
//    {
//        return null;
//    }
//}
