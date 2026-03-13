package org.vivecraft.client_vr.provider.openxr;

import com.google.common.collect.HashBiMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.provider.control.*;
import org.vivecraft.client_vr.provider.control.BindingProfile;
import org.vivecraft.client_vr.provider.openxr.control.ControllerMapping;
import org.vivecraft.client_vr.provider.openxr.control.XRInputAction;
import org.vivecraft.client_vr.settings.VRSettings;
import oshi.util.tuples.Pair;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MCOpenXR extends MCVR<XRInputAction> {

    private static MCOpenXR OME;
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
    // TODO either move to MCVR, Or make special for OpenXR holding the instance itself.
    private final Map<VRInputActionSet, Long> actionSetHandles = new EnumMap<>(VRInputActionSet.class);
    // TODO Move to MCVR
    private XrActiveActionSet.Buffer activeActionSetsBuffer;
    private boolean isActive;
    private final HashBiMap<String, Long> paths = HashBiMap.create();
    private final long[] grip = new long[2];
    private final long[] aim = new long[2];
    private final XrSpace[] gripSpace = new XrSpace[2];
    private final XrSpace[] aimSpace = new XrSpace[2];
    public static final XrPosef POSE_IDENTITY = XrPosef.calloc().set(
        XrQuaternionf.calloc().set(0, 0, 0, 1),
        XrVector3f.calloc()
    );
    public boolean shouldRender = true;
    public final long[] haptics = new long[2];
    public String systemName;
    private final String[] activeController = new String[2];

    public record ActionBind(VRInputActionSet actionSet, String path) {}

    public Map<ActionBind, Long> mappedBindings = new HashMap<>();

    public MCOpenXR(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh, VivecraftVRMod.INSTANCE);
        OME = this;
        this.hapticScheduler = new OpenXRHapticScheduler();
    }

    @Override
    public String getName() {
        return "OpenXR";
    }

    @Override
    public void destroy() {
        int error;
        // Not sure if we need the action sets one here, as we are shutting down
        for (Long inputActionSet : this.actionSetHandles.values()) {
            error = XR10.xrDestroyActionSet(new XrActionSet(inputActionSet, this.instance));
            logError(error, "xrDestroyActionSet", "");
        }
        if (this.swapchain != null) {
            error = XR10.xrDestroySwapchain(this.swapchain);
            logError(error, "xrDestroySwapchain", "");
        }
        if (this.viewBuffer != null) {
            this.viewBuffer.close();
        }
        if (this.xrAppSpace != null) {
            error = XR10.xrDestroySpace(this.xrAppSpace);
            logError(error, "xrDestroySpace", "xrAppSpace");
        }
        if (this.xrViewSpace != null) {
            error = XR10.xrDestroySpace(this.xrViewSpace);
            logError(error, "xrDestroySpace", "xrViewSpace");
        }
        if (this.session != null) {
            error = XR10.xrDestroySession(this.session);
            logError(error, "xrDestroySession", "");
        }
        if (this.instance != null) {
            error = XR10.xrDestroyInstance(this.instance);
            logError(error, "xrDestroyInstance", "");
        }
        this.eventDataBuffer.close();
    }

    @Override
    public XRInputAction createAction(
        KeyMapping keyMapping, String requirement, ActionType type, VRInputActionSet actionSetOverride)
    {
        return new XRInputAction(keyMapping, requirement, type, actionSetOverride);
    }

    //TODO fix, action origins don't work like that on openXR
    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping keyMapping) {
        if (!this.inputInitialized) {
            return null;
        } else {
            var action = this.getInputAction(keyMapping);
            for (ControllerType controllerType : ControllerType.values()) {
                var handedaction = action.getHandle(this.activeController[controllerType.ordinal()])
                    .get(action.activeAction);
                if (handedaction.hand() != controllerType) {
                    continue;
                }
                return handedaction.hand();
            }
            return null;
//            long path = this.getInputAction(keyMapping).getLastOrigin();
//            String name = getString(path);
//            if (name.contains("right")) {
//                return ControllerType.RIGHT;
//            }
//            return ControllerType.LEFT;
        }
    }

    @Override
    public void handleEvents() {
        Profiler.get().push("events");
        this.pollVREvents();
        Profiler.get().pop();
    }

    @Override
    public void poll(long frameIndex) {
        if (this.initialized) {

            if (!this.dh.vrSettings.seated) {
                Profiler.get().push("controllers");
                Profiler.get().push("gui");

                Profiler.get().pop();
            }
            Profiler.get().popPush("updatePose/Vsync");
            this.updatePose();
            Profiler.get().popPush("processInputs");
            this.processInputs();
            Profiler.get().popPush("hmdSampling");
            this.hmdSampling();
            Profiler.get().pop();
        }
    }

    private void updatePose() {
        if (this.mc == null) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrFrameState frameState = XrFrameState.calloc(stack).type(XR10.XR_TYPE_FRAME_STATE);

            int error = XR10.xrWaitFrame(
                this.session,
                XrFrameWaitInfo.calloc(stack).type(XR10.XR_TYPE_FRAME_WAIT_INFO),
                frameState);
            logError(error, "xrWaitFrame", "");

            this.time = frameState.predictedDisplayTime();
            this.shouldRender = frameState.shouldRender();

            error = XR10.xrBeginFrame(
                this.session,
                XrFrameBeginInfo.calloc(stack).type(XR10.XR_TYPE_FRAME_BEGIN_INFO));
            logError(error, "xrBeginFrame", "");


            XrViewState viewState = XrViewState.calloc(stack).type(XR10.XR_TYPE_VIEW_STATE);
            IntBuffer intBuf = stack.callocInt(1);

            XrViewLocateInfo viewLocateInfo = XrViewLocateInfo.calloc(stack);
            viewLocateInfo.set(XR10.XR_TYPE_VIEW_LOCATE_INFO,
                0,
                XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO,
                frameState.predictedDisplayTime(),
                this.xrAppSpace
            );

            error = XR10.xrLocateViews(this.session, viewLocateInfo, viewState, intBuf, this.viewBuffer);
            logError(error, "xrLocateViews", "");

            XrSpaceLocation space_location = XrSpaceLocation.calloc(stack).type(XR10.XR_TYPE_SPACE_LOCATION);

            // HMD pose
            error = XR10.xrLocateSpace(this.xrViewSpace, this.xrAppSpace, this.time, space_location);
            logError(error, "xrLocateSpace", "xrViewSpace");
            if (error >= 0) {
                OpenXRUtil.openXRPoseToMarix(space_location.pose(), this.hmdPose);
                this.headIsTracking = true;
            } else {
                this.headIsTracking = false;
                this.hmdPose.identity();
                this.hmdPose.m31(1.6F);
            }

            // Eye positions
            OpenXRUtil.openXRPoseToMarix(this.viewBuffer.get(0).pose(), this.hmdPoseLeftEye);
            OpenXRUtil.openXRPoseToMarix(this.viewBuffer.get(1).pose(), this.hmdPoseRightEye);

            if (this.inputInitialized) {
                Profiler.get().push("updateActionState");

                if (this.updateActiveActionSets()) {
                    XrActionsSyncInfo syncInfo = XrActionsSyncInfo.calloc(stack)
                        .type(XR10.XR_TYPE_ACTIONS_SYNC_INFO)
                        .activeActionSets(this.activeActionSetsBuffer);
                    error = XR10.xrSyncActions(this.session, syncInfo);
                    logError(error, "xrSyncActions", "");
                }

                XrInteractionProfileState state = XrInteractionProfileState.calloc(stack);
                state.type(XR10.XR_TYPE_INTERACTION_PROFILE_STATE);
                error = XR10.xrGetCurrentInteractionProfile(this.session, getPath("/user/hand/right"), state);
                logError(error, "xrGetCurrentInteractionProfile", "right");
                this.activeController[RIGHT_CONTROLLER] = getString(state.interactionProfile());
                error = XR10.xrGetCurrentInteractionProfile(this.session, getPath("/user/hand/left"), state);
                logError(error, "xrGetCurrentInteractionProfile", "left");
                this.activeController[LEFT_CONTROLLER] = getString(state.interactionProfile());

                this.inputActions.values().forEach(this::readNewData);

                //TODO Not needed it seems? Poses come from the action space
                XrActionSet actionSet = new XrActionSet(this.actionSetHandles.get(VRInputActionSet.GLOBAL),
                    this.instance);
                this.readPoseData(this.grip[RIGHT_CONTROLLER], actionSet);
                this.readPoseData(this.grip[LEFT_CONTROLLER], actionSet);
                this.readPoseData(this.aim[RIGHT_CONTROLLER], actionSet);
                this.readPoseData(this.aim[LEFT_CONTROLLER], actionSet);

                Profiler.get().pop();

                // reverse
                if (this.dh.vrSettings.reverseHands) {
                    XrSpace temp = this.gripSpace[RIGHT_CONTROLLER];
                    this.gripSpace[RIGHT_CONTROLLER] = this.gripSpace[LEFT_CONTROLLER];
                    this.gripSpace[LEFT_CONTROLLER] = temp;
                    temp = this.aimSpace[RIGHT_CONTROLLER];
                    this.aimSpace[RIGHT_CONTROLLER] = this.aimSpace[LEFT_CONTROLLER];
                    this.aimSpace[LEFT_CONTROLLER] = temp;
                }

                // Controller aim and grip poses
                error = XR10.xrLocateSpace(this.gripSpace[RIGHT_CONTROLLER], this.xrAppSpace, this.time,
                    space_location);
                logError(error, "xrLocateSpace", "gripSpace[0]");
                if (error >= 0) {
                    OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(),
                        this.handRotation[RIGHT_CONTROLLER]);
                }

                error = XR10.xrLocateSpace(this.gripSpace[LEFT_CONTROLLER], this.xrAppSpace, this.time, space_location);
                logError(error, "xrLocateSpace", "gripSpace[1]");
                if (error >= 0) {
                    OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(),
                        this.handRotation[LEFT_CONTROLLER]);
                }

                error = XR10.xrLocateSpace(this.aimSpace[RIGHT_CONTROLLER], this.xrAppSpace, this.time, space_location);
                logError(error, "xrLocateSpace", "aimSpace[0]");
                if (error >= 0) {
                    OpenXRUtil.openXRPoseToMarix(space_location.pose(), this.controllerPose[RIGHT_CONTROLLER]);
                    OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(),
                        this.controllerRotation[RIGHT_CONTROLLER]);
                    this.controllerTracking[RIGHT_CONTROLLER] = true;
                } else {
                    this.controllerTracking[RIGHT_CONTROLLER] = false;
                }

                error = XR10.xrLocateSpace(this.aimSpace[LEFT_CONTROLLER], this.xrAppSpace, this.time, space_location);
                logError(error, "xrLocateSpace", "aimSpace[1]");
                if (error >= 0) {
                    OpenXRUtil.openXRPoseToMarix(space_location.pose(), this.controllerPose[LEFT_CONTROLLER]);
                    OpenXRUtil.openXRPoseToMarix(space_location.pose().orientation(),
                        this.controllerRotation[LEFT_CONTROLLER]);
                    this.controllerTracking[LEFT_CONTROLLER] = true;
                } else {
                    this.controllerTracking[LEFT_CONTROLLER] = false;
                }
            }

            this.updateAim();
        }
    }

    public void readNewData(XRInputAction action) {
        for (var controller : ControllerType.values()) {
            for (int i = 0; i < action.getHandle(this.activeController[controller.ordinal()]).size(); i++) {
                var handedAction = action.getHandle(this.activeController[controller.ordinal()]).get(i);
                if (handedAction.handle() == 0L) {
                    continue;
                }
                if (handedAction.hand() != controller) {
                    continue;
                }
                switch (handedAction.action()) {
                    case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE ->
                        this.readBoolean(action, handedAction.hand(), i);

                    case VEC1 -> this.readFloat(action, handedAction.hand(), i);

                    case VEC2 -> this.readVecData(action, handedAction.hand(), i);
                }
            }
        }
    }

    private void readBoolean(XRInputAction action, ControllerType hand, int index) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            XRInputAction.HandedAction handedAction = action.getHandle(this.activeController[hand.ordinal()])
                .get(index);
            info.action(new XrAction(handedAction.handle(),
                new XrActionSet(this.actionSetHandles.get(action.actionSet), this.instance)));
            info.subactionPath(
                hand == ControllerType.LEFT ? getPath(BOTH_HANDS[0]) : getPath(BOTH_HANDS[1]));
            XrActionStateBoolean state = XrActionStateBoolean.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_BOOLEAN);
            int error = XR10.xrGetActionStateBoolean(this.session, info, state);
            logError(error, "xrGetActionStateBoolean", action.name);

            if (state.changedSinceLastSync()) {
                if (state.currentState()) {
                    action.digitalData.get(index).toggle = !action.digitalData.get(index).toggle;
                    action.digitalData.get(index).doublePress =
                        System.nanoTime() - action.digitalData.get(index).lastChange > 250_000_000L;
                } else {
                    action.digitalData.get(index).longPress =
                        System.nanoTime() - action.digitalData.get(index).lastChange > 500_000_000L;
                }
                action.digitalData.get(index).lastChange = System.nanoTime();
            } else if (state.currentState()) {
                action.digitalData.get(index).hold =
                    System.nanoTime() - action.digitalData.get(index).lastChange > 500_000_000L;
            }

            action.digitalData.get(index).state = state.currentState();
            action.digitalData.get(index).isActive = state.isActive();
            action.digitalData.get(index).isChanged = state.changedSinceLastSync();
            action.digitalData.get(index).activeOrigin = getOrigins(handedAction, action).getFirst();
            action.digitalData.get(index).type = handedAction.action();
            action.digitalData.get(index).hand = handedAction.hand();

            action.analogData.get(index).deltaX =
                state.changedSinceLastSync() ? state.currentState() ? 1.0F : -1.0F : 0.0F;
            action.analogData.get(index).x = state.currentState() ? 1.0f : 0.0f;
            action.analogData.get(index).activeOrigin = getOrigins(handedAction, action).getFirst();
            action.analogData.get(index).isActive = state.isActive();
            action.analogData.get(index).isChanged = state.changedSinceLastSync();
        }
    }

    private void readFloat(XRInputAction action, ControllerType hand, int index) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            XRInputAction.HandedAction handedAction = action.getHandle(this.activeController[hand.ordinal()])
                .get(index);
            info.action(new XrAction(handedAction.handle(),
                new XrActionSet(this.actionSetHandles.get(action.actionSet), this.instance)));
            info.subactionPath(
                hand == ControllerType.LEFT ? getPath(BOTH_HANDS[0]) : getPath(BOTH_HANDS[1]));
            XrActionStateFloat state = XrActionStateFloat.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_FLOAT);
            int error = XR10.xrGetActionStateFloat(this.session, info, state);
            logError(error, "xrGetActionStateFloat", action.name);

            action.analogData.get(index).deltaX = state.currentState() - action.analogData.get(index).x;
            action.analogData.get(index).x = state.currentState();
            action.analogData.get(index).activeOrigin = getOrigins(action).getFirst();
            action.analogData.get(index).isActive = state.isActive();
            action.analogData.get(index).isChanged = state.changedSinceLastSync();

            //Write digital data
            boolean on = Math.abs(state.currentState()) > 0.5F;
            boolean changed =
                Math.abs(action.analogData.get(index).x - action.analogData.get(index).deltaX) > 0.5F != on;
            if (changed) {
                if (on) {
                    action.digitalData.get(index).toggle = !action.digitalData.get(index).toggle;
                    action.digitalData.get(index).doublePress =
                        System.nanoTime() - action.digitalData.get(index).lastChange > 250_000_000L;
                } else {
                    action.digitalData.get(index).longPress =
                        System.nanoTime() - action.digitalData.get(index).lastChange > 500_000_000L;
                }
                action.digitalData.get(index).lastChange = System.nanoTime();
            } else if (on) {
                action.digitalData.get(index).hold =
                    System.nanoTime() - action.digitalData.get(index).lastChange > 500_000_000L;
            }

            action.digitalData.get(index).state = on;
            action.digitalData.get(index).isActive = state.isActive();
            action.digitalData.get(index).isChanged = changed;
            action.digitalData.get(index).activeOrigin = getOrigins(action).getFirst();
            action.digitalData.get(index).type = handedAction.action();
            action.digitalData.get(index).hand = handedAction.hand();
        }
    }

    private void readVecData(XRInputAction action, ControllerType hand, int index) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            XRInputAction.HandedAction handedAction = action.getHandle(this.activeController[hand.ordinal()])
                .get(index);
            info.action(new XrAction(handedAction.handle(),
                new XrActionSet(this.actionSetHandles.get(action.actionSet), this.instance)));
            info.subactionPath(
                hand == ControllerType.LEFT ? getPath(BOTH_HANDS[0]) : getPath(BOTH_HANDS[1]));
            XrActionStateVector2f state = XrActionStateVector2f.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_VECTOR2F);
            int error = XR10.xrGetActionStateVector2f(this.session, info, state);
            logError(error, "xrGetActionStateVector2f", action.name);

            action.analogData.get(index).deltaX = state.currentState().x() - action.analogData.get(index).x;
            action.analogData.get(index).deltaY = state.currentState().y() - action.analogData.get(index).y;
            action.analogData.get(index).x = state.currentState().x();
            action.analogData.get(index).y = state.currentState().y();
            action.analogData.get(index).activeOrigin = getOrigins(action).getFirst();
            action.analogData.get(index).isActive = state.isActive();
            action.analogData.get(index).isChanged = state.changedSinceLastSync();

            //Write digital data
            boolean on = Math.abs(state.currentState().x()) > 0.5F || Math.abs(state.currentState().y()) > 0.5F;
            boolean changed = Math.abs(action.analogData.get(index).x - action.analogData.get(index).deltaX) > 0.5F !=
                Math.abs(action.analogData.get(index).x) > 0.5F ||
                Math.abs(action.analogData.get(index).y - action.analogData.get(index).deltaY) > 0.5F !=
                    Math.abs(action.analogData.get(index).y) > 0.5F;
            if (changed) {
                if (on) {
                    action.digitalData.get(index).toggle = !action.digitalData.get(index).toggle;
                    action.digitalData.get(index).doublePress =
                        System.nanoTime() - action.digitalData.get(index).lastChange > 250_000_000L;
                } else {
                    action.digitalData.get(index).longPress =
                        System.nanoTime() - action.digitalData.get(index).lastChange > 500_000_000L;
                }
                action.digitalData.get(index).lastChange = System.nanoTime();
            } else if (on) {
                action.digitalData.get(index).hold =
                    System.nanoTime() - action.digitalData.get(index).lastChange > 500_000_000L;
            }

            action.digitalData.get(index).state = on;
            action.digitalData.get(index).isActive = state.isActive();
            action.digitalData.get(index).isChanged = changed;
            action.digitalData.get(index).activeOrigin = getOrigins(action).getFirst();
            action.digitalData.get(index).type = handedAction.action();
            action.digitalData.get(index).hand = handedAction.hand();
        }
    }

    private void readPoseData(Long action, XrActionSet set) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrActionStateGetInfo info = XrActionStateGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);
            info.action(new XrAction(action, set));
            XrActionStatePose state = XrActionStatePose.calloc(stack).type(XR10.XR_TYPE_ACTION_STATE_POSE);
            int error = XR10.xrGetActionStatePose(this.session, info, state);
            logError(error, "xrGetActionStatePose", "");
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

        if (KeyboardHandler.SHOWING || RadialHandler.isShowing()) {
            arraylist.add(VRInputActionSet.KEYBOARD);
        }

        if (this.activeActionSetsBuffer == null) {
            this.activeActionSetsBuffer = XrActiveActionSet.calloc(arraylist.size());
        } else if (this.activeActionSetsBuffer.capacity() != arraylist.size()) {
            this.activeActionSetsBuffer.close();
            this.activeActionSetsBuffer = XrActiveActionSet.calloc(arraylist.size());
        }

        for (int i = 0; i < arraylist.size(); ++i) {
            VRInputActionSet vrinputactionset = arraylist.get(i);
            this.activeActionSetsBuffer.get(i)
                .set(new XrActionSet(this.getActionSetHandle(vrinputactionset), this.instance), NULL);
        }

        return !arraylist.isEmpty();
    }

    long getActionSetHandle(VRInputActionSet actionSet) {
        return this.actionSetHandles.get(actionSet);
    }

    private void pollVREvents() {
        while (true) {
            this.eventDataBuffer.clear();
            this.eventDataBuffer.type(XR10.XR_TYPE_EVENT_DATA_BUFFER);
            int error = XR10.xrPollEvent(this.instance, this.eventDataBuffer);
            logError(error, "xrPollEvent", "");
            if (error != XR10.XR_SUCCESS) {
                break;
            }
            XrEventDataBaseHeader event = XrEventDataBaseHeader.create(this.eventDataBuffer.address());

            switch (event.type()) {
                case XR10.XR_TYPE_EVENT_DATA_INSTANCE_LOSS_PENDING -> {
                    XrEventDataInstanceLossPending instanceLossPending = XrEventDataInstanceLossPending.create(
                        event.address());
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
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    XrSessionBeginInfo sessionBeginInfo = XrSessionBeginInfo.calloc(stack);
                    sessionBeginInfo.type(XR10.XR_TYPE_SESSION_BEGIN_INFO);
                    sessionBeginInfo.next(NULL);
                    sessionBeginInfo.primaryViewConfigurationType(XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO);

                    int error = XR10.xrBeginSession(this.session, sessionBeginInfo);
                    logError(error, "xrBeginSession", "XR_SESSION_STATE_READY");
                }
                this.isActive = true;
                break;
            }
            case XR10.XR_SESSION_STATE_STOPPING: {
                this.isActive = false;
                int error = XR10.xrEndSession(this.session);
                logError(error, "xrEndSession", "XR_SESSION_STATE_STOPPING");

                if (ClientDataHolderVR.getInstance().vrSettings.closeWithRuntime) {
                    VRSettings.LOGGER.info("Vivecraft: OpenXR stopped, closing the game with it");
                    this.mc.stop();
                } else {
                    VRSettings.LOGGER.info("Vivecraft: OpenXR stopped, disabling VR");
                    VRState.VR_ENABLED = !VRState.VR_ENABLED;
                    ClientDataHolderVR.getInstance().vrSettings.vrEnabled = VRState.VR_ENABLED;
                    ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                }
            }
            case XR10.XR_SESSION_STATE_VISIBLE, XR10.XR_SESSION_STATE_FOCUSED: {
                this.isActive = true;
                break;
            }
            case XR10.XR_SESSION_STATE_EXITING, XR10.XR_SESSION_STATE_IDLE, XR10.XR_SESSION_STATE_SYNCHRONIZED: {
                this.isActive = false;
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
            int error = XR10.xrGetReferenceSpaceBoundsRect(this.session, XR10.XR_REFERENCE_SPACE_TYPE_STAGE, vec);
            logError(error, "xrGetReferenceSpaceBoundsRect", "");
            return new Vector2f(vec.width(), vec.height());
        }
    }

    @Override
    public void refreshControllerTransforms() {
        // TODO controller type overrides
    }

    @Override
    public boolean init() {
        if (this.initialized) {
            return true;
        } else if (this.tried) {
            return this.initialized;
        } else {
            this.tried = true;
            this.mc = Minecraft.getInstance();
            try {
                this.initializeOpenXRInstance();
                this.initializeOpenXRSession();
                this.initializeOpenXRSpace();
                this.initializeOpenXRSwapChain();
                this.initInputAndApplication();
            } catch (Exception e) {
                VRSettings.LOGGER.error("Vivecraft: OpenXR init failed", e);
                this.initSuccess = false;
                this.initStatus = e.getLocalizedMessage();
                return false;
            }

            // TODO Seated when no controllers

            VRSettings.LOGGER.info("Vivecraft: OpenXR initialized & VR connected.");
            this.deviceVelocity = new Vector3f[64];

            for (int i = 0; i < this.poseMatrices.length; ++i) {
                this.poseMatrices[i] = new Matrix4f();
                this.deviceVelocity[i] = new Vector3f();
            }

            this.initialized = true;
            return true;
        }
    }

    private void initializeOpenXRInstance() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.device.initOpenXRLoader(stack);

            // Check extensions
            IntBuffer numExtensions = stack.callocInt(1);
            int error = XR10.xrEnumerateInstanceExtensionProperties((ByteBuffer) null, numExtensions, null);
            logError(error, "xrEnumerateInstanceExtensionProperties", "get count");

            XrExtensionProperties.Buffer properties = new XrExtensionProperties.Buffer(
                bufferStack(numExtensions.get(0), XrExtensionProperties.SIZEOF, XR10.XR_TYPE_EXTENSION_PROPERTIES)
            );

            // Load extensions
            error = XR10.xrEnumerateInstanceExtensionProperties((ByteBuffer) null, numExtensions, properties);
            logError(error, "xrEnumerateInstanceExtensionProperties", "get extensions");

            // get needed extensions
            String graphicsExtension = this.device.getGraphicsExtension();
            boolean missingGraphics = true;
            PointerBuffer extensions = stack.callocPointer(5);
            while (properties.hasRemaining()) {
                XrExtensionProperties prop = properties.get();
                String extensionName = prop.extensionNameString();
                if (extensionName.equals(graphicsExtension)) {
                    missingGraphics = false;
                    extensions.put(memAddress(stackUTF8(graphicsExtension)));
                }
                if (extensionName.equals(
                    EXTHPMixedRealityController.XR_EXT_HP_MIXED_REALITY_CONTROLLER_EXTENSION_NAME))
                {
                    extensions.put(memAddress(
                        stackUTF8(EXTHPMixedRealityController.XR_EXT_HP_MIXED_REALITY_CONTROLLER_EXTENSION_NAME)));
                }
                if (extensionName.equals(
                    HTCViveCosmosControllerInteraction.XR_HTC_VIVE_COSMOS_CONTROLLER_INTERACTION_EXTENSION_NAME))
                {
                    extensions.put(memAddress(stackUTF8(
                        HTCViveCosmosControllerInteraction.XR_HTC_VIVE_COSMOS_CONTROLLER_INTERACTION_EXTENSION_NAME)));
                }
                if (extensionName.equals(
                    BDControllerInteraction.XR_BD_CONTROLLER_INTERACTION_EXTENSION_NAME))
                {
                    extensions.put(memAddress(stackUTF8(
                        BDControllerInteraction.XR_BD_CONTROLLER_INTERACTION_EXTENSION_NAME)));
                }
                if (extensionName.equals(
                    FBDisplayRefreshRate.XR_FB_DISPLAY_REFRESH_RATE_EXTENSION_NAME))
                {
                    extensions.put(memAddress(stackUTF8(
                        FBDisplayRefreshRate.XR_FB_DISPLAY_REFRESH_RATE_EXTENSION_NAME)));
                }
            }

            if (missingGraphics) {
                throw new RuntimeException("OpenXR runtime is missing a supported graphics extension.");
            }

            // Create APP info
            XrApplicationInfo applicationInfo = XrApplicationInfo.calloc(stack);
            applicationInfo.apiVersion(XR10.XR_MAKE_VERSION(1, 0, 40));
            applicationInfo.applicationName(stack.UTF8("Vivecraft"));
            applicationInfo.applicationVersion(1);

            // Create instance info
            XrInstanceCreateInfo createInfo = XrInstanceCreateInfo.calloc(stack);
            createInfo.type(XR10.XR_TYPE_INSTANCE_CREATE_INFO);
            createInfo.next(this.device.getPlatformInfo(stack));
            createInfo.createFlags(0);
            createInfo.applicationInfo(applicationInfo);
            createInfo.enabledApiLayerNames(null);
            createInfo.enabledExtensionNames(extensions.flip());

            // Create XR instance
            PointerBuffer instancePtr = stack.callocPointer(1);
            int xrResult = XR10.xrCreateInstance(createInfo, instancePtr);
            if (xrResult == XR10.XR_ERROR_RUNTIME_FAILURE) {
                throw new RuntimeException("Failed to create xrInstance, are you sure your headset is plugged in?");
            } else if (xrResult == XR10.XR_ERROR_INSTANCE_LOST) {
                throw new RuntimeException("Failed to create xrInstance due to runtime updating");
            } else if (xrResult < 0) {
                throw new RuntimeException("XR method returned " + xrResult);
            }
            this.instance = new XrInstance(instancePtr.get(0), createInfo);

            this.poseMatrices = new Matrix4f[64];

            for (int i = 0; i < this.poseMatrices.length; ++i) {
                this.poseMatrices[i] = new Matrix4f();
            }

            this.initSuccess = true;
        }
    }

    public static MCOpenXR get() {
        return OME;
    }

    private void initializeOpenXRSession() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Create system
            XrSystemGetInfo system = XrSystemGetInfo.calloc(stack);
            system.type(XR10.XR_TYPE_SYSTEM_GET_INFO);
            system.next(NULL);
            system.formFactor(XR10.XR_FORM_FACTOR_HEAD_MOUNTED_DISPLAY);

            LongBuffer longBuffer = stack.callocLong(1);
            int error = XR10.xrGetSystem(this.instance, system, longBuffer);
            logError(error, "xrGetSystem", "");
            this.systemID = longBuffer.get(0);

            if (this.systemID == 0) {
                throw new RuntimeException("No compatible headset detected");
            }

            XrSystemProperties systemProperties = XrSystemProperties.calloc(stack).type(XR10.XR_TYPE_SYSTEM_PROPERTIES);
            error = XR10.xrGetSystemProperties(this.instance, this.systemID, systemProperties);
            MCOpenXR.get().logError(error, "xrGetSystemProperties", "");
            XrSystemTrackingProperties trackingProperties = systemProperties.trackingProperties();
            XrSystemGraphicsProperties graphicsProperties = systemProperties.graphicsProperties();

            MCOpenXR.get().systemName = memUTF8(memAddress(systemProperties.systemName()));
            int vendor = systemProperties.vendorId();
            boolean orientationTracking = trackingProperties.orientationTracking();
            boolean positionTracking = trackingProperties.positionTracking();
            int maxWidth = graphicsProperties.maxSwapchainImageWidth();
            int maxHeight = graphicsProperties.maxSwapchainImageHeight();
            int maxLayerCount = graphicsProperties.maxLayerCount();

            VRSettings.LOGGER.info("Found device with id:  {}", this.systemID);
            VRSettings.LOGGER.info("Headset Name: {}, Vendor: {}", MCOpenXR.get().systemName, vendor);
            VRSettings.LOGGER.info("Headset Orientation Tracking: {}, Position Tracking: {}", orientationTracking,
                positionTracking);
            VRSettings.LOGGER.info("Headset Max Width: {}, Max Height: {}, Max Layer Count: {}", maxWidth, maxHeight,
                maxLayerCount);

            // Create session
            XrSessionCreateInfo info = XrSessionCreateInfo.calloc(stack);
            info.type(XR10.XR_TYPE_SESSION_CREATE_INFO);
            info.next(this.device.checkGraphics(stack, this.instance, this.systemID).address());
            info.createFlags(0);
            info.systemId(this.systemID);

            PointerBuffer sessionPtr = stack.callocPointer(1);
            error = XR10.xrCreateSession(this.instance, info, sessionPtr);
            logError(error, "xrCreateSession", "");

            this.session = new XrSession(sessionPtr.get(0), this.instance);
            while (!this.isActive) {pollVREvents();}
        }
    }

    private void initializeOpenXRSpace() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
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
            int error = XR10.xrCreateReferenceSpace(this.session, referenceSpaceCreateInfo, pp);
            this.xrAppSpace = new XrSpace(pp.get(0), this.session);
            logError(error, "xrCreateReferenceSpace", "XR_REFERENCE_SPACE_TYPE_STAGE");

            referenceSpaceCreateInfo.referenceSpaceType(XR10.XR_REFERENCE_SPACE_TYPE_VIEW);
            error = XR10.xrCreateReferenceSpace(this.session, referenceSpaceCreateInfo, pp);
            logError(error, "xrCreateReferenceSpace", "XR_REFERENCE_SPACE_TYPE_VIEW");
            this.xrViewSpace = new XrSpace(pp.get(0), this.session);
        }
    }

    private void initializeOpenXRSwapChain() {
        try (MemoryStack stack = stackPush()) {
            // Check amount of views
            IntBuffer intBuf = stack.callocInt(1);
            int error = XR10.xrEnumerateViewConfigurationViews(this.instance, this.systemID,
                XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO, intBuf, null);
            logError(error, "xrEnumerateViewConfigurationViews", "get count");

            // Get all views
            ByteBuffer viewConfBuffer = bufferStack(intBuf.get(0), XrViewConfigurationView.SIZEOF,
                XR10.XR_TYPE_VIEW_CONFIGURATION_VIEW);
            XrViewConfigurationView.Buffer views = new XrViewConfigurationView.Buffer(viewConfBuffer);
            error = XR10.xrEnumerateViewConfigurationViews(this.instance, this.systemID,
                XR10.XR_VIEW_CONFIGURATION_TYPE_PRIMARY_STEREO, intBuf, views);
            logError(error, "xrEnumerateViewConfigurationViews", "get views");
            int viewCountNumber = intBuf.get(0);

            this.viewBuffer = new XrView.Buffer(
                bufferHeap(viewCountNumber, XrView.SIZEOF, XR10.XR_TYPE_VIEW)
            );
            // Check swapchain formats
            error = XR10.xrEnumerateSwapchainFormats(this.session, intBuf, null);
            logError(error, "xrEnumerateSwapchainFormats", "get count");

            // Get swapchain formats
            LongBuffer swapchainFormats = stack.callocLong(intBuf.get(0));
            error = XR10.xrEnumerateSwapchainFormats(this.session, intBuf, swapchainFormats);
            logError(error, "xrEnumerateSwapchainFormats", "get formats");

            long[] desiredSwapchainFormats = {
                // SRGB formats
                GL21.GL_SRGB8_ALPHA8,
                GL21.GL_SRGB8,
                // others
                GL11.GL_RGB10_A2,
                GL30.GL_RGBA16F,
                GL30.GL_RGB16F,

                // The two below should only be used as a fallback, as they are linear color formats without enough bits for color
                // depth, thus leading to banding.
                GL11.GL_RGBA8,
                GL31.GL_RGBA8_SNORM,
            };

            // Choose format
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

            // Make swapchain
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
            error = XR10.xrCreateSwapchain(this.session, swapchainCreateInfo, handlePointer);
            logError(error, "xrCreateSwapchain", "format: " + chosenFormat);
            this.swapchain = new XrSwapchain(handlePointer.get(0), this.session);
            this.width = swapchainCreateInfo.width();
            this.height = swapchainCreateInfo.height();
        }
    }

    private void initDisplayRefreshRate() {
        if (this.session.getCapabilities().XR_FB_display_refresh_rate) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer refreshRateCount = stack.callocInt(1);
                FBDisplayRefreshRate.xrEnumerateDisplayRefreshRatesFB(this.session, refreshRateCount, null);
                FloatBuffer refreshRateBuffer = stack.callocFloat(refreshRateCount.get(0));
                FBDisplayRefreshRate.xrEnumerateDisplayRefreshRatesFB(this.session, refreshRateCount,
                    refreshRateBuffer);
                refreshRateBuffer.rewind();
                FBDisplayRefreshRate.xrRequestDisplayRefreshRateFB(this.session,
                    refreshRateBuffer.get(refreshRateCount.get(0) - 1));
            }
        }
    }

    /**
     * Creates an array of XrStructs with their types preset to {@code type}
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

    private void initInputAndApplication() {
        this.populateInputActions();

        //this.generateActionManifest();
        //this.loadActionManifest();
        this.loadActionHandles();
        this.loadBindings();
        //this.installApplicationManifest(false);
        this.inputInitialized = true;
        this.initDisplayRefreshRate();
    }

    @Override
    public Matrix4f getControllerComponentTransform(int controllerIndex, String componentName) {
        return new Matrix4f();
    }

    @Override
    public boolean hasCameraTracker() {
        return false;
    }

    public String getCurrentInteractionProfile() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrInteractionProfileState state = XrInteractionProfileState.calloc(stack);
            state.type(XR10.XR_TYPE_INTERACTION_PROFILE_STATE);
            int error = XR10.xrGetCurrentInteractionProfile(this.session, getPath("/user/hand/left"), state);
            logError(error, "xrGetCurrentInteractionProfile", "left");
            return getString(state.interactionProfile());
        }
    }

    public List<Long> getOrigins(XRInputAction.HandedAction handedAction, XRInputAction action) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrBoundSourcesForActionEnumerateInfo info = XrBoundSourcesForActionEnumerateInfo.calloc(stack);
            info.type(XR10.XR_TYPE_BOUND_SOURCES_FOR_ACTION_ENUMERATE_INFO);
            info.next(NULL);
            info.action(new XrAction(handedAction.handle(),
                new XrActionSet(this.actionSetHandles.get(action.actionSet), this.instance)));
            IntBuffer buf = stack.callocInt(1);
            int error = XR10.xrEnumerateBoundSourcesForAction(this.session, info, buf, null);
            logError(error, "xrEnumerateBoundSourcesForAction", action.name);

            int size = buf.get();
            if (size <= 0) {
                return List.of(0L);
            }

            buf = stack.callocInt(size);
            LongBuffer longbuf = stack.callocLong(size);
            error = XR10.xrEnumerateBoundSourcesForAction(this.session, info, buf, longbuf);
            logError(error, "xrEnumerateBoundSourcesForAction", action.name);
            longbuf.rewind();
            long[] array = new long[longbuf.remaining()];
            longbuf.get(array);
            return Arrays.stream(array).boxed().toList();
        }
    }

    @Override
    public <I extends InputAction> List<Long> getOrigins(I action) {
        if (action instanceof XRInputAction xrAction) {
            var handedAction = xrAction.getActiveAction(this.activeController);
            if (handedAction != null) {
                return getOrigins(handedAction, xrAction);
            }
        }
        return List.of(0L);
    }

    @Override
    public String getOriginName(long origin) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrInputSourceLocalizedNameGetInfo info = XrInputSourceLocalizedNameGetInfo.calloc(stack);
            info.type(XR10.XR_TYPE_INPUT_SOURCE_LOCALIZED_NAME_GET_INFO);
            info.next(0);
            info.sourcePath(origin);
            info.whichComponents(XR10.XR_INPUT_SOURCE_LOCALIZED_NAME_COMPONENT_BIT);

            IntBuffer buf = stack.callocInt(1);
            int error = XR10.xrGetInputSourceLocalizedName(this.session, info, buf, null);
            logError(error, "xrGetInputSourceLocalizedName", "get length");

            int size = buf.get();
            if (size <= 0) {
                return "";
            }

            buf = stack.callocInt(size);
            ByteBuffer byteBuffer = stack.calloc(size);
            error = XR10.xrGetInputSourceLocalizedName(this.session, info, buf, byteBuffer);
            logError(error, "xrGetInputSourceLocalizedName", "get String");
            return MemoryUtil.memUTF8(MemoryUtil.memAddress(buf));
        }
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new OpenXRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public ControllerType getOriginControllerType(long inputValueHandle) {
        if (inputValueHandle == this.aim[RIGHT_CONTROLLER]) {
            return ControllerType.RIGHT;
        }
        return ControllerType.LEFT;
    }

    @Override
    public float getIPD() {
        return this.getEyePosition(RenderPass.RIGHT).x - this.getEyePosition(RenderPass.LEFT).x;
    }

    @Override
    public String getRuntimeName() {
        return "OpenXR";
    }

    private static final String[] BOTH_HANDS = new String[]{"/user/hand/left", "/user/hand/right"};

    //TODO Collect and register all actions
    private void loadActionHandles() {
        for (VRInputActionSet vrinputactionset : VRInputActionSet.values()) {
            long actionSet = makeActionSet(this.instance, vrinputactionset.name, vrinputactionset.localizedName, 0);
            this.actionSetHandles.put(vrinputactionset, actionSet);

            for (String headset : BindingProfile.supportedHeadsets()) {
                for (var binding : ControllerMapping.getMapping(headset).entrySet()) {
                    long action = createAction(
                        (binding.getKey() + "." + headset.replace("/interaction_profiles/", "")).replace("/", "."),
                        binding.getKey(), binding.getValue(),
                        new XrActionSet(actionSet, this.instance),
                        binding.getKey().contains("left") ? BOTH_HANDS[0] : BOTH_HANDS[1]);
                    this.mappedBindings.put(new ActionBind(vrinputactionset, binding.getKey()), action);
                }
            }
        }

        setupControllers();

        XrActionSet actionSet = new XrActionSet(this.actionSetHandles.get(VRInputActionSet.GLOBAL), this.instance);
        this.haptics[RIGHT_CONTROLLER] = createAction("righthaptic",
            "/actions/global/out/righthaptic", ActionType.HAPTIC, actionSet, BOTH_HANDS[1]);
        this.haptics[LEFT_CONTROLLER] = createAction("lefthaptic", "/actions/global/out/lefthaptic",
            ActionType.HAPTIC, actionSet, BOTH_HANDS[0]);
    }

    private void setupControllers() {
        XrActionSet actionSet = new XrActionSet(this.actionSetHandles.get(VRInputActionSet.GLOBAL), this.instance);
        this.grip[RIGHT_CONTROLLER] = createAction("righthand", "/actions/global/in/righthand",
            ActionType.POSE, actionSet, BOTH_HANDS[1]);
        this.grip[LEFT_CONTROLLER] = createAction("lefthand", "/actions/global/in/lefthand", ActionType.POSE,
            actionSet, BOTH_HANDS[0]);
        this.aim[RIGHT_CONTROLLER] = createAction("righthandaim", "/actions/global/in/righthandaim",
            ActionType.POSE, actionSet, BOTH_HANDS[1]);
        this.aim[LEFT_CONTROLLER] = createAction("lefthandaim", "/actions/global/in/lefthandaim",
            ActionType.POSE, actionSet, BOTH_HANDS[0]);
    }

    private void loadBindings() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int error;
            BindingProfile bindingProfile;
            if (ClientDataHolderVR.getInstance().vrSettings.currentBindingProfile != null) {
                bindingProfile = BindingProfile.getCurrentProfile();
                if (bindingProfile == null) {
                    VRSettings.LOGGER.warn("Custom profile '{}' not found, loading defaults", ClientDataHolderVR.getInstance().vrSettings.currentBindingProfile);
                }
            } else {
                bindingProfile = null;
            }

            List<BindingProfile> profilesToLoad = new ArrayList<>();
            if (bindingProfile != null) {
                profilesToLoad.add(bindingProfile);
                VRSettings.LOGGER.info("Loading custom profile: {}", bindingProfile.name());
            } else {
                for (String headset : BindingProfile.supportedHeadsets()) {
                    BindingProfile profile = BindingProfile.getDefaultBinding(headset);
                    if (profile != null) {
                        profilesToLoad.add(profile);
                        VRSettings.LOGGER.info("Loading default profile for {}", headset);
                    } else {
                        VRSettings.LOGGER.warn("Failed to load default profile for {}", headset);
                    }
                }
            }

            for (BindingProfile profile : profilesToLoad) {
                if (profile.controller_paths() == null) continue;
                String headsetPath = profile.controller_paths().openxr();
                VRSettings.LOGGER.info("Loading interaction profile: {}", profile.name());
                for (Map.Entry<String, ActionSet> set : profile.sets().entrySet()) {
                    List<Pair<XrAction, Long>> bindingList = new ArrayList<>();
                    List<Source> sources = set.getValue().sources();
                    for (Source source : sources) {
                        for (Action action : source.inputs()) {
                            XRInputAction inputAction = this.getInputActionByName("/actions/" + set.getKey() + "/in/" + action.action());
                            if (inputAction == null) {
                                VRSettings.LOGGER.warn("Input action '{}' not found", "/actions/" + set.getKey() + "/in/" + action.action());
                                continue;
                            }
                            long handle = this.mappedBindings.get(new ActionBind(inputAction.actionSet, source.path()));
                            ActionType buttonType = ControllerMapping.getMapping(headsetPath).get(source.path());

                            inputAction.addHandle(
                                headsetPath,
                                handle,
                                source.path().contains("/left/") ? ControllerType.LEFT : ControllerType.RIGHT,
                                buttonType
                            );
                            inputAction.setType(action.type());

                            if (inputAction.getHandle(headsetPath).isEmpty() || handle == 0L) {
                                VRSettings.LOGGER.error("Handle for '{}'/'{}' is null", "/actions/" + set.getKey() + "/in/" + action.action(), source.path());
                                continue;
                            }

                            // TODO support multiple bindings per action
                            bindingList.add(new Pair<>(
                                new XrAction(handle, new XrActionSet(this.actionSetHandles.get(inputAction.actionSet), this.instance)),
                                getPath(source.path())
                            ));

                            VRSettings.LOGGER.info("Mapped action '{}' to set '{}'", "/actions/" + set.getKey() + "/in/" + action.action(), source.path());
                        }
                    }

                    XrActionSet actionSet = new XrActionSet(this.actionSetHandles.get(VRInputActionSet.getByName("/actions/" + set.getKey())), this.instance);
                    String[] hands = {"/user/hand/right", "/user/hand/left"};
                    long[] poses = {this.grip[RIGHT_CONTROLLER], this.grip[LEFT_CONTROLLER], this.aim[RIGHT_CONTROLLER], this.aim[LEFT_CONTROLLER]};
                    long[] haptics = {this.haptics[RIGHT_CONTROLLER], this.haptics[LEFT_CONTROLLER]};
                    String[] posePaths = {"/input/grip/pose", "/input/grip/pose", "/input/aim/pose", "/input/aim/pose"};
                    String[] hapticPaths = {"/output/haptic", "/output/haptic"};

                    for (int j = 0; j < poses.length; j++) {
                        bindingList.add(new Pair<>(new XrAction(poses[j], actionSet), getPath(hands[j % 2] + posePaths[j])));
                    }

                    for (int j = 0; j < haptics.length; j++) {
                        bindingList.add(new Pair<>(new XrAction(haptics[j], actionSet), getPath(hands[j] + hapticPaths[j])));
                    }

                    XrActionSuggestedBinding.Buffer bindings = XrActionSuggestedBinding.calloc(bindingList.size(), stack);
                    for (int i = 0; i < bindingList.size(); i++) {
                        Pair<XrAction, Long> binding = bindingList.get(i);
                        bindings.get(i).set(binding.getA(), binding.getB());
                    }

                    XrInteractionProfileSuggestedBinding suggested_binds = XrInteractionProfileSuggestedBinding.calloc(stack);
                    suggested_binds.type(XR10.XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING);
                    suggested_binds.next(NULL);
                    suggested_binds.interactionProfile(getPath(headsetPath));
                    suggested_binds.suggestedBindings(bindings);

                    error = XR10.xrSuggestInteractionProfileBindings(this.instance, suggested_binds);
                    logError(error, "xrSuggestInteractionProfileBindings", profile.name());
                }
            }

            VRSettings.LOGGER.info("Using interaction profile: {}", getCurrentInteractionProfile());
            XrSessionActionSetsAttachInfo attach_info = XrSessionActionSetsAttachInfo.calloc(stack);
            attach_info.type(XR10.XR_TYPE_SESSION_ACTION_SETS_ATTACH_INFO);
            attach_info.next(NULL);
            attach_info.actionSets(stackPointers(this.actionSetHandles.values().stream().mapToLong(value -> value).toArray()));

            error = XR10.xrAttachSessionActionSets(this.session, attach_info);
            logError(error, "xrAttachSessionActionSets", "");

            XrActionSet globalActionSet = new XrActionSet(this.actionSetHandles.get(VRInputActionSet.GLOBAL), this.instance);
            XrActionSpaceCreateInfo actionSpace = XrActionSpaceCreateInfo.calloc(stack);
            actionSpace.type(XR10.XR_TYPE_ACTION_SPACE_CREATE_INFO);
            actionSpace.next(NULL);
            PointerBuffer pp = stackCallocPointer(1);

            long[] allPoses = {this.grip[RIGHT_CONTROLLER], this.grip[LEFT_CONTROLLER], this.aim[RIGHT_CONTROLLER], this.aim[LEFT_CONTROLLER]};
            for (int i = 0; i < allPoses.length; i++) {
                String hand = (i % 2 == 0) ? "/user/hand/right" : "/user/hand/left";
                actionSpace.action(new XrAction(allPoses[i], globalActionSet));
                actionSpace.subactionPath(getPath(hand));
                actionSpace.poseInActionSpace(POSE_IDENTITY);

                error = XR10.xrCreateActionSpace(this.session, actionSpace, pp);
                logError(error, "xrCreateActionSpace", allPoses[i] == this.grip[RIGHT_CONTROLLER] ? "grip: " + hand : "aim: " + hand);

                if (i == 0) this.gripSpace[RIGHT_CONTROLLER] = new XrSpace(pp.get(0), this.session);
                else if (i == 1) this.gripSpace[LEFT_CONTROLLER] = new XrSpace(pp.get(0), this.session);
                else if (i == 2) this.aimSpace[RIGHT_CONTROLLER] = new XrSpace(pp.get(0), this.session);
                else this.aimSpace[LEFT_CONTROLLER] = new XrSpace(pp.get(0), this.session);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Binding profile file not found: " + e.getMessage(), e);
        }
    }

    public long getPath(String pathString) {
        return this.paths.computeIfAbsent(pathString, s -> {
            try (MemoryStack ignored = stackPush()) {
                LongBuffer buf = stackCallocLong(1);
                int error = XR10.xrStringToPath(this.instance, pathString, buf);
                logError(error, "getPath", pathString);
                return buf.get();
            }
        });
    }

    public String getString(long path) {
        if (path == 0L) { //Quest takes some time to loas what controller is used
            return "";
        }
        return this.paths.inverse().computeIfAbsent(path, l -> {
            try (MemoryStack ignored = stackPush()) {
                IntBuffer size = stackCallocInt(1);
                int error = XR10.xrPathToString(this.instance, l, size, null);
                logError(error, "getString", l.toString());
                int i = size.get(0);
                size.put(0, i);
                ByteBuffer string = stackCalloc(i);
                error = XR10.xrPathToString(this.instance, l, size, string);
                logError(error, "getString", l.toString());
                byte[] data = new byte[i];
                string.get(data);
                return new String(data).trim();
            }
        });
    }

    private long createAction(
        String name, String localisedName, ActionType type, XrActionSet actionSet, String subactionPath)
    {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            String s = name.replace(".user.hand.", "");
            XrActionCreateInfo hands = XrActionCreateInfo.calloc(stack);
            hands.type(XR10.XR_TYPE_ACTION_CREATE_INFO);
            hands.next(NULL);
            hands.actionName(memUTF8(s));
            switch (type) {
                case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE ->
                    hands.actionType(XR10.XR_ACTION_TYPE_BOOLEAN_INPUT);
                case VEC1 -> hands.actionType(XR10.XR_ACTION_TYPE_FLOAT_INPUT);
                case VEC2 -> hands.actionType(XR10.XR_ACTION_TYPE_VECTOR2F_INPUT);
                case POSE -> hands.actionType(XR10.XR_ACTION_TYPE_POSE_INPUT);
                case HAPTIC -> hands.actionType(XR10.XR_ACTION_TYPE_VIBRATION_OUTPUT);
            }
            LongBuffer lb = stackCallocLong(1);
            lb.put(getPath(subactionPath));
            hands.countSubactionPaths(1);
            hands.subactionPaths(lb.rewind());
            hands.localizedActionName(memUTF8(s));
            PointerBuffer buffer = stackCallocPointer(1);

            int error = XR10.xrCreateAction(actionSet, hands, buffer);
            logError(error, "xrCreateAction", "name:", name, "type:", type.name());
            return buffer.get(0);
        }
    }

    private long makeActionSet(XrInstance instance, String name, String localisedName, int priority) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrActionSetCreateInfo info = XrActionSetCreateInfo.calloc(stack);
            info.type(XR10.XR_TYPE_ACTION_SET_CREATE_INFO);
            info.next(NULL);
            info.actionSetName(memUTF8(localisedName.toLowerCase()));
            info.localizedActionSetName(memUTF8(localisedName.toLowerCase()));
            info.priority(priority);
            PointerBuffer buffer = stack.callocPointer(1);

            int error = XR10.xrCreateActionSet(instance, info, buffer);
            logError(error, "makeActionSet", localisedName.toLowerCase());
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

    /**
     * gets the String for the given xrResult
     */
    private String getResultName(int xrResult) {
        String resultString = switch (xrResult) {
            case 1 -> "XR_TIMEOUT_EXPIRED";
            case 3 -> "XR_SESSION_LOSS_PENDING";
            case 4 -> "XR_EVENT_UNAVAILABLE";
            case 7 -> "XR_SPACE_BOUNDS_UNAVAILABLE";
            case 8 -> "XR_SESSION_NOT_FOCUSED";
            case 9 -> "XR_FRAME_DISCARDED";
            case -1 -> "XR_ERROR_VALIDATION_FAILURE";
            case -2 -> "XR_ERROR_RUNTIME_FAILURE";
            case -3 -> "XR_ERROR_OUT_OF_MEMORY";
            case -4 -> "XR_ERROR_API_VERSION_UNSUPPORTED";
            case -6 -> "XR_ERROR_INITIALIZATION_FAILED";
            case -7 -> "XR_ERROR_FUNCTION_UNSUPPORTED";
            case -8 -> "XR_ERROR_FEATURE_UNSUPPORTED";
            case -9 -> "XR_ERROR_EXTENSION_NOT_PRESENT";
            case -10 -> "XR_ERROR_LIMIT_REACHED";
            case -11 -> "XR_ERROR_SIZE_INSUFFICIENT";
            case -12 -> "XR_ERROR_HANDLE_INVALID";
            case -13 -> "XR_ERROR_INSTANCE_LOST";
            case -14 -> "XR_ERROR_SESSION_RUNNING";
            case -16 -> "XR_ERROR_SESSION_NOT_RUNNING";
            case -17 -> "XR_ERROR_SESSION_LOST";
            case -18 -> "XR_ERROR_SYSTEM_INVALID";
            case -19 -> "XR_ERROR_PATH_INVALID";
            case -20 -> "XR_ERROR_PATH_COUNT_EXCEEDED";
            case -21 -> "XR_ERROR_PATH_FORMAT_INVALID";
            case -22 -> "XR_ERROR_PATH_UNSUPPORTED";
            case -23 -> "XR_ERROR_LAYER_INVALID";
            case -24 -> "XR_ERROR_LAYER_LIMIT_EXCEEDED";
            case -25 -> "XR_ERROR_SWAPCHAIN_RECT_INVALID";
            case -26 -> "XR_ERROR_SWAPCHAIN_FORMAT_UNSUPPORTED";
            case -27 -> "XR_ERROR_ACTION_TYPE_MISMATCH";
            case -28 -> "XR_ERROR_SESSION_NOT_READY";
            case -29 -> "XR_ERROR_SESSION_NOT_STOPPING";
            case -30 -> "XR_ERROR_TIME_INVALID";
            case -31 -> "XR_ERROR_REFERENCE_SPACE_UNSUPPORTED";
            case -32 -> "XR_ERROR_FILE_ACCESS_ERROR";
            case -33 -> "XR_ERROR_FILE_CONTENTS_INVALID";
            case -34 -> "XR_ERROR_FORM_FACTOR_UNSUPPORTED";
            case -35 -> "XR_ERROR_FORM_FACTOR_UNAVAILABLE";
            case -36 -> "XR_ERROR_API_LAYER_NOT_PRESENT";
            case -37 -> "XR_ERROR_CALL_ORDER_INVALID";
            case -38 -> "XR_ERROR_GRAPHICS_DEVICE_INVALID";
            case -39 -> "XR_ERROR_POSE_INVALID";
            case -40 -> "XR_ERROR_INDEX_OUT_OF_RANGE";
            case -41 -> "XR_ERROR_VIEW_CONFIGURATION_TYPE_UNSUPPORTED";
            case -42 -> "XR_ERROR_ENVIRONMENT_BLEND_MODE_UNSUPPORTED";
            case -44 -> "XR_ERROR_NAME_DUPLICATED";
            case -45 -> "XR_ERROR_NAME_INVALID";
            case -46 -> "XR_ERROR_ACTIONSET_NOT_ATTACHED";
            case -47 -> "XR_ERROR_ACTIONSETS_ALREADY_ATTACHED";
            case -48 -> "XR_ERROR_LOCALIZED_NAME_DUPLICATED";
            case -49 -> "XR_ERROR_LOCALIZED_NAME_INVALID";
            case -50 -> "XR_ERROR_GRAPHICS_REQUIREMENTS_CALL_MISSING";
            case -51 -> "XR_ERROR_RUNTIME_UNAVAILABLE";
            default -> null;
        };
        if (resultString == null) {
            // ask the runtime for the xrResult name
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer str = stack.calloc(XR10.XR_MAX_RESULT_STRING_SIZE);

                if (XR10.xrResultToString(this.instance, xrResult, str) == XR10.XR_SUCCESS) {
                    resultString = (memUTF8(memAddress(str)));
                } else {
                    resultString = "Unknown Error: " + xrResult;
                }
            }
        }
        return resultString;
    }

    /**
     * logs only errors
     *
     * @param xrResult result to check
     * @param caller   where the xrResult came from
     * @param args     arguments may be helpful in locating the error
     */
    protected void logError(int xrResult, String caller, String... args) {
        if (xrResult < 0) {
            VRSettings.LOGGER.error("{} for {} errored: {}", caller, String.join(" ", args), getResultName(xrResult));
        }
    }

    //TODO remove/rework
    public Map<String, XRInputAction> getBinds() {
        return inputActions;
    }
}
