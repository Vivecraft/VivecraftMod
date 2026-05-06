package org.vivecraft.client_vr.provider.openxr_lwjgl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.openxr.XR10.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Manages OpenXR action sets, actions, and interaction profile bindings.
 * Maps Vivecraft's VRInputAction/VRInputActionSet abstractions to OpenXR's action system.
 */
public class OpenXRInputMapper {

    // Synthetic origin values for controller identification
    public static final long ORIGIN_RIGHT_HAND = 1L;
    public static final long ORIGIN_LEFT_HAND = 2L;

    private final XrInstance instance;
    private final XrSession session;

    // Action sets
    private final Map<VRInputActionSet, XrActionSet> actionSets = new EnumMap<>(VRInputActionSet.class);

    // Per-VRInputAction -> XrAction mapping
    private final Map<String, XrAction> xrActions = new HashMap<>();

    // Pose actions
    private XrAction leftGripPoseAction;
    private XrAction rightGripPoseAction;
    private XrAction leftAimPoseAction;
    private XrAction rightAimPoseAction;

    // Haptic actions
    private XrAction leftHapticAction;
    private XrAction rightHapticAction;

    // Pose spaces
    private XrSpace leftGripSpace;
    private XrSpace rightGripSpace;
    private XrSpace leftAimSpace;
    private XrSpace rightAimSpace;

    // Subaction paths
    private long leftHandPath;
    private long rightHandPath;

    // Reusable list for syncActions() to avoid per-frame allocation
    private final List<XrActionSet> activeSetsCache = new ArrayList<>();

    public OpenXRInputMapper(XrInstance instance, XrSession session) {
        this.instance = instance;
        this.session = session;
    }

    /**
     * Initializes all action sets, actions, pose/haptic actions, and suggests interaction profile bindings.
     *
     * @param inputActions the VRInputAction collection from MCVR
     */
    public void init(Collection<VRInputAction> inputActions) throws Exception {
        try (MemoryStack stack = stackPush()) {
            // Get subaction paths
            this.leftHandPath = getPath("/user/hand/left");
            this.rightHandPath = getPath("/user/hand/right");

            LongBuffer subactionPaths = stack.callocLong(2);
            subactionPaths.put(0, this.leftHandPath);
            subactionPaths.put(1, this.rightHandPath);

            // Create action sets for each VRInputActionSet
            for (VRInputActionSet actionSetEnum : VRInputActionSet.values()) {
                String name = sanitizeActionName(actionSetEnum.name().toLowerCase());
                XrActionSetCreateInfo createInfo = XrActionSetCreateInfo.calloc(stack)
                    .type(XR_TYPE_ACTION_SET_CREATE_INFO)
                    .actionSetName(stack.UTF8(name))
                    .localizedActionSetName(stack.UTF8(actionSetEnum.name()))
                    .priority(0);

                PointerBuffer actionSetPtr = stack.callocPointer(1);
                int result = xrCreateActionSet(this.instance, createInfo, actionSetPtr);
                if (result < 0) {
                    VRSettings.LOGGER.error("Vivecraft: Failed to create OpenXR action set '{}': {}",
                        name, OpenXRUtil.resultToString(result));
                    continue;
                }
                this.actionSets.put(actionSetEnum, new XrActionSet(actionSetPtr.get(0), this.instance));
            }

            // Create actions for each VRInputAction
            for (VRInputAction action : inputActions) {
                XrActionSet actionSet = this.actionSets.get(action.actionSet);
                if (actionSet == null) continue;

                String actionName = sanitizeActionName(extractActionName(action.name));
                int xrType = mapActionType(action.type);
                if (xrType == -1) continue;

                XrActionCreateInfo actionCreateInfo = XrActionCreateInfo.calloc(stack)
                    .type(XR_TYPE_ACTION_CREATE_INFO)
                    .actionName(stack.UTF8(actionName))
                    .actionType(xrType)
                    .localizedActionName(stack.UTF8(actionName));

                if (action.isHanded()) {
                    actionCreateInfo.subactionPaths(subactionPaths);
                }

                PointerBuffer actionPtr = stack.callocPointer(1);
                int result = xrCreateAction(actionSet, actionCreateInfo, actionPtr);
                if (result < 0) {
                    VRSettings.LOGGER.warn("Vivecraft: Failed to create OpenXR action '{}': {}",
                        actionName, OpenXRUtil.resultToString(result));
                    continue;
                }
                this.xrActions.put(action.name, new XrAction(actionPtr.get(0), actionSet));
                VRSettings.LOGGER.debug("Vivecraft: Created OpenXR action: key='{}' xrName='{}' type='{}' set={}",
                    action.name, actionName, action.type, action.actionSet);
            }
            VRSettings.LOGGER.info("Vivecraft: Created {} OpenXR actions total", this.xrActions.size());

            // Create pose actions (grip and aim for each hand)
            XrActionSet globalSet = this.actionSets.get(VRInputActionSet.GLOBAL);
            if (globalSet != null) {
                this.leftGripPoseAction = createAction(globalSet, "left_grip_pose",
                    XR_ACTION_TYPE_POSE_INPUT, stack, null);
                this.rightGripPoseAction = createAction(globalSet, "right_grip_pose",
                    XR_ACTION_TYPE_POSE_INPUT, stack, null);
                this.leftAimPoseAction = createAction(globalSet, "left_aim_pose",
                    XR_ACTION_TYPE_POSE_INPUT, stack, null);
                this.rightAimPoseAction = createAction(globalSet, "right_aim_pose",
                    XR_ACTION_TYPE_POSE_INPUT, stack, null);

                // Create haptic actions
                this.leftHapticAction = createAction(globalSet, "left_haptic",
                    XR_ACTION_TYPE_VIBRATION_OUTPUT, stack, null);
                this.rightHapticAction = createAction(globalSet, "right_haptic",
                    XR_ACTION_TYPE_VIBRATION_OUTPUT, stack, null);
            }

            // Suggest interaction profile bindings for Oculus Touch controllers
            suggestOculusTouchBindings(stack);

            // Suggest KHR simple controller bindings as fallback
            suggestSimpleControllerBindings(stack);

            // Attach all action sets to the session
            attachActionSets(stack);

            // Create pose action spaces
            if (this.leftGripPoseAction != null) {
                this.leftGripSpace = createActionSpace(this.leftGripPoseAction, this.leftHandPath, stack);
                this.rightGripSpace = createActionSpace(this.rightGripPoseAction, this.rightHandPath, stack);
                this.leftAimSpace = createActionSpace(this.leftAimPoseAction, this.leftHandPath, stack);
                this.rightAimSpace = createActionSpace(this.rightAimPoseAction, this.rightHandPath, stack);
            }
        }
    }

    private XrAction createAction(XrActionSet actionSet, String name, int type,
                                   MemoryStack stack, LongBuffer subactionPaths)
    {
        XrActionCreateInfo createInfo = XrActionCreateInfo.calloc(stack)
            .type(XR_TYPE_ACTION_CREATE_INFO)
            .actionName(stack.UTF8(name))
            .actionType(type)
            .localizedActionName(stack.UTF8(name));

        if (subactionPaths != null) {
            createInfo.subactionPaths(subactionPaths);
        }

        PointerBuffer actionPtr = stack.callocPointer(1);
        int result = xrCreateAction(actionSet, createInfo, actionPtr);
        if (result < 0) {
            VRSettings.LOGGER.error("Vivecraft: Failed to create OpenXR action '{}': {}",
                name, OpenXRUtil.resultToString(result));
            return null;
        }
        return new XrAction(actionPtr.get(0), actionSet);
    }

    private XrSpace createActionSpace(XrAction action, long subactionPath, MemoryStack stack) {
        XrActionSpaceCreateInfo spaceCreateInfo = XrActionSpaceCreateInfo.calloc(stack)
            .type(XR_TYPE_ACTION_SPACE_CREATE_INFO)
            .action(action)
            .subactionPath(subactionPath)
            .poseInActionSpace(XrPosef.calloc(stack)
                .orientation(XrQuaternionf.calloc(stack).set(0, 0, 0, 1))
                .position$(XrVector3f.calloc(stack).set(0, 0, 0)));

        PointerBuffer spacePtr = stack.callocPointer(1);
        int result = xrCreateActionSpace(this.session, spaceCreateInfo, spacePtr);
        if (result < 0) {
            VRSettings.LOGGER.error("Vivecraft: Failed to create action space: {}",
                OpenXRUtil.resultToString(result));
            return null;
        }
        return new XrSpace(spacePtr.get(0), this.session);
    }

    private void suggestOculusTouchBindings(MemoryStack stack) {
        long profilePath = getPath("/interaction_profiles/oculus/touch_controller");
        List<XrActionSuggestedBinding> bindings = new ArrayList<>();

        // Pose bindings
        addBinding(bindings, this.leftGripPoseAction, "/user/hand/left/input/grip/pose", stack);
        addBinding(bindings, this.rightGripPoseAction, "/user/hand/right/input/grip/pose", stack);
        addBinding(bindings, this.leftAimPoseAction, "/user/hand/left/input/aim/pose", stack);
        addBinding(bindings, this.rightAimPoseAction, "/user/hand/right/input/aim/pose", stack);

        // Haptic bindings
        addBinding(bindings, this.leftHapticAction, "/user/hand/left/output/haptic", stack);
        addBinding(bindings, this.rightHapticAction, "/user/hand/right/output/haptic", stack);

        // Map game actions to Oculus Touch inputs.
        // Action names MUST match VRInputAction.name format: "{VRInputActionSet.name}/in/{keyMapping.getName()}"
        // The action set in the name depends on getSpecialActionParams() overrides, isModBinding(), and category.

        // === MOVEMENT (vector2 actions for primary movement — these are what processBindings() checks first) ===
        // FreeMoveStrafe (left thumbstick, vector2) - primary movement input
        mapActionBinding(bindings, "/actions/ingame/in/vivecraft.key.freeMoveStrafe",
            "/user/hand/left/input/thumbstick", stack);
        // FreeMoveRotate (right thumbstick, vector2) - snap turn / smooth rotation
        mapActionBinding(bindings, "/actions/ingame/in/vivecraft.key.freeMoveRotate",
            "/user/hand/right/input/thumbstick", stack);

        // Individual movement axes (vector1 fallback — used when freeMoveStrafe is not active)
        mapActionBinding(bindings, "/actions/ingame/in/key.forward", "/user/hand/left/input/thumbstick/y", stack);
        mapActionBinding(bindings, "/actions/ingame/in/key.back", "/user/hand/left/input/thumbstick/y", stack);
        mapActionBinding(bindings, "/actions/ingame/in/key.left", "/user/hand/left/input/thumbstick/x", stack);
        mapActionBinding(bindings, "/actions/ingame/in/key.right", "/user/hand/left/input/thumbstick/x", stack);

        // Rotate left/right (right thumbstick X, vector1) - INGAME set (in userKeyBindingSet → vanillaBindingSet)
        mapActionBinding(bindings, "/actions/ingame/in/vivecraft.key.rotateRight",
            "/user/hand/right/input/thumbstick/x", stack);
        mapActionBinding(bindings, "/actions/ingame/in/vivecraft.key.rotateLeft",
            "/user/hand/right/input/thumbstick/x", stack);

        // === COMBAT / INTERACTION ===
        // Attack (right trigger) - boolean action, runtime converts float>0.5 to true
        mapActionBinding(bindings, "/actions/ingame/in/key.attack", "/user/hand/right/input/trigger/value", stack);
        // Use/place (left trigger)
        mapActionBinding(bindings, "/actions/ingame/in/key.use", "/user/hand/left/input/trigger/value", stack);
        // VR interact (right A button) - CONTEXTUAL set (override in getSpecialActionParams)
        mapActionBinding(bindings, "/actions/contextual/in/vivecraft.key.vrInteract",
            "/user/hand/right/input/a/click", stack);

        // === BUTTONS ===
        // Menu button (left menu/Oculus button) - GLOBAL set (override in getSpecialActionParams)
        mapActionBinding(bindings, "/actions/global/in/vivecraft.key.ingameMenuButton",
            "/user/hand/left/input/menu/click", stack);
        // Jump (right A button) - INGAME set
        mapActionBinding(bindings, "/actions/ingame/in/key.jump", "/user/hand/right/input/a/click", stack);
        // Sneak (right B button) - INGAME set
        mapActionBinding(bindings, "/actions/ingame/in/key.sneak", "/user/hand/right/input/b/click", stack);
        // Inventory (left Y button) - GLOBAL set (override in getSpecialActionParams)
        mapActionBinding(bindings, "/actions/global/in/key.inventory", "/user/hand/left/input/y/click", stack);
        // Radial menu (left X button) - INGAME set (in hiddenKeyBindingSet → vanillaBindingSet)
        mapActionBinding(bindings, "/actions/ingame/in/vivecraft.key.radialMenu",
            "/user/hand/left/input/x/click", stack);
        // Sprint (left thumbstick click) - INGAME set
        mapActionBinding(bindings, "/actions/ingame/in/key.sprint", "/user/hand/left/input/thumbstick/click", stack);

        // === GUI ACTIONS (needed for menu/screen interaction with controllers) ===
        // Left click (right trigger in GUI) — for clicking buttons in menus
        mapActionBinding(bindings, "/actions/gui/in/vivecraft.key.guiLeftClick",
            "/user/hand/right/input/trigger/value", stack);
        // Right click (left trigger in GUI)
        mapActionBinding(bindings, "/actions/gui/in/vivecraft.key.guiRightClick",
            "/user/hand/left/input/trigger/value", stack);
        // GUI scroll (right thumbstick, vector2) — for scrolling in menus
        mapActionBinding(bindings, "/actions/gui/in/vivecraft.key.guiScrollAxis",
            "/user/hand/right/input/thumbstick", stack);

        if (bindings.isEmpty()) return;

        XrActionSuggestedBinding.Buffer bindingBuffer = XrActionSuggestedBinding.calloc(bindings.size(), stack);
        for (int i = 0; i < bindings.size(); i++) {
            bindingBuffer.get(i).set(bindings.get(i));
        }

        XrInteractionProfileSuggestedBinding suggestion = XrInteractionProfileSuggestedBinding.calloc(stack)
            .type(XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING)
            .interactionProfile(profilePath)
            .suggestedBindings(bindingBuffer);

        int result = xrSuggestInteractionProfileBindings(this.instance, suggestion);
        if (result < 0) {
            VRSettings.LOGGER.warn("Vivecraft: Failed to suggest Oculus Touch bindings: {}",
                OpenXRUtil.resultToString(result));
        }
    }

    private void suggestSimpleControllerBindings(MemoryStack stack) {
        long profilePath = getPath("/interaction_profiles/khr/simple_controller");
        List<XrActionSuggestedBinding> bindings = new ArrayList<>();

        // Simple controller only has select, menu, grip/aim pose, and haptic
        addBinding(bindings, this.leftGripPoseAction, "/user/hand/left/input/grip/pose", stack);
        addBinding(bindings, this.rightGripPoseAction, "/user/hand/right/input/grip/pose", stack);
        addBinding(bindings, this.leftAimPoseAction, "/user/hand/left/input/aim/pose", stack);
        addBinding(bindings, this.rightAimPoseAction, "/user/hand/right/input/aim/pose", stack);
        addBinding(bindings, this.leftHapticAction, "/user/hand/left/output/haptic", stack);
        addBinding(bindings, this.rightHapticAction, "/user/hand/right/output/haptic", stack);

        if (bindings.isEmpty()) return;

        XrActionSuggestedBinding.Buffer bindingBuffer = XrActionSuggestedBinding.calloc(bindings.size(), stack);
        for (int i = 0; i < bindings.size(); i++) {
            bindingBuffer.get(i).set(bindings.get(i));
        }

        XrInteractionProfileSuggestedBinding suggestion = XrInteractionProfileSuggestedBinding.calloc(stack)
            .type(XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING)
            .interactionProfile(profilePath)
            .suggestedBindings(bindingBuffer);

        int result = xrSuggestInteractionProfileBindings(this.instance, suggestion);
        if (result < 0) {
            VRSettings.LOGGER.warn("Vivecraft: Failed to suggest simple controller bindings: {}",
                OpenXRUtil.resultToString(result));
        }
    }

    private void addBinding(List<XrActionSuggestedBinding> bindings, XrAction action,
                            String path, MemoryStack stack)
    {
        if (action == null) return;
        bindings.add(XrActionSuggestedBinding.calloc(stack)
            .action(action)
            .binding(getPath(path)));
    }

    private void mapActionBinding(List<XrActionSuggestedBinding> bindings, String actionName,
                                   String bindingPath, MemoryStack stack)
    {
        XrAction xrAction = this.xrActions.get(actionName);
        if (xrAction != null) {
            addBinding(bindings, xrAction, bindingPath, stack);
            VRSettings.LOGGER.debug("Vivecraft: Bound OpenXR action '{}' -> '{}'", actionName, bindingPath);
        } else {
            VRSettings.LOGGER.warn("Vivecraft: No XrAction found for '{}' — binding to '{}' skipped. " +
                "Available actions: {}", actionName, bindingPath,
                this.xrActions.keySet().stream().sorted().limit(30).toList());
        }
    }

    private void attachActionSets(MemoryStack stack) {
        List<XrActionSet> sets = new ArrayList<>(this.actionSets.values());
        if (sets.isEmpty()) return;

        PointerBuffer actionSetPtrs = stack.callocPointer(sets.size());
        for (int i = 0; i < sets.size(); i++) {
            actionSetPtrs.put(i, sets.get(i));
        }

        XrSessionActionSetsAttachInfo attachInfo = XrSessionActionSetsAttachInfo.calloc(stack)
            .type(XR_TYPE_SESSION_ACTION_SETS_ATTACH_INFO)
            .actionSets(actionSetPtrs);

        int result = xrAttachSessionActionSets(this.session, attachInfo);
        if (result < 0) {
            VRSettings.LOGGER.error("Vivecraft: Failed to attach action sets: {}",
                OpenXRUtil.resultToString(result));
        }
    }

    /**
     * Syncs action state for the given active action sets.
     */
    public void syncActions(Collection<VRInputActionSet> activeActionSets) {
        try (MemoryStack stack = stackPush()) {
            this.activeSetsCache.clear();
            for (VRInputActionSet set : activeActionSets) {
                XrActionSet xrSet = this.actionSets.get(set);
                if (xrSet != null) {
                    this.activeSetsCache.add(xrSet);
                }
            }
            if (this.activeSetsCache.isEmpty()) return;

            XrActiveActionSet.Buffer activeBuffer = XrActiveActionSet.calloc(this.activeSetsCache.size(), stack);
            for (int i = 0; i < this.activeSetsCache.size(); i++) {
                activeBuffer.get(i)
                    .actionSet(this.activeSetsCache.get(i))
                    .subactionPath(XR_NULL_PATH);
            }

            XrActionsSyncInfo syncInfo = XrActionsSyncInfo.calloc(stack)
                .type(XR_TYPE_ACTIONS_SYNC_INFO)
                .activeActionSets(activeBuffer);

            xrSyncActions(this.session, syncInfo);
        }
    }

    /**
     * Reads boolean action state.
     */
    public boolean getActionStateBoolean(VRInputAction action, ControllerType hand,
                                          VRInputAction.DigitalData outData)
    {
        XrAction xrAction = this.xrActions.get(action.name);
        if (xrAction == null) return false;

        try (MemoryStack stack = stackPush()) {
            XrActionStateGetInfo getInfo = XrActionStateGetInfo.calloc(stack)
                .type(XR_TYPE_ACTION_STATE_GET_INFO)
                .action(xrAction);

            if (action.isHanded()) {
                getInfo.subactionPath(hand == ControllerType.LEFT ? this.leftHandPath : this.rightHandPath);
            }

            XrActionStateBoolean state = XrActionStateBoolean.calloc(stack)
                .type(XR_TYPE_ACTION_STATE_BOOLEAN);

            int result = xrGetActionStateBoolean(this.session, getInfo, state);
            if (result < 0) return false;

            outData.state = state.currentState();
            outData.isChanged = state.changedSinceLastSync();
            outData.isActive = state.isActive();
            outData.activeOrigin = hand == ControllerType.LEFT ? ORIGIN_LEFT_HAND : ORIGIN_RIGHT_HAND;
            return true;
        }
    }

    /**
     * Reads float/vector1 action state.
     */
    public boolean getActionStateFloat(VRInputAction action, ControllerType hand,
                                        VRInputAction.AnalogData outData)
    {
        XrAction xrAction = this.xrActions.get(action.name);
        if (xrAction == null) return false;

        try (MemoryStack stack = stackPush()) {
            XrActionStateGetInfo getInfo = XrActionStateGetInfo.calloc(stack)
                .type(XR_TYPE_ACTION_STATE_GET_INFO)
                .action(xrAction);

            if (action.isHanded()) {
                getInfo.subactionPath(hand == ControllerType.LEFT ? this.leftHandPath : this.rightHandPath);
            }

            XrActionStateFloat state = XrActionStateFloat.calloc(stack)
                .type(XR_TYPE_ACTION_STATE_FLOAT);

            int result = xrGetActionStateFloat(this.session, getInfo, state);
            if (result < 0) return false;

            float prevX = outData.x;
            outData.x = state.currentState();
            outData.deltaX = outData.x - prevX;
            outData.isActive = state.isActive();
            outData.activeOrigin = hand == ControllerType.LEFT ? ORIGIN_LEFT_HAND : ORIGIN_RIGHT_HAND;
            return true;
        }
    }

    /**
     * Reads vector2 action state.
     */
    public boolean getActionStateVector2f(VRInputAction action, ControllerType hand,
                                           VRInputAction.AnalogData outData)
    {
        XrAction xrAction = this.xrActions.get(action.name);
        if (xrAction == null) return false;

        try (MemoryStack stack = stackPush()) {
            XrActionStateGetInfo getInfo = XrActionStateGetInfo.calloc(stack)
                .type(XR_TYPE_ACTION_STATE_GET_INFO)
                .action(xrAction);

            if (action.isHanded()) {
                getInfo.subactionPath(hand == ControllerType.LEFT ? this.leftHandPath : this.rightHandPath);
            }

            XrActionStateVector2f state = XrActionStateVector2f.calloc(stack)
                .type(XR_TYPE_ACTION_STATE_VECTOR2F);

            int result = xrGetActionStateVector2f(this.session, getInfo, state);
            if (result < 0) return false;

            float prevX = outData.x;
            float prevY = outData.y;
            outData.x = state.currentState().x();
            outData.y = state.currentState().y();
            outData.deltaX = outData.x - prevX;
            outData.deltaY = outData.y - prevY;
            outData.isActive = state.isActive();
            outData.activeOrigin = hand == ControllerType.LEFT ? ORIGIN_LEFT_HAND : ORIGIN_RIGHT_HAND;
            return true;
        }
    }

    /**
     * Triggers haptic feedback on the specified controller.
     */
    public int triggerHaptic(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
        XrAction action = controller == ControllerType.LEFT ? this.leftHapticAction : this.rightHapticAction;
        if (action == null) return -1;

        try (MemoryStack stack = stackPush()) {
            XrHapticVibration vibration = XrHapticVibration.calloc(stack)
                .type(XR_TYPE_HAPTIC_VIBRATION)
                .duration((long) (durationSeconds * 1_000_000_000L))
                .frequency(frequency)
                .amplitude(amplitude);

            XrHapticActionInfo hapticInfo = XrHapticActionInfo.calloc(stack)
                .type(XR_TYPE_HAPTIC_ACTION_INFO)
                .action(action)
                .subactionPath(controller == ControllerType.LEFT ? this.leftHandPath : this.rightHandPath);

            return xrApplyHapticFeedback(this.session, hapticInfo,
                XrHapticBaseHeader.create(vibration.address()));
        }
    }

    // Accessors for pose spaces
    public XrSpace getLeftGripSpace() { return this.leftGripSpace; }
    public XrSpace getRightGripSpace() { return this.rightGripSpace; }
    public XrSpace getLeftAimSpace() { return this.leftAimSpace; }
    public XrSpace getRightAimSpace() { return this.rightAimSpace; }

    public XrAction getXrAction(String actionName) {
        return this.xrActions.get(actionName);
    }

    public ControllerType getControllerTypeForOrigin(long origin) {
        if (origin == ORIGIN_RIGHT_HAND) return ControllerType.RIGHT;
        if (origin == ORIGIN_LEFT_HAND) return ControllerType.LEFT;
        return null;
    }

    private long getPath(String pathString) {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pathBuf = stack.callocLong(1);
            int result = xrStringToPath(this.instance, stack.UTF8(pathString), pathBuf);
            if (result < 0) {
                VRSettings.LOGGER.error("Vivecraft: Failed to convert path '{}': {}",
                    pathString, OpenXRUtil.resultToString(result));
                return XR_NULL_PATH;
            }
            return pathBuf.get(0);
        }
    }

    private static String sanitizeActionName(String name) {
        // OpenXR action names must be lowercase, alphanumeric, dash, dot, or underscore
        // and must start with a lowercase letter or underscore
        String sanitized = name.toLowerCase()
            .replaceAll("[^a-z0-9._-]", "_")
            .replaceAll("^[^a-z_]", "_");
        if (sanitized.length() > 64) {
            sanitized = sanitized.substring(0, 64);
        }
        return sanitized;
    }

    private static String extractActionName(String fullPath) {
        // "/actions/ingame/in/key.attack" -> "key_attack"
        int lastSlash = fullPath.lastIndexOf('/');
        String name = lastSlash >= 0 ? fullPath.substring(lastSlash + 1) : fullPath;
        return name.replace('.', '_');
    }

    private static int mapActionType(String vrType) {
        return switch (vrType) {
            case "boolean" -> XR_ACTION_TYPE_BOOLEAN_INPUT;
            case "vector1" -> XR_ACTION_TYPE_FLOAT_INPUT;
            case "vector2" -> XR_ACTION_TYPE_VECTOR2F_INPUT;
            default -> -1;
        };
    }

    public void destroy() {
        // Destroy pose spaces
        if (this.leftGripSpace != null) xrDestroySpace(this.leftGripSpace);
        if (this.rightGripSpace != null) xrDestroySpace(this.rightGripSpace);
        if (this.leftAimSpace != null) xrDestroySpace(this.leftAimSpace);
        if (this.rightAimSpace != null) xrDestroySpace(this.rightAimSpace);

        // Destroy actions (action sets own them, destroying action sets handles this)
        for (XrActionSet actionSet : this.actionSets.values()) {
            xrDestroyActionSet(actionSet);
        }
        this.actionSets.clear();
        this.xrActions.clear();
    }
}
