//package org.vivecraft.client_vr.provider.ovr_lwjgl;
//
//import net.minecraft.client.KeyMapping;
//import net.minecraft.client.Minecraft;
//import org.joml.Vector2f;
//import org.lwjgl.BufferUtils;
//import org.lwjgl.PointerBuffer;
//import org.lwjgl.ovr.*;
//import org.lwjgl.ovr.OVRPosef.Buffer;
//import org.vivecraft.client.VivecraftVRMod;
//import org.vivecraft.client_vr.ClientDataHolderVR;
//import org.vivecraft.client_vr.provider.ControllerType;
//import org.vivecraft.client_vr.provider.MCVR;
//import org.vivecraft.client_vr.provider.VRRenderer;
//import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
//import org.vivecraft.client_vr.settings.VRSettings;
//import org.vivecraft.common.utils.math.Matrix4f;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MC_OVR extends MCVR {
//    protected PointerBuffer session;
//    protected OVREyeRenderDesc eyeRenderDesc0;
//    protected OVREyeRenderDesc eyeRenderDesc1;
//    protected Buffer hmdToEyeViewPose;
//    protected OVRHmdDesc hmdDesc;
//    protected OVRLayerEyeFov layer;
//    protected PointerBuffer textureSwapChainL;
//    protected PointerBuffer textureSwapChainR;
//
//    private OVRGraphicsLuid luid;
//    private final OVRTrackingState trackingState;
//    private final OVRVector3f guardian;
//    private final OVRInputState inputs;
//    private boolean inputInitialized;
//
//    protected static MC_OVR ome;
//
//    public MC_OVR(Minecraft mc, ClientDataHolderVR dh, VivecraftVRMod mod) {
//        super(mc, dh, mod);
//        ome = this;
//        this.hapticScheduler = new OVR_HapticScheduler();
//        this.trackingState = OVRTrackingState.calloc();
//        this.eyeRenderDesc0 = OVREyeRenderDesc.calloc();
//        this.eyeRenderDesc1 = OVREyeRenderDesc.calloc();
//        this.hmdToEyeViewPose = OVRPosef.calloc(2);
//        this.hmdDesc = OVRHmdDesc.calloc();
//        this.inputs = OVRInputState.calloc();
//        this.guardian = OVRVector3f.calloc();
//        this.layer = OVRLayerEyeFov.calloc();
//    }
//
//    @Override
//    public void destroy() {
//        OVR.ovr_DestroyTextureSwapChain(this.session.get(0), this.textureSwapChainL.get(0));
//        OVR.ovr_DestroyTextureSwapChain(this.session.get(0), this.textureSwapChainR.get(0));
//        OVR.ovr_Destroy(this.session.get(0));
//        OVR.ovr_Shutdown();
//
//        this.trackingState.free();
//        this.eyeRenderDesc0.free();
//        this.eyeRenderDesc1.free();
//        this.hmdToEyeViewPose.free();
//        this.hmdDesc.free();
//        this.inputs.free();
//        this.guardian.free();
//        this.layer.free();
//    }
//
//    public static MC_OVR get() {
//        return ome;
//    }
//
//    @Override
//    public String getName() {
//        return "Oculus_LWJGL";
//    }
//
//    @Override
//    public String getID() {
//        return "oculus_lwjgl";
//    }
//
//    @Override
//    public void processInputs() {
//        if (!this.dh.vrSettings.seated && !ClientDataHolderVR.viewonly && this.inputInitialized) {
//            OVR.ovr_GetInputState(this.session.get(0), 3, this.inputs);
//            this.processInputAction(this.getInputAction(this.mc.options.keyAttack), this.inputs.IndexTrigger(0) > 0.5F);
//            this.processInputAction(this.getInputAction(this.mc.options.keyUse), (this.inputs.Buttons() & 1) == 1);
//            this.processInputAction(this.getInputAction(mod.keyRadialMenu), (this.inputs.Buttons() & 2) == 2);
//            this.processInputAction(this.getInputAction(mod.keyVRInteract),
//                this.inputs.IndexTrigger(0) > 0.5F || this.inputs.IndexTrigger(0) > 0.5F ||
//                    this.inputs.IndexTrigger(1) > 0.5F || this.inputs.HandTrigger(1) > 0.5F);
//            this.processInputAction(this.getInputAction(this.mc.options.keyInventory),
//                (this.inputs.Buttons() & 256) == 256);
//            this.processInputAction(this.getInputAction(mod.keyMenuButton), (this.inputs.Buttons() & 512) == 512);
//            this.processInputAction(this.getInputAction(this.mc.options.keyShift), (this.inputs.Buttons() & 4) == 4);
//            this.processInputAction(this.getInputAction(this.mc.options.keyJump),
//                (this.inputs.Buttons() & 1024) == 1024);
//            this.processInputAction(this.getInputAction(mod.keyHotbarNext), this.inputs.HandTrigger(0) > 0.5F);
//            this.processInputAction(this.getInputAction(mod.keyHotbarPrev), this.inputs.HandTrigger(1) > 0.5F);
//            this.processInputAction(this.getInputAction(mod.keyTeleport), this.inputs.IndexTrigger(1) > 0.5F);
//            this.processInputAction(this.getInputAction(mod.keyFreeMoveStrafe), this.inputs.Thumbstick(1).y() > 0.5F);
//            this.getInputAction(mod.keyRotateAxis).analogData[0].x = this.inputs.Thumbstick(0).x();
//            this.getInputAction(mod.keyFreeMoveStrafe).analogData[0].x = this.inputs.Thumbstick(1).x();
//            this.getInputAction(mod.keyFreeMoveStrafe).analogData[0].y = this.inputs.Thumbstick(1).y();
//            this.ignorePressesNextFrame = false;
//        }
//    }
//
//    private void processInputAction(VRInputAction action, boolean buttonstate) {
//        if (action.isActive() && action.isEnabledRaw()) {
//            if (buttonstate && action.isEnabled()) {
//                if (!this.ignorePressesNextFrame) {
//                    // We do this so shit like closing a GUI by clicking a button won't
//                    // also click in the world immediately after.
//                    action.pressBinding();
//                }
//            } else {
//                action.unpressBinding();
//            }
//        } else {
//            action.unpressBinding();
//        }
//    }
//
//    @Override
//    public void poll(long frameIndex) {
//        if (this.initialized) {
//            OVR.ovr_WaitToBeginFrame(this.session.get(0), 0L);
//            OVR.ovr_BeginFrame(this.session.get(0), 0L);
//            OVR.ovr_GetTrackingState(this.session.get(0), OVR.ovr_GetPredictedDisplayTime(this.session.get(0), 0L),
//                true, this.trackingState);
//            OVRPoseStatef ovrposestatef = this.trackingState.HeadPose();
//            OVRPosef ovrposef = ovrposestatef.ThePose();
//            this.headIsTracking =
//                (this.trackingState.StatusFlags() & OVR.ovrStatus_PositionTracked) == OVR.ovrStatus_PositionTracked;
//
//            OVRUtil.ovr_CalcEyePoses(ovrposef, this.hmdToEyeViewPose, this.layer.RenderPose());
//            this.hmdPoseLeftEye = OVRUtils.ovrPoseToMatrix(this.layer.RenderPose(0));
//            this.hmdPoseRightEye = OVRUtils.ovrPoseToMatrix(this.layer.RenderPose(1));
//
//            if (this.headIsTracking) {
//                this.hmdPose = OVRUtils.ovrPoseToMatrix(ovrposef);
//            } else {
//                this.hmdPose.SetIdentity();
//                this.hmdPose.M[1][3] = 1.62F;
//            }
//
//            OVRPoseStatef leftState = this.trackingState.HandPoses(0);
//            OVRPosef leftPose = leftState.ThePose();
//
//            OVRPoseStatef rightState = this.trackingState.HandPoses(1);
//            OVRPosef rightPose = rightState.ThePose();
//
//            this.controllerPose[0] = OVRUtils.ovrPoseToMatrix(leftPose);
//            this.controllerPose[1] = OVRUtils.ovrPoseToMatrix(rightPose);
//            //TODO: velocities
//
//            this.controllerTracking[0] = (this.trackingState.HandStatusFlags(0) & OVR.ovrStatus_PositionTracked) ==
//                OVR.ovrStatus_PositionTracked;
//            this.controllerTracking[1] = (this.trackingState.HandStatusFlags(1) & OVR.ovrStatus_PositionTracked) ==
//                OVR.ovrStatus_PositionTracked;
//
//            this.updateAim();
//
//            this.processInputs();
//        }
//    }
//
//    @Override
//    public Vector2f getPlayAreaSize() {
//        OVR.ovr_GetBoundaryDimensions(this.session.get(0), 256, this.guardian);
//        return new Vector2f(this.guardian.x(), this.guardian.z());
//    }
//
//    @Override
//    public boolean init() {
//        if (this.initialized) {
//            return true;
//        } else {
//            OVR.ovr_Initialize(null);
//            this.session = BufferUtils.createPointerBuffer(1);
//            this.luid = OVRGraphicsLuid.create();
//
//            if (OVR.ovr_Create(this.session, this.luid) != 0) {
//                this.initStatus = "Couldn't create OVR!";
//                VRSettings.logger.error(this.initStatus);
//                return false;
//            } else {
//                VRSettings.logger.info("Oculus OVR loaded.");
//
//                OVR.ovr_GetHmdDesc(this.session.get(0), this.hmdDesc);
//
//                VRSettings.logger.info("Oculus hmd res: {}x{}", this.hmdDesc.Resolution().w(),
//                    this.hmdDesc.Resolution().h());
//
//                OVR.ovr_GetRenderDesc(this.session.get(0), OVR.ovrEye_Left, this.hmdDesc.DefaultEyeFov(OVR.ovrEye_Left),
//                    this.eyeRenderDesc0);
//                OVR.ovr_GetRenderDesc(this.session.get(0), OVR.ovrEye_Right,
//                    this.hmdDesc.DefaultEyeFov(OVR.ovrEye_Right), this.eyeRenderDesc1);
//
//                this.hmdToEyeViewPose.put(OVR.ovrEye_Left, this.eyeRenderDesc0.HmdToEyePose());
//                this.hmdToEyeViewPose.put(OVR.ovrEye_Right, this.eyeRenderDesc1.HmdToEyePose());
//
//                OVR.ovr_SetTrackingOriginType(this.session.get(0), OVR.ovrTrackingOrigin_FloorLevel);
//                this.initialized = true;
//                this.initSuccess = true;
//                return true;
//            }
//        }
//    }
//
//    @Override
//    public boolean postinit() {
//        this.populateInputActions();
//        VRSettings.logger.info("Oculus Keybinds loaded.");
//        this.inputInitialized = true;
//        return this.inputInitialized;
//    }
//
//    @Override
//    public Matrix4f getControllerComponentTransform(int c, String name) {
//        return new Matrix4f();
//    }
//
//    @Override
//    public boolean hasCameraTracker() {
//        return false;
//    }
//
//    @Override
//    public List<Long> getOrigins(VRInputAction vrInputAction) {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public String getOriginName(long l) {
//        return "";
//    }
//
//    @Override
//    public VRRenderer createVRRenderer() {
//        return new OVR_StereoRenderer(this);
//    }
//
//    @Override
//    public boolean isActive() {
//        return true;
//    }
//
//    @Override
//    protected void triggerBindingHapticPulse(KeyMapping key, int i) {}
//
//    @Override
//    protected ControllerType findActiveBindingControllerType(KeyMapping key) {
//        return null;
//    }
//}
