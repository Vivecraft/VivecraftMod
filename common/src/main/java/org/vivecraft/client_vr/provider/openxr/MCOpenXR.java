package org.vivecraft.client_vr.provider.openxr;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWNativeGLX;
import org.lwjgl.glfw.GLFWNativeWGL;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.system.Struct;
import org.lwjgl.system.linux.X11;
import org.lwjgl.system.windows.User32;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.provider.control.VRInputAction;
import org.vivecraft.client_vr.provider.control.VRInputActionSet;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.math.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.opengl.GLX13.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MCOpenXR extends MCVR {

    private static MCOpenXR ome;
    public XrInstance instance;
    public XrSession session;
    public XrSpace xrAppSpace;
    public XrSpace xrViewSpace;
    public XrSwapchain swapchain;
    public final XrEventDataBuffer eventDataBuffer = XrEventDataBuffer.calloc();
    public long time;
    private boolean tried;
    private long systemID;
    public XrView.Buffer viewBuffer;
    public int width;
    public int height;
    //TODO either move to MCVR, Or make special for OpenXR holding the instance itself.
    private final Map<VRInputActionSet, Long> actionSetHandles = new EnumMap<>(VRInputActionSet.class);
    //TODO Move to MCVR
    private  XrActiveActionSet.Buffer activeActionSetsBuffer;
    private boolean isActive;
    private final HashMap<String, Long> paths = new HashMap<>();
    private final long[] grip = new long[2];
    private final long[] aim = new long[2];
    private final XrSpace[] gripSpace = new XrSpace[2];
    private final XrSpace[] aimSpace = new XrSpace[2];
    public static final XrPosef POSE_IDENTITY = XrPosef.calloc().set(
        XrQuaternionf.calloc().set(0, 0, 0, 1),
        XrVector3f.calloc()
    );


    public MCOpenXR(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh, VivecraftVRMod.INSTANCE);
        ome = this;
        this.hapticScheduler = new OpenXRHapticSchedular();

    }

    @Override
    public String getName() {
        return "OpenXR";
    }

    @Override
    public String getID() {
        return "openxr";
    }

    @Override
    public void destroy() {
        //Not sure if we need the action sets one here, as we are shutting down
        for (Long inputActionSet : actionSetHandles.values()){
            XR10.xrDestroyActionSet(new XrActionSet(inputActionSet, instance));
        }
        if (swapchain != null) {
            XR10.xrDestroySwapchain(swapchain);
        }
        if (viewBuffer != null) {
            viewBuffer.close();
        }
        if (xrAppSpace != null) {
            XR10.xrDestroySpace(xrAppSpace);
        }
        if (xrViewSpace != null) {
            XR10.xrDestroySpace(xrViewSpace);
        }
        if (session != null){
            XR10.xrEndSession(session);
        }
        if (instance != null){
            XR10.xrEndSession(session);
        }
        eventDataBuffer.close();
    }

    @Override
    protected void triggerBindingHapticPulse(KeyMapping var1, int var2) {

    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping var1) {
        return null;
    }

    @Override
    public void poll(long var1) {
        if (this.initialized) {
            this.mc.getProfiler().push("events");
            pollVREvents();

            if (!this.dh.vrSettings.seated) {
                this.mc.getProfiler().popPush("controllers");
                this.mc.getProfiler().push("gui");

                if (this.mc.screen == null && this.dh.vrSettings.vrTouchHotbar) {
                    VRSettings vrsettings = this.dh.vrSettings;

                    if (this.dh.vrSettings.vrHudLockMode != VRSettings.HUDLock.HEAD && this.hudPopup) {
                        this.processHotbar();
                    }
                }

                this.mc.getProfiler().pop();
            }
            this.mc.getProfiler().popPush("updatePose/Vsync");
            this.updatePose();
            this.mc.getProfiler().popPush("processInputs");
            this.processInputs();
            this.mc.getProfiler().popPush("hmdSampling");
            this.hmdSampling();
            this.mc.getProfiler().pop();
        }
    }

    private void updatePose() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrSpaceLocation space_location = XrSpaceLocation.calloc(stack).type(XR10.XR_TYPE_SPACE_LOCATION);
            //HMD pose
            XR10.xrLocateSpace(xrViewSpace, xrAppSpace, time, space_location);
            OpenXRUtil.openXRPoseToMarix(space_location.pose(), this.hmdPose);

            //Eye positions
            OpenXRUtil.openXRPoseToMarix(viewBuffer.get(0).pose(), this.hmdPoseLeftEye);
            viewBuffer.get(0).pose();
            viewBuffer.get(0).fov();

            OpenXRUtil.openXRPoseToMarix(viewBuffer.get(1).pose(), this.hmdPoseRightEye);
            viewBuffer.get(1).pose();
            viewBuffer.get(1).fov();

            //Controller aim and grip poses
            XR10.xrLocateSpace(gripSpace[0], xrAppSpace, time, space_location);
            OpenXRUtil.openXRPoseToMarix(space_location.pose(), this.controllerPose[0]);
            OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(), this.controllerRotation[0]);
            XR10.xrLocateSpace(gripSpace[1], xrAppSpace, time, space_location);
            OpenXRUtil.openXRPoseToMarix(space_location.pose(), this.controllerPose[1]);
            OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(), this.controllerRotation[1]);
            XR10.xrLocateSpace(aimSpace[0], xrAppSpace, time, space_location);
            OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(), this.handRotation[0]);
            XR10.xrLocateSpace(aimSpace[1], xrAppSpace, time, space_location);
            OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(), this.handRotation[1]);

            if (this.inputInitialized) {
                this.mc.getProfiler().push("updateActionState");

                if (this.updateActiveActionSets()) {
                    XrActionsSyncInfo syncInfo = XrActionsSyncInfo.calloc(stack)
                        .type(XR10.XR_TYPE_ACTIONS_SYNC_INFO)
                        .activeActionSets(activeActionSetsBuffer);
                    XR10.xrSyncActions(session, syncInfo);
                }

                this.inputActions.values().forEach(this::readNewData);

                //TODO Not needed it seems? Poses come from the action space
                XrActionSet actionSet = new XrActionSet(this.actionSetHandles.get(VRInputActionSet.GLOBAL), instance);
                this.readPoseData(this.grip[0], actionSet);
                this.readPoseData(this.grip[1], actionSet);
                this.readPoseData(this.aim[0], actionSet);
                this.readPoseData(this.aim[1], actionSet);

                this.mc.getProfiler().pop();
            }

//            if (this.dh.vrSettings.reverseHands) {
//                this.updateControllerPose(0, this.leftPoseHandle);
//                this.updateControllerPose(1, this.rightPoseHandle);
//            } else {
//                this.updateControllerPose(0, this.rightPoseHandle);
//                this.updateControllerPose(1, this.leftPoseHandle);
//            }
//
//            this.updateControllerPose(2, this.externalCameraPoseHandle);
        }

        //this.updateAim();
    }

    public void readNewData(VRInputAction action) {
        String s = action.type;

        switch (s) {
            case "boolean":
                if (action.isHanded()) {
                    for (ControllerType controllertype1 : ControllerType.values()) {
                        this.readBoolean(action, controllertype1);
                    }
                } else {
                    this.readBoolean(action, null);
                }

                break;

            case "vector1":
                if (action.isHanded()) {
                    for (ControllerType controllertype : ControllerType.values()) {
                        this.readFloat(action, controllertype);
                    }
                } else {
                    this.readFloat(action, null);
                }
            case "vector2":
                if (action.isHanded()) {
                    for (ControllerType controllertype : ControllerType.values()) {
                        this.readVecData(action, controllertype);
                    }
                } else {
                    this.readVecData(action, null);
                }
            case "vector3":

        }
    }

    private void readBoolean(VRInputAction action, ControllerType hand) {
        int i = 0;

        if (hand != null) {
            i = hand.ordinal();
        }
        try (MemoryStack stack = MemoryStack.stackPush()){
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            info.action(new XrAction(action.handle, new XrActionSet(actionSetHandles.get(action.actionSet), instance)));
            XrActionStateBoolean state = XrActionStateBoolean.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_BOOLEAN);
            XR10.xrGetActionStateBoolean(session, info, state);

            action.digitalData[i].state = state.currentState();
            action.digitalData[i].isActive = state.isActive();
            action.digitalData[i].isChanged = state.changedSinceLastSync();
        }
    }

    private void readFloat(VRInputAction action, ControllerType hand) {
        int i = 0;

        if (hand != null) {
            i = hand.ordinal();
        }
        try (MemoryStack stack = MemoryStack.stackPush()){
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            info.action(new XrAction(action.handle, new XrActionSet(actionSetHandles.get(action.actionSet), instance)));
            XrActionStateFloat state = XrActionStateFloat.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_FLOAT);
            XR10.xrGetActionStateFloat(session, info, state);

            action.analogData[i].deltaX = action.analogData[i].x - state.currentState();
            action.analogData[i].x = state.currentState();
            //action.analogData[i].activeOrigin = this.analog.activeOrigin();
            action.analogData[i].isActive = state.isActive();
            action.analogData[i].isChanged = state.changedSinceLastSync();
        }
    }

    private void readVecData(VRInputAction action, ControllerType hand) {
        int i = 0;

        if (hand != null) {
            i = hand.ordinal();
        }
        try (MemoryStack stack = MemoryStack.stackPush()){
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            info.action(new XrAction(action.handle, new XrActionSet(actionSetHandles.get(action.actionSet), instance)));
            XrActionStateVector2f state = XrActionStateVector2f.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_VECTOR2F);
            XR10.xrGetActionStateVector2f(session, info, state);

            action.analogData[i].deltaX = action.analogData[i].x - state.currentState().x();
            action.analogData[i].deltaX = action.analogData[i].y - state.currentState().y();
            action.analogData[i].x = state.currentState().x();
            action.analogData[i].x = state.currentState().y();
            //action.analogData[i].activeOrigin = this.analog.activeOrigin();
            action.analogData[i].isActive = state.isActive();
            action.analogData[i].isChanged = state.changedSinceLastSync();
        }
    }

    private void readPoseData(Long action, XrActionSet set) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            info.action(new XrAction(action, set));
            XrActionStatePose state = XrActionStatePose.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_POSE);
            XR10.xrGetActionStatePose(session, info, state);
        }
    }

    private boolean updateActiveActionSets() {
        ArrayList<VRInputActionSet> arraylist = new ArrayList<>();
        arraylist.add(VRInputActionSet.GLOBAL);

        // we are always modded
        arraylist.add(VRInputActionSet.MOD);

        arraylist.add(VRInputActionSet.MIXED_REALITY);
        arraylist.add(VRInputActionSet.TECHNICAL);

        if (this.mc.screen == null) {
            arraylist.add(VRInputActionSet.INGAME);
            arraylist.add(VRInputActionSet.CONTEXTUAL);
        } else {
            arraylist.add(VRInputActionSet.GUI);
            if (ClientDataHolderVR.getInstance().vrSettings.ingameBindingsInGui) {
                arraylist.add(VRInputActionSet.INGAME);
            }
        }

        if (KeyboardHandler.Showing || RadialHandler.isShowing()) {
            arraylist.add(VRInputActionSet.KEYBOARD);
        }

        if (this.activeActionSetsBuffer == null) {
            activeActionSetsBuffer = XrActiveActionSet.calloc(arraylist.size());
        } else if (activeActionSetsBuffer.capacity() != arraylist.size()) {
            activeActionSetsBuffer.close();
            activeActionSetsBuffer = XrActiveActionSet.calloc(arraylist.size());
        }

        for (int i = 0; i < arraylist.size(); ++i) {
            VRInputActionSet vrinputactionset = arraylist.get(i);
            activeActionSetsBuffer.get(i).set(new XrActionSet(this.getActionSetHandle(vrinputactionset), instance), NULL);
        }

        return !arraylist.isEmpty();
    }

    private void updateControllerPose(int controller, long actionHandle) {

    }

    long getActionSetHandle(VRInputActionSet actionSet) {
        return this.actionSetHandles.get(actionSet);
    }

    private void pollVREvents() {
        while (true) {
            eventDataBuffer.clear();
            eventDataBuffer.type(XR10.XR_TYPE_EVENT_DATA_BUFFER);
            int error = XR10.xrPollEvent(instance, eventDataBuffer);
            if (error != XR10.XR_SUCCESS) {
                break;
            }
            XrEventDataBaseHeader event = XrEventDataBaseHeader.create(eventDataBuffer.address());

            switch (event.type()) {
                case XR10.XR_TYPE_EVENT_DATA_INSTANCE_LOSS_PENDING -> {
                    XrEventDataInstanceLossPending instanceLossPending = XrEventDataInstanceLossPending.create(event.address());
                }
                case XR10.XR_TYPE_EVENT_DATA_SESSION_STATE_CHANGED -> {
                    this.sessionChanged(XrEventDataSessionStateChanged.create(event.address()));
                }
                case XR10.XR_TYPE_EVENT_DATA_INTERACTION_PROFILE_CHANGED -> {
                }
                case XR10.XR_TYPE_EVENT_DATA_REFERENCE_SPACE_CHANGE_PENDING -> {
                }
                default -> {
                }
            }
        }
    }

    private void sessionChanged(XrEventDataSessionStateChanged xrEventDataSessionStateChanged) {
        int state = xrEventDataSessionStateChanged.state();

        switch (state) {
            case XR10.XR_SESSION_STATE_READY: {
                try (MemoryStack stack = MemoryStack.stackPush()){
                    XrSessionBeginInfo sessionBeginInfo = XrSessionBeginInfo.calloc(stack);
                    sessionBeginInfo.type(XR10.XR_TYPE_SESSION_BEGIN_INFO);
                    sessionBeginInfo.next(NULL);
                    sessionBeginInfo.primaryViewConfigurationType(XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO);

                    XR10.xrBeginSession(session, sessionBeginInfo);
                }
                this.isActive = true;
                break;
            }
            case XR10.XR_SESSION_STATE_STOPPING: {
                this.isActive = false;
                XR10.xrEndSession(session);
            }
            case XR10.XR_SESSION_STATE_EXITING: {
                break;
            }
            case XR10.XR_SESSION_STATE_LOSS_PENDING: {
                break;
            }
            default:
                break;
        }
    }

    @Override
    public Vector2f getPlayAreaSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrExtent2Df vec = XrExtent2Df.calloc(stack);
            XR10.xrGetReferenceSpaceBoundsRect(session, XR10.XR_REFERENCE_SPACE_TYPE_STAGE, vec);
            return new Vector2f(vec.width(), vec.height());
        }
    }

    @Override
    public boolean init() {
        if (this.initialized) {
            return true;
        } else if (this.tried) {
            return this.initialized;
        } else {
            tried = true;
            this.mc = Minecraft.getInstance();
            try {
                this.initializeOpenXRInstance();
                this.initializeOpenXRSession();
                this.initializeOpenXRSpace();
                this.initializeOpenXRSwapChain();
            } catch (Exception e) {
                e.printStackTrace();
                this.initSuccess = false;
                this.initStatus = e.getLocalizedMessage();
                return false;
            }

            //TODO Seated when no controllers

            System.out.println("OpenXR initialized & VR connected.");
            this.deviceVelocity = new Vec3[64];

            for (int i = 0; i < this.poseMatrices.length; ++i) {
                this.poseMatrices[i] = new Matrix4f();
                this.deviceVelocity[i] = new Vec3(0.0D, 0.0D, 0.0D);
            }

            this.initialized = true;
            return true;
        }
    }

    private void initializeOpenXRInstance() {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            //Check extensions
            IntBuffer numExtensions = stack.callocInt(1);
            XR10.xrEnumerateInstanceExtensionProperties((ByteBuffer) null, numExtensions, null);

            XrExtensionProperties.Buffer properties = new XrExtensionProperties.Buffer(
                bufferStack(numExtensions.get(0), XrExtensionProperties.SIZEOF, XR10.XR_TYPE_EXTENSION_PROPERTIES)
            );

            //Load extensions
            XR10.xrEnumerateInstanceExtensionProperties((ByteBuffer) null, numExtensions, properties);

            //get needed extensions
            boolean missingOpenGL = true;
            PointerBuffer extensions = stack.callocPointer(3);
            while (properties.hasRemaining()) {
                XrExtensionProperties prop = properties.get();
                String extensionName = prop.extensionNameString();
                if (extensionName.equals(KHROpenGLEnable.XR_KHR_OPENGL_ENABLE_EXTENSION_NAME)) {
                    missingOpenGL = false;
                    extensions.put(memAddress(stackUTF8(KHROpenGLEnable.XR_KHR_OPENGL_ENABLE_EXTENSION_NAME)));
                }
                if (extensionName.equals(EXTHPMixedRealityController.XR_EXT_HP_MIXED_REALITY_CONTROLLER_EXTENSION_NAME)) {
                    extensions.put(memAddress(stackUTF8(EXTHPMixedRealityController.XR_EXT_HP_MIXED_REALITY_CONTROLLER_EXTENSION_NAME)));
                }
                if (extensionName.equals(HTCViveCosmosControllerInteraction.XR_HTC_VIVE_COSMOS_CONTROLLER_INTERACTION_EXTENSION_NAME)) {
                    extensions.put(memAddress(stackUTF8(HTCViveCosmosControllerInteraction.XR_HTC_VIVE_COSMOS_CONTROLLER_INTERACTION_EXTENSION_NAME)));
                }
            }

            if (missingOpenGL) {
                throw new RuntimeException("OpenXR runtime does not support OpenGL, try using SteamVR instead");
            }

            //Create APP info
            XrApplicationInfo applicationInfo = XrApplicationInfo.calloc(stack);
            applicationInfo.apiVersion(XR10.XR_CURRENT_API_VERSION);
            applicationInfo.applicationName(stack.UTF8("Vivecraft"));
            applicationInfo.applicationVersion(1);

            //Create instance info
            XrInstanceCreateInfo createInfo = XrInstanceCreateInfo.calloc(stack);
            createInfo.type(XR10.XR_TYPE_INSTANCE_CREATE_INFO);
            createInfo.next(NULL);
            createInfo.createFlags(0);
            createInfo.applicationInfo(applicationInfo);
            createInfo.enabledApiLayerNames(null);
            createInfo.enabledExtensionNames(extensions.flip());

            //Create XR instance
            PointerBuffer instancePtr = stack.callocPointer(1);
            int xrResult = XR10.xrCreateInstance(createInfo, instancePtr);
            if (xrResult == XR10.XR_ERROR_RUNTIME_FAILURE) {
                throw new RuntimeException("Failed to create xrInstance, are you sure your headset is plugged in?");
            } else if (xrResult == XR10.XR_ERROR_INSTANCE_LOST) {
                throw new RuntimeException("Failed to create xrInstance due to runtime updating");
            } else if (xrResult < 0) {
                throw new RuntimeException("XR method returned " + xrResult);
            }
            instance = new XrInstance(instancePtr.get(0), createInfo);

            this.poseMatrices = new Matrix4f[64];

            for (int i = 0; i < this.poseMatrices.length; ++i) {
                this.poseMatrices[i] = new Matrix4f();
            }

            this.initSuccess = true;
        }
    }

    public static MCOpenXR get() {
        return ome;
    }

    private void initializeOpenXRSession() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //Create system
            XrSystemGetInfo system = XrSystemGetInfo.calloc(stack);
            system.type(XR10.XR_TYPE_SYSTEM_GET_INFO);
            system.next(NULL);
            system.formFactor(XR10.XR_FORM_FACTOR_HEAD_MOUNTED_DISPLAY);

            LongBuffer longBuffer = stack.callocLong(1);
            XR10.xrGetSystem(instance, system, longBuffer);
            this.systemID = longBuffer.get(0);

            if (systemID == 0) {
                throw new RuntimeException("No compatible headset detected");
            }

            //Bind graphics
            Struct graphics = this.getGraphicsAPI(stack);

            //Create session
            XrSessionCreateInfo info = XrSessionCreateInfo.calloc(stack);
            info.type(XR10.XR_TYPE_SESSION_CREATE_INFO);
            info.next(graphics.address());
            info.createFlags(0);
            info.systemId(systemID);

            PointerBuffer sessionPtr = stack.callocPointer(1);
            XR10.xrCreateSession(instance, info, sessionPtr);

            session = new XrSession(sessionPtr.get(0), instance);

            XrSessionBeginInfo sessionBeginInfo = XrSessionBeginInfo.calloc(stack);
            sessionBeginInfo.type(XR10.XR_TYPE_SESSION_BEGIN_INFO);
            sessionBeginInfo.next(NULL);
            sessionBeginInfo.primaryViewConfigurationType(XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO);

            XR10.xrBeginSession(session, sessionBeginInfo);
            this.isActive = true;

        }
    }

    private void initializeOpenXRSpace() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            XrPosef identityPose = XrPosef.calloc(stack);
            identityPose.set(
                XrQuaternionf.calloc(stack).set(0, 0, 0, 1),
                XrVector3f.calloc(stack)
            );

            XrReferenceSpaceCreateInfo referenceSpaceCreateInfo = XrReferenceSpaceCreateInfo.calloc(stack);
            referenceSpaceCreateInfo.type(XR10.XR_TYPE_REFERENCE_SPACE_CREATE_INFO);
            referenceSpaceCreateInfo.next(NULL);
            referenceSpaceCreateInfo.referenceSpaceType(XR10.XR_REFERENCE_SPACE_TYPE_STAGE);
            referenceSpaceCreateInfo.poseInReferenceSpace(identityPose);

            PointerBuffer pp = stack.callocPointer(1);
            XR10.xrCreateReferenceSpace(session, referenceSpaceCreateInfo, pp);
            xrAppSpace = new XrSpace(pp.get(0), session);

            referenceSpaceCreateInfo.referenceSpaceType(XR10.XR_REFERENCE_SPACE_TYPE_VIEW);
            XR10.xrCreateReferenceSpace(session, referenceSpaceCreateInfo, pp);
            xrViewSpace = new XrSpace(pp.get(0), session);
        }
    }

    private void initializeOpenXRSwapChain() {
        try (MemoryStack stack = stackPush()) {
            //Check amount of views
            IntBuffer intBuf = stack.callocInt(1);
            XR10.xrEnumerateViewConfigurationViews(instance, systemID,  XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO, intBuf, null);

            //Get all views
            ByteBuffer viewConfBuffer = bufferStack(intBuf.get(0), XrViewConfigurationView.SIZEOF, XR10.XR_TYPE_VIEW_CONFIGURATION_VIEW);
            XrViewConfigurationView.Buffer views = new XrViewConfigurationView.Buffer(viewConfBuffer);
            XR10.xrEnumerateViewConfigurationViews(instance, systemID,  XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO, intBuf, views);
            int viewCountNumber = intBuf.get(0);

            this.viewBuffer = new XrView.Buffer(
                bufferHeap(viewCountNumber, XrView.SIZEOF, XR10.XR_TYPE_VIEW)
            );
            //Check swapchain formats
            XR10.xrEnumerateSwapchainFormats(session, intBuf, null);

            //Get swapchain formats
            LongBuffer swapchainFormats = stack.callocLong(intBuf.get(0));
            XR10.xrEnumerateSwapchainFormats(session, intBuf, swapchainFormats);

            long[] desiredSwapchainFormats = {
                GL11.GL_RGB10_A2,
                GL30.GL_RGBA16F,
                GL30.GL_RGB16F,
                //SRGB formats
                GL21.GL_SRGB8_ALPHA8,
                GL21.GL_SRGB8,
                // The two below should only be used as a fallback, as they are linear color formats without enough bits for color
                // depth, thus leading to banding.
                GL11.GL_RGBA8,
                GL31.GL_RGBA8_SNORM,
            };

            //Choose format
            long chosenFormat = 0;
            for (long glFormatIter : desiredSwapchainFormats) {
                swapchainFormats.rewind();
                while (swapchainFormats.hasRemaining()) {
                    if (glFormatIter == swapchainFormats.get()) {
                        chosenFormat = glFormatIter;
                        break;
                    }
                }
                if (chosenFormat != 0) {
                    break;
                }
            }

            if (chosenFormat == 0) {
                var formats = new ArrayList<Long>();
                swapchainFormats.rewind();
                while (swapchainFormats.hasRemaining()) {
                    formats.add(swapchainFormats.get());
                }
                throw new RuntimeException("No compatible swapchain / framebuffer format available: " + formats);
            }

            //Make swapchain
            XrViewConfigurationView viewConfig = views.get(0);
            XrSwapchainCreateInfo swapchainCreateInfo = XrSwapchainCreateInfo.calloc(stack);
            swapchainCreateInfo.type(XR10.XR_TYPE_SWAPCHAIN_CREATE_INFO);
            swapchainCreateInfo.next(NULL);
            swapchainCreateInfo.createFlags(0);
            swapchainCreateInfo.usageFlags(XR10.XR_SWAPCHAIN_USAGE_COLOR_ATTACHMENT_BIT);
            swapchainCreateInfo.format(chosenFormat);
            swapchainCreateInfo.sampleCount(1);
            swapchainCreateInfo.width(viewConfig.recommendedImageRectWidth());
            swapchainCreateInfo.height(viewConfig.recommendedImageRectHeight());
            swapchainCreateInfo.faceCount(1);
            swapchainCreateInfo.arraySize(2);
            swapchainCreateInfo.mipCount(1);

            PointerBuffer handlePointer = stack.callocPointer(1);
            XR10.xrCreateSwapchain(session, swapchainCreateInfo, handlePointer);
            swapchain = new XrSwapchain(handlePointer.get(0), session);
            this.width = swapchainCreateInfo.width();
            this.height = swapchainCreateInfo.height();
        }
    }

    private Struct getGraphicsAPI(MemoryStack stack) {
        XrGraphicsRequirementsOpenGLKHR graphicsRequirements = XrGraphicsRequirementsOpenGLKHR.calloc(stack).type(KHROpenGLEnable.XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_KHR);
        KHROpenGLEnable.xrGetOpenGLGraphicsRequirementsKHR(instance, systemID, graphicsRequirements);

        XrSystemProperties systemProperties = XrSystemProperties.calloc(stack).type(XR10.XR_TYPE_SYSTEM_PROPERTIES);
        XR10.xrGetSystemProperties(instance, systemID, systemProperties);
        XrSystemTrackingProperties trackingProperties = systemProperties.trackingProperties();
        XrSystemGraphicsProperties graphicsProperties = systemProperties.graphicsProperties();

        String systemName = memUTF8(memAddress(systemProperties.systemName()));
        int vendor = systemProperties.vendorId();
        boolean orientationTracking = trackingProperties.orientationTracking();
        boolean positionTracking = trackingProperties.positionTracking();
        int maxWidth = graphicsProperties.maxSwapchainImageWidth();
        int maxHeight = graphicsProperties.maxSwapchainImageHeight();
        int maxLayerCount = graphicsProperties.maxLayerCount();

        System.out.println(String.format("Found device with id: %d", systemID));
        System.out.println(String.format("Headset Name:%s Vendor:%d ", systemName, vendor));
        System.out.println(String.format("Headset Orientation Tracking:%b Position Tracking:%b ", orientationTracking, positionTracking));
        System.out.println(String.format("Headset Max Width:%d Max Height:%d Max Layer Count:%d ", maxWidth, maxHeight, maxLayerCount));
        
        //Bind the OpenGL context to the OpenXR instance and create the session
        Window window = mc.getWindow();
        long windowHandle = window.getWindow();
        if (Platform.get() == Platform.WINDOWS) {
            return XrGraphicsBindingOpenGLWin32KHR.calloc(stack).set(
                KHROpenGLEnable.XR_TYPE_GRAPHICS_BINDING_OPENGL_WIN32_KHR,
                NULL,
                User32.GetDC(GLFWNativeWin32.glfwGetWin32Window(windowHandle)),
                GLFWNativeWGL.glfwGetWGLContext(windowHandle)
            );
        } else if (Platform.get() == Platform.LINUX) {
            long xDisplay = GLFWNativeX11.glfwGetX11Display();

            long glXContext = GLFWNativeGLX.glfwGetGLXContext(windowHandle);
            long glXWindowHandle = GLFWNativeGLX.glfwGetGLXWindow(windowHandle);

            int fbXID = glXQueryDrawable(xDisplay, glXWindowHandle, GLX_FBCONFIG_ID);
            PointerBuffer fbConfigBuf = glXChooseFBConfig(xDisplay, X11.XDefaultScreen(xDisplay), stackInts(GLX_FBCONFIG_ID, fbXID, 0));
            if(fbConfigBuf == null) {
                throw new IllegalStateException("Your framebuffer config was null, make a github issue");
            }
            long fbConfig = fbConfigBuf.get();

            return XrGraphicsBindingOpenGLXlibKHR.calloc(stack).set(
                KHROpenGLEnable.XR_TYPE_GRAPHICS_BINDING_OPENGL_XLIB_KHR,
                NULL,
                xDisplay,
                (int) Objects.requireNonNull(glXGetVisualFromFBConfig(xDisplay, fbConfig)).visualid(),
                fbConfig,
                glXWindowHandle,
                glXContext
            );
        } else {
            throw new IllegalStateException("Macos not supported");
        }
    }

    /**
     * Creates an array of XrStructs with their types pre set to @param type
     */
    static ByteBuffer bufferStack(int capacity, int sizeof, int type) {
        ByteBuffer b = stackCalloc(capacity * sizeof);

        for (int i = 0; i < capacity; i++) {
            b.position(i * sizeof);
            b.putInt(type);
        }
        b.rewind();
        return b;
    }


    @Override
    public boolean postinit() throws RenderConfigException {
        this.initInputAndApplication();
        return inputInitialized;
    }

    private void initInputAndApplication() {
        this.populateInputActions();

        //this.generateActionManifest();
        //this.loadActionManifest();
        this.loadActionHandles();
        this.loadDefaultBindings();
        //this.installApplicationManifest(false);
        this.inputInitialized = true;
        
    }

    @Override
    public Matrix4f getControllerComponentTransform(int var1, String var2) {
        return Utils.Matrix4fSetIdentity(new Matrix4f());
    }

    @Override
    public boolean hasThirdController() {
        return false;
    }

    @Override
    public List<Long> getOrigins(VRInputAction var1) {
        return List.of(0L);
    }

    @Override
    public String getOriginName(long l) {
        return "null";
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new OpenXRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public ControllerType getOriginControllerType(long i) {
        return ControllerType.LEFT;
    }

    //TODO Collect and register all actions
    private void loadActionHandles() {
        for (VRInputActionSet vrinputactionset : VRInputActionSet.values()) {
            long actionSet = makeActionSet(instance, vrinputactionset.name, vrinputactionset.localizedName, 0);
            this.actionSetHandles.put(vrinputactionset, actionSet);
        }

        for (VRInputAction vrinputaction : this.inputActions.values()) {
            long action = createAction(vrinputaction.name, vrinputaction.name, vrinputaction.type, new XrActionSet(this.actionSetHandles.get(vrinputaction.actionSet), instance));
            vrinputaction.setHandle(action);
        }

        setupControllers();
    }

    private void setupControllers() {
        XrActionSet actionSet = new XrActionSet(this.actionSetHandles.get(VRInputActionSet.GLOBAL), instance);
        this.grip[0] = createAction("/actions/global/in/lefthand", "/actions/global/in/lefthand", "pose", actionSet);
        this.grip[1] = createAction("/actions/global/in/righthand", "/actions/global/in/righthand", "pose", actionSet);
        this.aim[0] = createAction("/actions/global/in/lefthandaim", "/actions/global/in/lefthandaim", "pose", actionSet);
        this.aim[1] = createAction("/actions/global/in/righthandaim", "/actions/global/in/righthandaim", "pose", actionSet);

        try (MemoryStack stack = MemoryStack.stackPush()){
            XrActionSpaceCreateInfo grip_left = XrActionSpaceCreateInfo.calloc(stack);
            grip_left.type(XR10.XR_TYPE_ACTION_SPACE_CREATE_INFO);
            grip_left.next(NULL);
            grip_left.action(new XrAction(grip[0], actionSet));
            grip_left.subactionPath(getPath("/user/hand/left/input/grip/pose"));
            grip_left.poseInActionSpace(POSE_IDENTITY);
            PointerBuffer pp = stackCallocPointer(1);
            XR10.xrCreateActionSpace(session, grip_left, pp);
            this.gripSpace[0] = new XrSpace(pp.get(0), session);

            grip_left.action(new XrAction(grip[1], actionSet));
            grip_left.subactionPath(getPath("/user/hand/right/input/grip/pose"));
            XR10.xrCreateActionSpace(session, grip_left, pp);
            this.gripSpace[1] = new XrSpace(pp.get(0), session);

            grip_left.action(new XrAction(aim[0], actionSet));
            grip_left.subactionPath(getPath("/user/hand/left/input/aim/pose"));
            XR10.xrCreateActionSpace(session, grip_left, pp);
            this.aimSpace[0] = new XrSpace(pp.get(0), session);

            grip_left.action(new XrAction(aim[1], actionSet));
            grip_left.subactionPath(getPath("/user/hand/right/input/aim/pose"));
            XR10.xrCreateActionSpace(session, grip_left, pp);
            this.aimSpace[1] = new XrSpace(pp.get(0), session);


        }

    }

    private void loadDefaultBindings() {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            for (String headset: XRBindings.supportedHeadsets()) {
                Pair<String, String>[] defaultBindings = XRBindings.getBinding(headset).toArray(new Pair[0]);
                XrActionSuggestedBinding.Buffer bindings = XrActionSuggestedBinding.calloc(defaultBindings.length + 4, stack); //TODO different way of adding controller poses

                for (int i = 0; i < defaultBindings.length; i++) {
                    Pair<String, String> pair = defaultBindings[i];
                    VRInputAction binding = this.getInputActionByName(pair.getLeft());
                    bindings.get(i).set(
                        new XrAction(binding.handle, new XrActionSet(actionSetHandles.get(binding.actionSet), instance)),
                        getPath(pair.getRight())
                    );
                }

                //TODO make this also changeable?
                XrActionSet actionSet = new XrActionSet(actionSetHandles.get(VRInputActionSet.GLOBAL), instance);
                bindings.get(defaultBindings.length).set(
                    new XrAction(this.grip[0], actionSet),
                    getPath("/user/hand/left/input/grip/pose")
                );
                bindings.get(defaultBindings.length + 1).set(
                    new XrAction(this.grip[1], actionSet),
                    getPath("/user/hand/right/input/grip/pose")
                );
                bindings.get(defaultBindings.length + 2).set(
                    new XrAction(this.aim[0], actionSet),
                    getPath("/user/hand/left/input/aim/pose")
                );
                bindings.get(defaultBindings.length + 3).set(
                    new XrAction(this.aim[1], actionSet),
                    getPath("/user/hand/right/input/aim/pose")
                );

                XrInteractionProfileSuggestedBinding suggested_binds = XrInteractionProfileSuggestedBinding.calloc(stack);
                suggested_binds.type(XR10.XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING);
                suggested_binds.next(NULL);
                suggested_binds.interactionProfile(getPath(headset));
                suggested_binds.suggestedBindings(bindings);

                int i = XR10.xrSuggestInteractionProfileBindings(instance, suggested_binds);
            }


            XrSessionActionSetsAttachInfo attach_info = XrSessionActionSetsAttachInfo.calloc(stack);
            attach_info.type(XR10.XR_TYPE_SESSION_ACTION_SETS_ATTACH_INFO);
            attach_info.next(NULL);
            attach_info.actionSets(stackPointers(actionSetHandles.values().stream().mapToLong(value -> value).toArray()));

            XR10.xrAttachSessionActionSets(session, attach_info);

            //Controller tracking?
            //XrActionSpaceCreateInfo
            //XR10.xrCreateActionSpace()


        }
    }

    public long getPath(String pathString) {
        return this.paths.computeIfAbsent(pathString, s -> {
            try (MemoryStack ignored = stackPush()) {
                LongBuffer buf = stackCallocLong(1);
                int xrResult = XR10.xrStringToPath(instance, pathString, buf);
                return buf.get();
            }
        });
    }

    private long createAction(String name, String localisedName, String type, XrActionSet actionSet) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            String s = name.split("/")[name.split("/").length -1].toLowerCase();
            XrActionCreateInfo hands = XrActionCreateInfo.calloc(stack);
            hands.actionType(XR10.XR_TYPE_ACTION_CREATE_INFO);
            hands.next(NULL);
            hands.actionName(memUTF8(s));
            switch (type) {
                case "boolean" -> hands.actionType(XR10.XR_ACTION_TYPE_BOOLEAN_INPUT);
                case "vector1" -> hands.actionType(XR10.XR_ACTION_TYPE_FLOAT_INPUT);
                case "vector2" -> hands.actionType(XR10.XR_ACTION_TYPE_VECTOR2F_INPUT);
                case "pose" -> hands.actionType(XR10.XR_ACTION_TYPE_POSE_INPUT);
            }
            hands.countSubactionPaths(0);
            hands.subactionPaths(null);
            hands.localizedActionName(memUTF8(s));
            PointerBuffer buffer = stackCallocPointer(1);
            int i = XR10.xrCreateAction(actionSet, hands, buffer);
            return buffer.get(0);
        }
    }

    private long makeActionSet(XrInstance instance, String name, String localisedName, int priority) {
        try (MemoryStack stack = MemoryStack.stackPush()){
            XrActionSetCreateInfo info = XrActionSetCreateInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_SET_CREATE_INFO);
            info.next(NULL);
            info.actionSetName(memUTF8(localisedName.toLowerCase()));
            info.localizedActionSetName(memUTF8(localisedName.toLowerCase()));
            info.priority(priority);
            PointerBuffer buffer = stack.callocPointer(1);
            //Handle error
            int i = XR10.xrCreateActionSet(instance, info, buffer);
            return buffer.get(0);
        }
    }

    static ByteBuffer bufferHeap(int capacity, int sizeof, int type) {
        ByteBuffer b = memCalloc(capacity * sizeof);

        for (int i = 0; i < capacity; i++) {
            b.position(i * sizeof);
            b.putInt(type);
        }
        b.rewind();
        return b;
    }
}
