package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.sun.jna.NativeLibrary;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.*;
import org.lwjgl.openvr.*;
import org.lwjgl.openvr.VRBoneTransform.Buffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.*;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.TrackpadSwipeSampler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings.HUDLock;
import org.vivecraft.client_vr.utils.external.jinfinadeck;
import org.vivecraft.client_vr.utils.external.jkatvr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.joml.Math.*;
import static org.lwjgl.openvr.VR.*;
import static org.lwjgl.openvr.VRApplications.*;
import static org.lwjgl.openvr.VRCompositor.*;
import static org.lwjgl.openvr.VRInput.*;
import static org.lwjgl.openvr.VRRenderModels.VRRenderModels_GetComponentButtonMask;
import static org.lwjgl.openvr.VRRenderModels.VRRenderModels_GetComponentStateForDevicePath;
import static org.lwjgl.openvr.VRSettings.VRSettings_GetFloat;
import static org.lwjgl.openvr.VRSystem.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

public class MCOpenVR extends MCVR {
    private final String ACTION_EXTERNAL_CAMERA = "/actions/mixedreality/in/externalcamera";
    private final String ACTION_LEFT_HAND = "/actions/global/in/lefthand";
    private final String ACTION_LEFT_HAND_GESTURE = "/actions/global/in/lefthandbones";
    private final String ACTION_LEFT_HAPTIC = "/actions/global/out/lefthaptic";
    private final String ACTION_RIGHT_HAND = "/actions/global/in/righthand";
    private final String ACTION_RIGHT_HAND_GESTURE = "/actions/global/in/righthandbones";
    private final String ACTION_RIGHT_HAPTIC = "/actions/global/out/righthaptic";
    private final Map<VRInputActionSet, Long> actionSetHandles = new EnumMap<>(VRInputActionSet.class);
    private VRActiveActionSet.Buffer activeActionSetsBuffer;
    private Map<Long, String> controllerComponentNames;
    private Map<String, Matrix4f[]> controllerComponentTransforms;
    private boolean getDeviceProperties = true;
    private long externalCameraPoseHandle;
    private final int[] controllerDeviceIndex = new int[2];
    private boolean getXforms = true;
    private final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private final IntBuffer hmdErrorStore = MemoryUtil.memCallocInt(1);
    private IntBuffer hmdErrorStoreBuf;
    private TrackedDevicePose.Buffer hmdTrackedDevicePoses;
    private boolean inputInitialized;
    private long leftControllerHandle;
    private long leftHapticHandle;
    private long leftPoseHandle;
    private long leftGestureHandle;
    private final InputOriginInfo originInfo;
    private boolean paused = false;
    private final InputPoseActionData poseData;
    private final InputPoseActionData gestureData;
    private long rightControllerHandle;
    private long rightHapticHandle;
    private long rightPoseHandle;
    private long rightGestureHandle;
    private final VRTextureBounds texBounds = VRTextureBounds.calloc();
    private final Map<String, TrackpadSwipeSampler> trackpadSwipeSamplers = new HashMap<>();
    private boolean tried;
    private final Queue<VREvent> vrEvents = new LinkedList<>();
    final Texture texType0 = Texture.calloc();
    final Texture texType1 = Texture.calloc();
    InputDigitalActionData digital = InputDigitalActionData.calloc();
    InputAnalogActionData analog = InputAnalogActionData.calloc();

    public MCOpenVR() {
        super();
        this.hapticScheduler = new OpenVRHapticScheduler();

        for (int i = 0; i < controllerDeviceIndex.length; ++i) {
            this.controllerDeviceIndex[i] = -1;
        }

        this.hmdTrackedDevicePoses = TrackedDevicePose.calloc(k_unMaxTrackedDeviceCount);
        this.poseMatrices = new Matrix4f[k_unMaxTrackedDeviceCount];

        for (int i = 0; i < this.poseMatrices.length; ++i) {
            this.poseMatrices[i] = new Matrix4f();
        }

        this.poseData = InputPoseActionData.calloc();
        this.gestureData = InputPoseActionData.calloc();
        this.originInfo = InputOriginInfo.calloc();
    }

    static String getInputErrorName(int code) {
        return switch (code) {
            case EVRInputError_VRInputError_None -> {
                yield "None";
            }
            case EVRInputError_VRInputError_NameNotFound -> {
                yield "NameNotFound";
            }
            case EVRInputError_VRInputError_WrongType -> {
                yield "WrongType";
            }
            case EVRInputError_VRInputError_InvalidHandle -> {
                yield "InvalidHandle";
            }
            case EVRInputError_VRInputError_InvalidParam -> {
                yield "InvalidParam";
            }
            case EVRInputError_VRInputError_NoSteam -> {
                yield "NoSteam";
            }
            case EVRInputError_VRInputError_MaxCapacityReached -> {
                yield "MaxCapacityReached";
            }
            case EVRInputError_VRInputError_IPCError -> {
                yield "IPCError";
            }
            case EVRInputError_VRInputError_NoActiveActionSet -> {
                yield "NoActiveActionSet";
            }
            case EVRInputError_VRInputError_InvalidDevice -> {
                yield "InvalidDevice";
            }
            case EVRInputError_VRInputError_InvalidSkeleton -> {
                yield "InvalidSkeleton";
            }
            case EVRInputError_VRInputError_InvalidBoneCount -> {
                yield "InvalidBoneCount";
            }
            case EVRInputError_VRInputError_InvalidCompressedData -> {
                yield "InvalidCompressedData";
            }
            case EVRInputError_VRInputError_NoData -> {
                yield "NoData";
            }
            case EVRInputError_VRInputError_BufferTooSmall -> {
                yield "BufferTooSmall";
            }
            case EVRInputError_VRInputError_MismatchedActionManifest -> {
                yield "MismatchedActionManifest";
            }
            case EVRInputError_VRInputError_MissingSkeletonData -> {
                yield "MissingSkeletonData";
            }
            case EVRInputError_VRInputError_InvalidBoneIndex -> {
                yield "InvalidBoneIndex";
            }
            case EVRInputError_VRInputError_InvalidPriority -> {
                yield "InvalidPriority";
            }
            case EVRInputError_VRInputError_PermissionDenied -> {
                yield "PermissionDenied";
            }
            case EVRInputError_VRInputError_InvalidRenderModel -> {
                yield "InvalidRenderModel";
            }
            default -> {
                yield "Unknown";
            }
        };
    }

    @Override
    public void destroy() {
        if (this.initialized) {
            try {
                VR_ShutdownInternal();
                this.initialized = false;

                if (dh.katvr) {
                    jkatvr.Halt();
                }

                if (dh.infinadeck) {
                    jinfinadeck.Destroy();
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Override
    public String getID() {
        return "openvr_lwjgl";
    }

    @Override
    public String getName() {
        return "OpenVR_LWJGL";
    }

    @Override
    public Vector2f getPlayAreaSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (OpenVR.VRChaperone != null && OpenVR.VRChaperone.GetPlayAreaSize != 0) {
                FloatBuffer pSizeZ = stack.callocFloat(1);
                FloatBuffer pSizeX = stack.callocFloat(1);
                boolean b0 = VRChaperone.VRChaperone_GetPlayAreaSize(pSizeX, pSizeZ);
                return b0 ? new Vector2f(pSizeX.get(0) * dh.vrSettings.walkMultiplier, pSizeZ.get(0) * dh.vrSettings.walkMultiplier) : null;
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean init() {
        if (this.initialized) {
            return true;
        } else if (this.tried) {
            return this.initialized;
        } else {
            this.tried = true;
            try {
                this.initializeOpenVR();
                this.initOpenVRCompositor();
//                this.initOpenVRSettings();
//                this.initOpenVRRenderModels();
//                this.initOpenVRChaperone();
//                this.initOpenVRApplications();
//                this.initOpenVRInput();
//                this.initOpenComposite();
            } catch (Exception exception2) {
                exception2.printStackTrace();
                this.initSuccess = false;
                this.initStatus = exception2.getLocalizedMessage();
                return false;
            }

            if (OpenVR.VRInput == null) {
                logger.info("Controller input not available. Forcing seated mode.");
                dh.vrSettings.seated = true;
            }

            logger.info("OpenVR initialized & VR connected.");

            this.initialized = true;

            if (dh.katvr) {
                try {
                    logger.info("Waiting for KATVR....");
                    Utils.unpackNatives("katvr");
                    NativeLibrary.addSearchPath("WalkerBase.dll", new File("openvr/katvr").getAbsolutePath());
                    jkatvr.Init(1);
                    jkatvr.Launch();

                    if (jkatvr.CheckForLaunch()) {
                        logger.info("KATVR Loaded");
                    } else {
                        logger.info("KATVR Failed to load");
                    }
                } catch (Exception exception1) {
                    logger.error("KATVR crashed: {}", exception1.getMessage());
                }
            }

            if (dh.infinadeck) {
                try {
                    logger.info("Waiting for Infinadeck....");
                    Utils.unpackNatives("infinadeck");
                    NativeLibrary.addSearchPath("InfinadeckAPI.dll", (new File("openvr/infinadeck")).getAbsolutePath());

                    if (jinfinadeck.InitConnection()) {
                        jinfinadeck.CheckConnection();
                        logger.info("Infinadeck Loaded");
                    } else {
                        logger.warn("Infinadeck Failed to load");
                    }
                } catch (Exception exception) {
                    logger.error("Infinadeck crashed: {}", exception.getMessage());
                }
            }

            return true;
        }
    }

    @Override
    public void poll(long frameIndex) {
        if (this.initialized) {
            this.paused = VRSystem_ShouldApplicationPause();
            mc.getProfiler().push("events");
            this.pollVREvents();

            if (!dh.vrSettings.seated) {
                mc.getProfiler().popPush("controllers");
                mc.getProfiler().push("gui");

                if (mc.screen == null && dh.vrSettings.vrTouchHotbar) {
                    if (dh.vrSettings.vrHudLockMode != HUDLock.HEAD && this.hudPopup) {
                        this.processHotbar();
                    }
                }

                mc.getProfiler().pop();
            }

            mc.getProfiler().popPush("processEvents");
            this.processVREvents();
            mc.getProfiler().popPush("updatePose/Vsync");
            this.updatePose();
            mc.getProfiler().popPush("processInputs");
            this.processInputs();
            mc.getProfiler().popPush("hmdSampling");
            this.hmdSampling();
            mc.getProfiler().pop();
        }
    }

    @Override
    public void processInputs() {
        if (!dh.vrSettings.seated && !dh.viewonly && this.inputInitialized) {
            for (VRInputAction vrinputaction : this.inputActions.values()) {
                if (vrinputaction.isHanded()) {
                    for (ControllerType controllertype : ControllerType.values()) {
                        vrinputaction.setCurrentHand(controllertype);
                        this.processInputAction(vrinputaction);
                    }
                } else {
                    this.processInputAction(vrinputaction);
                }
            }

            this.processScrollInput(
                GuiHandler.keyScrollAxis,
                () -> InputSimulator.scrollMouse(0.0D, 1.0D),
                () -> InputSimulator.scrollMouse(0.0D, -1.0D)
            );
            this.processScrollInput(
                VivecraftVRMod.keyHotbarScroll,
                () -> this.changeHotbar(-1),
                () -> this.changeHotbar(1)
            );
            this.processSwipeInput(
                VivecraftVRMod.keyHotbarSwipeX,
                () -> this.changeHotbar(1),
                () -> this.changeHotbar(-1),
                null,
                null
            );
            this.processSwipeInput(
                VivecraftVRMod.keyHotbarSwipeY,
                null,
                null,
                () -> this.changeHotbar(-1),
                () -> this.changeHotbar(1)
            );
            this.ignorePressesNextFrame = false;
        }
    }

    @Override
    @Deprecated
    protected void triggerBindingHapticPulse(KeyMapping binding, int duration) {
        ControllerType controllertype = this.findActiveBindingControllerType(binding);

        if (controllertype != null) {
            this.triggerHapticPulse(controllertype, duration);
        }
    }

    private boolean isError() {
        return this.hmdErrorStore.get(0) != EVRInitError_VRInitError_None || this.hmdErrorStoreBuf.get(0) != EVRInitError_VRInitError_None;
    }

    private void debugOut(int deviceindex) {
        try {
            StringBuilder VRDeviceProps = new StringBuilder(5653); // 5653 is the count of guaranteed characters to print
            for (Field field : VR.class.getDeclaredFields()) {
                String[] field_words = field.getName().split("_");

                if (Arrays.asList(field_words).contains("ETrackedDeviceProperty")) {
                    VRDeviceProps.append(field.getName().replace("ETrackedDeviceProperty_Prop_", "\n")).append(" ").append(
                        switch (field_words[field_words.length - 1]) {
                            case "Float" ->
                                VRSystem_GetFloatTrackedDeviceProperty(deviceindex, field.getInt(null), this.hmdErrorStore);
                            case "String" ->
                                VRSystem_GetStringTrackedDeviceProperty(deviceindex, field.getInt(null), this.hmdErrorStore);
                            case "Bool" ->
                                VRSystem_GetBoolTrackedDeviceProperty(deviceindex, field.getInt(null), this.hmdErrorStore);
                            case "Int32" ->
                                VRSystem_GetInt32TrackedDeviceProperty(deviceindex, field.getInt(null), this.hmdErrorStore);
                            case "Uint64" ->
                                VRSystem_GetUint64TrackedDeviceProperty(deviceindex, field.getInt(null), this.hmdErrorStore);
                            default -> "(skipped)";
                        }
                    );
                }
            }
            logger.info("VR DEVICE {}:{}", deviceindex, VRDeviceProps);
        } catch (IllegalAccessException illegalaccessexception) {
            illegalaccessexception.printStackTrace();
        }
    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping binding) {
        if (!this.inputInitialized) {
            return null;
        } else {
            long i = this.getInputAction(binding).getLastOrigin();
            return i != k_ulInvalidInputValueHandle ? this.getOriginControllerType(i) : null;
        }
    }

    private void generateActionManifest() {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        for (VRInputActionSet vrinputactionset : VRInputActionSet.values()) {
            String s = vrinputactionset.usage;

            if (vrinputactionset.advanced && !dh.vrSettings.allowAdvancedBindings) {
                s = "hidden";
            }

            list.add(ImmutableMap.<String, Object>builder().put("name", vrinputactionset.name).put("usage", s).build());
        }

        map.put("action_sets", list);
        List<VRInputAction> list1 = new ArrayList<>(this.inputActions.values());
        list1.sort(Comparator.comparing((action) -> action.keyBinding));
        List<Map<String, Object>> list2 = new ArrayList<>();

        for (VRInputAction vrinputaction : list1) {
            list2.add(ImmutableMap.<String, Object>builder().put("name", vrinputaction.name).put("requirement", vrinputaction.requirement).put("type", vrinputaction.type).build());
        }

        list2.add(ImmutableMap.<String, Object>builder().put("name", this.ACTION_LEFT_HAND).put("requirement", "suggested").put("type", "pose").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", this.ACTION_LEFT_HAND_GESTURE).put("requirement", "optional").put("type", "skeleton").put("skeleton", "/skeleton/hand/left").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", this.ACTION_LEFT_HAPTIC).put("requirement", "suggested").put("type", "vibration").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", this.ACTION_RIGHT_HAND).put("requirement", "suggested").put("type", "pose").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", this.ACTION_RIGHT_HAND_GESTURE).put("requirement", "optional").put("type", "skeleton").put("skeleton", "/skeleton/hand/right").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", this.ACTION_RIGHT_HAPTIC).put("requirement", "suggested").put("type", "vibration").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", this.ACTION_EXTERNAL_CAMERA).put("requirement", "optional").put("type", "pose").build());
        map.put("actions", list2);
        Map<String, Object> map1 = new HashMap<>();

        for (VRInputAction vrinputaction1 : list1) {
            MutableComponent component = Component.translatable(vrinputaction1.keyBinding.getCategory()).append(" - ").append(Component.translatable(vrinputaction1.keyBinding.getName()));
            map1.put(vrinputaction1.name, component.getString());
        }

        for (VRInputActionSet vrinputactionset1 : VRInputActionSet.values()) {
            MutableComponent component = Component.translatable(vrinputactionset1.localizedName);
            map1.put(vrinputactionset1.name, component.getString());
        }

        map1.put(this.ACTION_LEFT_HAND, "Left Hand Pose");
        map1.put(this.ACTION_LEFT_HAND_GESTURE, "Left Hand Gestures");
        map1.put(this.ACTION_LEFT_HAPTIC, "Left Hand Haptic");
        map1.put(this.ACTION_RIGHT_HAND, "Right Hand Pose");
        map1.put(this.ACTION_RIGHT_HAND_GESTURE, "Right Hand Gestures");
        map1.put(this.ACTION_RIGHT_HAPTIC, "Right Hand Haptic");
        map1.put(this.ACTION_EXTERNAL_CAMERA, "External Camera");
        map1.put("language_tag", "en_US");
        map.put("localization", ImmutableList.<Map<String, Object>>builder().add(map1).build());
        List<Map<String, Object>> list3 = new ArrayList<>();
        list3.add(ImmutableMap.<String, Object>builder().put("controller_type", "vive_controller").put("binding_url", "vive_defaults.json").build());
        list3.add(ImmutableMap.<String, Object>builder().put("controller_type", "oculus_touch").put("binding_url", "oculus_defaults.json").build());
        list3.add(ImmutableMap.<String, Object>builder().put("controller_type", "holographic_controller").put("binding_url", "wmr_defaults.json").build());
        list3.add(ImmutableMap.<String, Object>builder().put("controller_type", "knuckles").put("binding_url", "knuckles_defaults.json").build());
        list3.add(ImmutableMap.<String, Object>builder().put("controller_type", "vive_cosmos_controller").put("binding_url", "cosmos_defaults.json").build());
        list3.add(ImmutableMap.<String, Object>builder().put("controller_type", "vive_tracker_camera").put("binding_url", "tracker_defaults.json").build());
        map.put("default_bindings", list3);

        try {
            (new File("openvr/input")).mkdirs();

            try (OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream("openvr/input/action_manifest.json"), StandardCharsets.UTF_8)) {
                this.GSON.toJson(map, outputstreamwriter);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to write action manifest", exception);
        }

        String s1 = dh.vrSettings.reverseHands ? "_reversed" : "";
        Utils.loadAssetToFile("input/vive_defaults" + s1 + ".json", new File("openvr/input/vive_defaults.json"), false);
        Utils.loadAssetToFile("input/oculus_defaults" + s1 + ".json", new File("openvr/input/oculus_defaults.json"), false);
        Utils.loadAssetToFile("input/wmr_defaults" + s1 + ".json", new File("openvr/input/wmr_defaults.json"), false);
        Utils.loadAssetToFile("input/knuckles_defaults" + s1 + ".json", new File("openvr/input/knuckles_defaults.json"), false);
        Utils.loadAssetToFile("input/cosmos_defaults" + s1 + ".json", new File("openvr/input/cosmos_defaults.json"), false);
        Utils.loadAssetToFile("input/tracker_defaults.json", new File("openvr/input/tracker_defaults.json"), false);
    }

    private long getActionHandle(String name) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longbyreference = stack.callocLong(1);
            int i = VRInput_GetActionHandle(name, longbyreference);

            if (i != EVRInputError_VRInputError_None) {
                throw new RuntimeException("Error getting action handle for '" + name + "': " + getInputErrorName(i));
            } else {
                return longbyreference.get(0);
            }
        }
    }

    private boolean updateActiveActionSets() {
        ArrayList<VRInputActionSet> arraylist = new ArrayList<>();
        arraylist.add(VRInputActionSet.GLOBAL);

        // we are always modded
        arraylist.add(VRInputActionSet.MOD);

        arraylist.add(VRInputActionSet.MIXED_REALITY);
        arraylist.add(VRInputActionSet.TECHNICAL);

        if (mc.screen == null) {
            arraylist.add(VRInputActionSet.INGAME);
            arraylist.add(VRInputActionSet.CONTEXTUAL);
        } else {
            arraylist.add(VRInputActionSet.GUI);
            if (dh.vrSettings.ingameBindingsInGui) {
                arraylist.add(VRInputActionSet.INGAME);
            }
        }

        if (KeyboardHandler.isShowing() || RadialHandler.isShowing()) {
            arraylist.add(VRInputActionSet.KEYBOARD);
        }

        if (activeActionSetsBuffer == null) {
            activeActionSetsBuffer = VRActiveActionSet.calloc(arraylist.size());
        } else if (activeActionSetsBuffer.capacity() != arraylist.size()) {
            activeActionSetsBuffer.close();
            activeActionSetsBuffer = VRActiveActionSet.calloc(arraylist.size());
        }

        for (int i = 0; i < arraylist.size(); ++i) {
            VRInputActionSet vrinputactionset = arraylist.get(i);
            activeActionSetsBuffer.get(i).set(this.getActionSetHandle(vrinputactionset), k_ulInvalidInputValueHandle, k_ulInvalidInputValueHandle, 0 /*k_nActionSetPriorityReservedMin*/);
        }

        return !arraylist.isEmpty();
    }

    @Override
    public Matrix4f getControllerComponentTransform(int controllerIndex, String componentName) {
        return this.controllerComponentTransforms != null && this.controllerComponentTransforms.containsKey(componentName) && this.controllerComponentTransforms.get(componentName)[controllerIndex] != null ? (this.controllerComponentTransforms.get(componentName))[controllerIndex] : new Matrix4f();
    }

    private Matrix4f getControllerComponentTransformFromButton(int controllerIndex, long button) {
        return this.controllerComponentNames != null && this.controllerComponentNames.containsKey(button) ? this.getControllerComponentTransform(controllerIndex, this.controllerComponentNames.get(button)) : new Matrix4f();
    }

    private int getError() {
        return this.hmdErrorStore.get(0) != EVRInitError_VRInitError_None ? this.hmdErrorStore.get(0) : this.hmdErrorStoreBuf.get(0);
    }

    long getHapticHandle(ControllerType hand) {
        return hand == ControllerType.RIGHT ? this.rightHapticHandle : this.leftHapticHandle;
    }

    public String memUTF8NullTerminated(ByteBuffer buf) {
        return MemoryUtil.memUTF8(MemoryUtil.memAddress(buf));
    }

    @Override
    public String getOriginName(long handle) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer str = stack.calloc(k_unMaxPropertyStringSize);
            // omit controller type
            int i = VRInput_GetOriginLocalizedName(handle, str, EVRInputStringBits_VRInputString_Hand | EVRInputStringBits_VRInputString_InputSource);

            if (i != EVRInputError_VRInputError_None) {
                throw new RuntimeException("Error getting origin name: " + getInputErrorName(i));
            } else {
                return memUTF8NullTerminated(str);
            }
        }
    }

    float getSuperSampling() {
        return OpenVR.VRSettings == null ? -1.0F : VRSettings_GetFloat("steamvr", "supersampleScale", this.hmdErrorStore);
    }


    private void getTransforms() {
        if (OpenVR.VRRenderModels != null) {
            if (this.getXforms) {
                this.controllerComponentTransforms = new HashMap<>();
            }

            if (this.controllerComponentNames == null) {
                this.controllerComponentNames = new HashMap<>();
            }

            List<String> componentNames = new ArrayList<>(); //TODO: get the controller-specific list
            componentNames.add("tip");
            componentNames.add("handgrip");
            boolean failed = false;

            for (String comp : componentNames) {
                this.controllerComponentTransforms.put(comp, new Matrix4f[]{new Matrix4f(), new Matrix4f()});

                for (int i = 0; i < 2; ++i) {
                    if (this.controllerDeviceIndex[i] == -1) {
                        failed = true;
                    } else {
                        try (MemoryStack stack = MemoryStack.stackPush()) {
                            ByteBuffer stringBuffer = stack.calloc(k_unMaxPropertyStringSize);
                            VRSystem_GetStringTrackedDeviceProperty(this.controllerDeviceIndex[i], ETrackedDeviceProperty_Prop_RenderModelName_String, stringBuffer, this.hmdErrorStore);
                            String renderModelName = this.memUTF8NullTerminated(stringBuffer);
                            VRSystem_GetStringTrackedDeviceProperty(this.controllerDeviceIndex[i], ETrackedDeviceProperty_Prop_InputProfilePath_String, stringBuffer, this.hmdErrorStore);
                            String inputProfilePath = this.memUTF8NullTerminated(stringBuffer);
                            boolean isWMR = inputProfilePath.contains("holographic");
                            boolean isRiftS = inputProfilePath.contains("rifts");

                            String componentName = isWMR && "handgrip".equals(comp) ? "body" : comp;

                            long k = VRRenderModels_GetComponentButtonMask(renderModelName, componentName);

                            if (k > k_ulInvalidInputValueHandle) {
                                this.controllerComponentNames.put(k, comp);
                            }

                            long sourceHandle = i == 0 ? this.rightControllerHandle : this.leftControllerHandle;

                            if (sourceHandle == k_ulInvalidInputValueHandle) {
                                //  print("Failed getting transform: " + comp + " controller " + i);
                                failed = true;
                            } else {
                                RenderModelComponentState renderModelComponentState = RenderModelComponentState.calloc(stack);
                                boolean b0 = VRRenderModels_GetComponentStateForDevicePath(renderModelName, componentName, sourceHandle, RenderModelControllerModeState.calloc(stack), renderModelComponentState);

                                if (!b0) {
                                    failed = true;
                                } else {
                                    convertRM34ToCM44(
                                        renderModelComponentState.mTrackingToComponentLocal().m(),
                                        this.controllerComponentTransforms.get(comp)[i]
                                    );

                                    if (i == 1 && isRiftS && "handgrip".equals(comp)) {
                                        this.controllerComponentTransforms.get(comp)[1] = this.controllerComponentTransforms.get(comp)[0];
                                    }

                                    if (!failed && i == 0) {
                                        try {
                                            Vector3f vector3 = this.getControllerComponentTransform(0, "tip").transformProject(forward, new Vector3f());
                                            Vector3f vector31 = this.getControllerComponentTransform(0, "handgrip").transformProject(forward, new Vector3f());
                                            float f0 = abs(vector3.normalize(new Vector3f()).dot(vector31.normalize(new Vector3f())));
                                            float f1 = acos(f0);
                                            float f2 = (float) toDegrees(f1);
                                            float f3 = acos(vector3.normalize(new Vector3f()).dot(forward.normalize(new Vector3f())));
                                            double d4 = (float) toDegrees(f3);
                                            this.gunStyle = f2 > 10.0D;
                                            this.gunAngle = f2;
                                        } catch (Exception exception) {
                                            failed = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                this.getXforms = failed;
            }
        }
    }

    private void initializeOpenVR() {
        this.hmdErrorStoreBuf = MemoryUtil.memCallocInt(1);
        int token = VR_InitInternal(this.hmdErrorStoreBuf, EVRApplicationType_VRApplication_Scene);

        if (!this.isError()) {
            OpenVR.create(token);
        }

        if (OpenVR.VRSystem != null && !this.isError()) {
            logger.info("OpenVR System Initialized OK.");

            this.initSuccess = true;
        } else {
            throw new RuntimeException(VR_GetVRInitErrorAsEnglishDescription(this.getError()));
        }
    }

    @Override
    public boolean postinit() throws RenderConfigException {
        this.initInputAndApplication();
        return this.inputInitialized;
    }

    private void initInputAndApplication() throws RenderConfigException {
        this.populateInputActions();

        if (OpenVR.VRInput != null) {
            this.generateActionManifest();
            this.loadActionManifest();
            this.loadActionHandles();
            this.installApplicationManifest(false);
            this.inputInitialized = true;
        }
    }

//    private void initOpenComposite() {
//        this.vrOpenComposite = new VR_IVROCSystem_FnTable(VR.VR_GetGenericInterface("FnTable:IVROCSystem_001", this.hmdErrorStoreBuf));
//
//        if (!this.isError()) {
//            this.vrOpenComposite.setAutoSynch(false);
//            this.vrOpenComposite.read();
//            logger.info("OpenComposite initialized.");
//        } else {
//            logger.error("OpenComposite not found: {}", VR.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
//            this.vrOpenComposite = null;
//        }
//    }

    private void initOpenVRCompositor() {
        if (OpenVR.VRSystem != null) {
            VRCompositor_SetTrackingSpace(ETrackingUniverseOrigin_TrackingUniverseStanding);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer pointer = stack.calloc(k_unMaxPropertyStringSize);
                int trackingSpace = VRCompositor_GetTrackingSpace();
                logger.info(
                    "TrackingSpace: {} {}",
                    trackingSpace,
                    Arrays.stream(VR.class.getDeclaredFields()).filter(
                        field -> field.getName().contains("ETrackingUniverseOrigin_TrackingUniverse")
                    ).map(field -> {
                        try {
                            return (field.getInt(null) == trackingSpace ?
                                    field.getName().replace("ETrackingUniverseOrigin_TrackingUniverse", "") :
                                    ""
                            );
                        } catch (Exception ignored) {
                            return "";
                        }
                    }).collect(Collectors.joining())
                );
                VRSystem_GetStringTrackedDeviceProperty(k_unTrackedDeviceIndex_Hmd, ETrackedDeviceProperty_Prop_ManufacturerName_String, pointer, this.hmdErrorStore);
                String s = this.memUTF8NullTerminated(pointer);
                logger.info("Device manufacturer is: {}", s);
                this.detectedHardware = HardwareType.fromManufacturer(s);
            }
            dh.vrSettings.loadOptions();
            VRHotkeys.loadExternalCameraConfig();
        }

        if (OpenVR.VRCompositor == null) {
            logger.info("Skipping VR Compositor...");
        }

        this.texBounds.uMax(1.0F);
        this.texBounds.uMin(0.0F);
        this.texBounds.vMax(1.0F);
        this.texBounds.vMin(0.0F);
        this.texType0.eColorSpace(EColorSpace_ColorSpace_Gamma);
        this.texType0.eType(ETextureType_TextureType_OpenGL);
        this.texType0.handle(ETextureType_TextureType_Invalid);
        this.texType1.eColorSpace(EColorSpace_ColorSpace_Gamma);
        this.texType1.eType(ETextureType_TextureType_OpenGL);
        this.texType1.handle(ETextureType_TextureType_Invalid);
        logger.info("OpenVR Compositor initialized OK.");
    }

    private void installApplicationManifest(boolean force) throws RenderConfigException {
        File file1 = new File("openvr/vivecraft.vrmanifest");
        Utils.loadAssetToFile("vivecraft.vrmanifest", file1, true);
        File file2 = new File("openvr/custom.vrmanifest");

        if (file2.exists()) {
            file1 = file2;
        }

        if (OpenVR.VRApplications != null) {
            String s;

            try {
                Map map = (new Gson()).fromJson(new FileReader(file1), Map.class);
                s = ((Map) ((List) map.get("applications")).get(0)).get("app_key").toString();
            } catch (Exception exception1) {
                logger.error("Error reading appkey from manifest");
                exception1.printStackTrace();
                return;
            }

            logger.info("Appkey: " + s);

            if (!force && VRApplications_IsApplicationInstalled(s)) {
                logger.warn("Application manifest already installed");
            } else {
                int i = VRApplications_AddApplicationManifest(file1.getAbsolutePath(), true);

                if (i != EVRApplicationError_VRApplicationError_None) {
                    // application needs to be installed, so abort
                    StringBuilder pathFormatted = new StringBuilder();
                    boolean hasInvalidChars = false;
                    for (char c : file1.getAbsolutePath().toCharArray()) {
                        if (c > 127) {
                            hasInvalidChars = true;
                            pathFormatted.append("§c").append(c).append("§r");
                        } else {
                            pathFormatted.append(c);
                        }
                    }

                    String error = VRApplications_GetApplicationsErrorNameFromEnum(i) + (hasInvalidChars ? "\nInvalid characters in path: \n" : "\n");
                    logger.error("Failed to install application manifest: {}{}", error, file1.getAbsolutePath());

                    throw new RenderConfigException("Failed to install application manifest", Component.empty().append(error).append(pathFormatted.toString()));
                }

                logger.info("Application manifest installed successfully");
            }

            int j;

            try {
                String s1 = ManagementFactory.getRuntimeMXBean().getName();
                j = Integer.parseInt(s1.split("@")[0]);
            } catch (Exception exception) {
                logger.error("Error getting process id");
                exception.printStackTrace();
                return;
            }

            int k = VRApplications_IdentifyApplication(j, s);

            if (k != EVRApplicationError_VRApplicationError_None) {
                logger.error("Failed to identify application: {}", VRApplications_GetApplicationsErrorNameFromEnum(k));
            } else {
                logger.info("Application identified successfully");
            }
        }
    }

    private void loadActionHandles() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longbyreference = stack.callocLong(1);

            for (VRInputAction vrinputaction : this.inputActions.values()) {
                int i = VRInput_GetActionHandle(vrinputaction.name, longbyreference);

                if (i != EVRInputError_VRInputError_None) {
                    throw new RuntimeException("Error getting action handle for '" + vrinputaction.name + "': " + getInputErrorName(i));
                }

                vrinputaction.setHandle(longbyreference.get(0));
            }

            this.leftPoseHandle = this.getActionHandle(this.ACTION_LEFT_HAND);
            this.leftGestureHandle = this.getActionHandle(this.ACTION_LEFT_HAND_GESTURE);
            this.leftHapticHandle = this.getActionHandle(this.ACTION_LEFT_HAPTIC);
            this.rightPoseHandle = this.getActionHandle(this.ACTION_RIGHT_HAND);
            this.rightGestureHandle = this.getActionHandle(this.ACTION_RIGHT_HAND_GESTURE);
            this.rightHapticHandle = this.getActionHandle(this.ACTION_RIGHT_HAPTIC);
            this.externalCameraPoseHandle = this.getActionHandle(this.ACTION_EXTERNAL_CAMERA);

            for (VRInputActionSet vrinputactionset : VRInputActionSet.values()) {
                int j = VRInput_GetActionSetHandle(vrinputactionset.name, longbyreference);

                if (j != EVRInputError_VRInputError_None) {
                    throw new RuntimeException("Error getting action set handle for '" + vrinputactionset.name + "': " + getInputErrorName(j));
                }

                this.actionSetHandles.put(vrinputactionset, longbyreference.get(0));
            }

            this.leftControllerHandle = this.getInputSourceHandle("/user/hand/left");
            this.rightControllerHandle = this.getInputSourceHandle("/user/hand/right");
        }
    }

    private void loadActionManifest() {
        int i = VRInput_SetActionManifestPath((new File("openvr/input/action_manifest.json")).getAbsolutePath());

        if (i != EVRInputError_VRInputError_None) {
            throw new RuntimeException("Failed to load action manifest: " + getInputErrorName(i));
        }
    }

    private void pollVREvents() {
        if (OpenVR.VRSystem != null) {
            for (VREvent vrevent = VREvent.calloc(); VRSystem_PollNextEvent(vrevent, VREvent.SIZEOF); vrevent = VREvent.calloc()) {
                this.vrEvents.add(vrevent);
            }
        }
    }

    private void processInputAction(VRInputAction action) {
        if (action.isActive() && action.isEnabledRaw()
            // try to prevent double left clicks
            && (!dh.vrSettings.ingameBindingsInGui
            || !(action.actionSet == VRInputActionSet.INGAME && action.keyBinding.key.getType() == Type.MOUSE && action.keyBinding.key.getValue() == 0 && mc.screen != null))) {
            if (action.isButtonChanged()) {
                if (action.isButtonPressed() && action.isEnabled()) {
                    if (!this.ignorePressesNextFrame) {
                        action.pressBinding();
                    }
                } else {
                    action.unpressBinding();
                }
            }
        } else {
            action.unpressBinding();
        }
    }

    private void processScrollInput(KeyMapping keyBinding, Runnable upCallback, Runnable downCallback) {
        VRInputAction vrinputaction = this.getInputAction(keyBinding);

        if (vrinputaction.isEnabled() && vrinputaction.getLastOrigin() != k_ulInvalidInputValueHandle && vrinputaction.getAxis2D(true).y() != 0.0F) {
            float f = vrinputaction.getAxis2D(false).y();

            if (f > 0.0F) {
                upCallback.run();
            } else if (f < 0.0F) {
                downCallback.run();
            }
        }
    }

    private void processSwipeInput(KeyMapping keyBinding, Runnable leftCallback, Runnable rightCallback, Runnable upCallback, Runnable downCallback) {
        VRInputAction vrinputaction = this.getInputAction(keyBinding);

        if (vrinputaction.isEnabled() && vrinputaction.getLastOrigin() != k_ulInvalidInputValueHandle) {
            ControllerType controllertype = this.findActiveBindingControllerType(keyBinding);

            if (controllertype != null) {
                if (!this.trackpadSwipeSamplers.containsKey(keyBinding.getName())) {
                    this.trackpadSwipeSamplers.put(keyBinding.getName(), new TrackpadSwipeSampler());
                }

                TrackpadSwipeSampler trackpadswipesampler = this.trackpadSwipeSamplers.get(keyBinding.getName());
                trackpadswipesampler.update(controllertype, vrinputaction.getAxis2D(false));

                if (trackpadswipesampler.isSwipedUp() && upCallback != null) {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    upCallback.run();
                }

                if (trackpadswipesampler.isSwipedDown() && downCallback != null) {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    downCallback.run();
                }

                if (trackpadswipesampler.isSwipedLeft() && leftCallback != null) {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    leftCallback.run();
                }

                if (trackpadswipesampler.isSwipedRight() && rightCallback != null) {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    rightCallback.run();
                }
            }
        }
    }

    private void processVREvents() {
        while (!this.vrEvents.isEmpty()) {
            VREvent vrevent = this.vrEvents.poll();

            switch (vrevent.eventType()) {
                case EVREventType_VREvent_TrackedDeviceActivated,
                    EVREventType_VREvent_TrackedDeviceDeactivated,
                    EVREventType_VREvent_TrackedDeviceUpdated,
                    EVREventType_VREvent_TrackedDeviceRoleChanged,
                    EVREventType_VREvent_ModelSkinSettingsHaveChanged -> {
                    this.getXforms = true;
                }
                case EVREventType_VREvent_Quit -> {
                    mc.stop();
                }
            }
        }
    }

    private void readOriginInfo(long inputValueHandle) {
        int i = VRInput_GetOriginTrackedDeviceInfo(inputValueHandle, this.originInfo, InputOriginInfo.SIZEOF);

        if (i != EVRInputError_VRInputError_None) {
            throw new RuntimeException("Error reading origin info: " + getInputErrorName(i));
        }
    }

    private void readPoseData(long actionHandle, InputPoseActionData holder) {
        int i = VRInput_GetPoseActionDataForNextFrame(actionHandle, ETrackingUniverseOrigin_TrackingUniverseStanding, holder, InputPoseActionData.SIZEOF, k_ulInvalidActionHandle);

        if (i != EVRInputError_VRInputError_None) {
            throw new RuntimeException("Error reading pose data: " + getInputErrorName(i));
        }
    }

    private void updateControllerPose(int controller, long actionHandle) {
        this.readPoseData(actionHandle, this.poseData);

        if (this.poseData.activeOrigin() != k_ulInvalidActionHandle) {
            this.readOriginInfo(this.poseData.activeOrigin());
            int i = this.originInfo.trackedDeviceIndex();

            if (i != this.controllerDeviceIndex[controller]) {
                this.getXforms = true;
            }

            this.controllerDeviceIndex[controller] = i;

            if (i != k_unTrackedDeviceIndexInvalid) {
                TrackedDevicePose trackeddevicepose = this.poseData.pose();

                if (trackeddevicepose.bPoseIsValid()) {
                    this.controllerPose[controller].set(this.poseMatrices[i]);
                    this.controllerTracking[controller] = true;
                    return;
                }
            }
        } else {
            this.controllerDeviceIndex[controller] = -1;
        }

        this.controllerTracking[controller] = false;
    }

    private void updateControllerGesture(int controller, long gestureHandle) {
        IntBuffer controllerStatics = MemoryUtil.memCallocInt(2);
        VRInput_GetBoneCount(gestureHandle, controllerStatics);
        int BoneCount = controllerStatics.get(0);
        // print("Bone Count: " + BoneCount);
        controllerStatics.position(1);
        VRInput_GetSkeletalTrackingLevel(gestureHandle, controllerStatics);
        int TrackingLevel = controllerStatics.get(1);
        // print("Skeletal Tracking Level: " + TrackingLevel);
        this.controllerSkeletalInputTrackingLevel[controller] = TrackingLevel;
        try (
            // InputSkeletalActionData BoneActDat = InputSkeletalActionData.calloc();
            Buffer BoneTransDat = VRBoneTransform.calloc(BoneCount);
            VRSkeletalSummaryData DevBoneSum = VRSkeletalSummaryData.calloc()
        ) {
            // VRInput.VRInput_GetSkeletalActionData(gestureHandle, BoneActDat);
            // print("Skeletal Action Data: " + BoneActDat.bActive() + " " + BoneActDat.activeOrigin());
            VRInput_GetSkeletalBoneData(gestureHandle, EVRSkeletalTransformSpace_VRSkeletalTransformSpace_Model, EVRSkeletalMotionRange_VRSkeletalMotionRange_WithController, BoneTransDat);
            // print("Skeletal Bone Data: " + BoneTransDat);
            this.gestureFingerTransforms[controller].clear();
            this.gestureFingerTransforms[controller].ensureCapacity(BoneCount);
            this.gestureFingerOrientations[controller].clear();
            this.gestureFingerOrientations[controller].ensureCapacity(BoneCount);
            for (VRBoneTransform BoneTrans : BoneTransDat) {
                HmdVector4 pos = BoneTrans.position$();
                // print("Skeletal Bone Position: " + pos);
                this.gestureFingerTransforms[controller].add(new Vector4f(pos.v()));
                HmdQuaternionf dir = BoneTrans.orientation();
                // print("Skeletal Bone Orientation: " + BoneTrans.orientation());
                this.gestureFingerOrientations[controller].add(new Quaternionf(dir.w(), dir.x(), dir.y(), dir.z()));
            }
            VRInput_GetSkeletalSummaryData(gestureHandle, EVRSummaryType_VRSummaryType_FromDevice, DevBoneSum);
            this.gestureFingerSplay[controller].clear();
            this.gestureFingerSplay[controller].ensureCapacity(EVRFingerSplay_VRFingerSplay_Count);
            for (int i = 0; i < EVRFingerSplay_VRFingerSplay_Count; i++) {
                this.gestureFingerSplay[controller].add(i, DevBoneSum.flFingerSplay(i));
//                print(switch (i){
//                    case EVRFingerSplay_VRFingerSplay_Thumb_Index -> "Thumb Index Splay: ";
//                    case EVRFingerSplay_VRFingerSplay_Index_Middle -> "Index Middle Splay: ";
//                    case EVRFingerSplay_VRFingerSplay_Middle_Ring -> "Middle Ring Splay: ";
//                    case EVRFingerSplay_VRFingerSplay_Ring_Pinky -> "Ring Pinky Splay: ";
//                    default -> "Unknown Splay: ";
//                } + DevBoneSum.flFingerSplay(i));
            }
            this.gestureFingerCurl[controller].clear();
            this.gestureFingerCurl[controller].ensureCapacity(EVRFinger_VRFinger_Count);
            for (int i = 0; i < EVRFinger_VRFinger_Count; i++) {
                this.gestureFingerCurl[controller].add(i, DevBoneSum.flFingerCurl(i));
//                print(switch(i) {
//                    case EVRFinger_VRFinger_Thumb -> "Thumb Curl: ";
//                    case EVRFinger_VRFinger_Index -> "Index Curl: ";
//                    case EVRFinger_VRFinger_Middle -> "Middle Curl: ";
//                    case EVRFinger_VRFinger_Ring -> "Ring Curl: ";
//                    case EVRFinger_VRFinger_Pinky -> "Pinky Curl: ";
//                    default -> "Unknown Curl: ";
//                } + DevBoneSum.flFingerCurl(i));
            }
//            if (this.TPose) {
//                try (
//                    VRBoneTransform.Buffer ReferenceTransDat = VRBoneTransform.calloc(BoneCount);
//                ) {
//                    VRInput_GetSkeletalReferenceTransforms(gestureHandle, EVRSkeletalTransformSpace_VRSkeletalTransformSpace_Model, EVRSkeletalReferencePose_VRSkeletalReferencePose_BindPose, ReferenceTransDat);
//                    HmdVector4 hold = ReferenceTransDat.position$();
//                    new Vector3f(hold.v(0), hold.v(1), hold.v(2));
//                    Utils.Matrix4fCopy(, this.gesturePose[controller]);
//                }
//                this.controllerTracking[controller] = true;
//            } else {
            this.readPoseData(gestureHandle, this.gestureData);
            TrackedDevicePose trackeddevicepose = this.gestureData.pose();
            if (trackeddevicepose.bPoseIsValid()) {
                this.gesturePose[controller].set(this.poseMatrices[this.originInfo.trackedDeviceIndex()]);
                this.gestureVelocity[controller].set(new Vector3f().setFromAddress(trackeddevicepose.vVelocity().address()));
            }
//            }
        }

        MemoryUtil.memFree(controllerStatics);
    }

    private void updatePose() {
        if (OpenVR.VRSystem != null && OpenVR.VRCompositor != null) {
            int ret = VRCompositor_WaitGetPoses(this.hmdTrackedDevicePoses, null);

            if (ret != EVRCompositorError_VRCompositorError_None) {
                logger.error("Compositor Error: GetPoseError {}", OpenVRStereoRenderer.getCompositorError(ret));
            }

            if (ret == EVRCompositorError_VRCompositorError_DoNotHaveFocus) { //this is so dumb but it works.
                this.triggerHapticPulse(0, 500);
                this.triggerHapticPulse(1, 500);
            }

            if (this.getXforms) { //set null by events.
                this.getTransforms(); //do we want the dynamic info? I don't think so...
                //findControllerDevices();
            } else if (this.getDeviceProperties) {
                this.getDeviceProperties = false;
                this.debugOut(0);
                this.debugOut(this.controllerDeviceIndex[0]);
                this.debugOut(this.controllerDeviceIndex[1]);
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                HmdMatrix34 hmdmatrix34 = HmdMatrix34.calloc(stack);
                convertRM34ToCM44(VRSystem_GetEyeToHeadTransform(EVREye_Eye_Left, hmdmatrix34).m(), this.hmdPoseLeftEye);
                convertRM34ToCM44(VRSystem_GetEyeToHeadTransform(EVREye_Eye_Right, hmdmatrix34).m(), this.hmdPoseRightEye);
            }

            for (int j = 0; j < this.poseMatrices.length; ++j) {

                if (this.hmdTrackedDevicePoses.get(j).bPoseIsValid()) {
                    convertRM34ToCM44(this.hmdTrackedDevicePoses.get(j).mDeviceToAbsoluteTracking().m(), this.poseMatrices[j]);
                }
            }

            if (this.hmdTrackedDevicePoses.get(0).bPoseIsValid()) {
                this.hmdPose.set(this.poseMatrices[0])
                    // wtf
                    .transpose3x3();
                this.headIsTracking = true;
            } else {
                this.headIsTracking = false;
                this.hmdPose.identity();
                this.hmdPose.m13(1.62F);
            }

            if (this.inputInitialized) {
                mc.getProfiler().push("updateActionState");

                if (this.updateActiveActionSets()) {
                    int k = VRInput_UpdateActionState(this.activeActionSetsBuffer, VRActiveActionSet.SIZEOF);

                    if (k != EVRInputError_VRInputError_None) {
                        throw new RuntimeException("Error updating action state: code " + getInputErrorName(k));
                    }
                }

                this.inputActions.values().forEach(this::readNewData);
                mc.getProfiler().pop();

                if (dh.vrSettings.reverseHands) {
                    this.updateControllerPose(0, this.leftPoseHandle);
                    this.updateControllerPose(1, this.rightPoseHandle);
                    if (dh.vrSettings.skeletalInput) {
                        this.updateControllerGesture(0, this.leftGestureHandle);
                        this.updateControllerGesture(1, this.rightGestureHandle);
                    }
                } else {
                    this.updateControllerPose(0, this.rightPoseHandle);
                    this.updateControllerPose(1, this.leftPoseHandle);
                    if (dh.vrSettings.skeletalInput) {
                        this.updateControllerGesture(0, this.rightGestureHandle);
                        this.updateControllerGesture(1, this.leftGestureHandle);
                    }
                }

                // this.updateControllerPose(2, this.externalCameraPoseHandle);
            }

            this.updateAim();
        }
    }

    long getActionSetHandle(VRInputActionSet actionSet) {
        return this.actionSetHandles.get(actionSet);
    }

    long getControllerHandle(ControllerType hand) {
        if (dh.vrSettings.reverseHands) {
            return hand == ControllerType.RIGHT ? this.leftControllerHandle : this.rightControllerHandle;
        } else {
            return hand == ControllerType.RIGHT ? this.rightControllerHandle : this.leftControllerHandle;
        }
    }

    long getGestureHandle(ControllerType hand) {
        return (dh.vrSettings.reverseHands ?
                (hand == ControllerType.RIGHT ? this.leftGestureHandle : this.rightGestureHandle) :
                (hand == ControllerType.RIGHT ? this.rightGestureHandle : this.leftGestureHandle)
        );
    }

    long getInputSourceHandle(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longbyreference = stack.callocLong(1);
            int i = VRInput_GetInputSourceHandle(path, longbyreference);

            if (i != EVRInputError_VRInputError_None) {
                throw new RuntimeException("Error getting input source handle for '" + path + "': " + getInputErrorName(i));
            } else {
                return longbyreference.get(0);
            }
        }
    }

    @Override
    public ControllerType getOriginControllerType(long inputValueHandle) {
        if (inputValueHandle != k_ulInvalidInputValueHandle) {
            this.readOriginInfo(inputValueHandle);

            if (this.originInfo.trackedDeviceIndex() != k_unTrackedDeviceIndexInvalid) {
                if (this.originInfo.trackedDeviceIndex() == this.controllerDeviceIndex[0]) {
                    return ControllerType.RIGHT;
                }

                if (this.originInfo.trackedDeviceIndex() == this.controllerDeviceIndex[1]) {
                    return ControllerType.LEFT;
                }
            }
        }
        return null;
    }

    public void readNewData(VRInputAction action) {
        switch (action.type) {
            case "boolean" -> {
                if (action.isHanded()) {
                    for (ControllerType controllertype1 : ControllerType.values()) {
                        this.readDigitalData(action, controllertype1);
                    }
                } else {
                    this.readDigitalData(action, null);
                }
            }
            case "vector1", "vector2", "vector3" -> {
                if (action.isHanded()) {
                    for (ControllerType controllertype : ControllerType.values()) {
                        this.readAnalogData(action, controllertype);
                    }
                } else {
                    this.readAnalogData(action, null);
                }
            }
        }
    }

    private void readDigitalData(VRInputAction action, ControllerType hand) {
        int i = 0;

        if (hand != null) {
            i = hand.ordinal();
        }

        int j = VRInput_GetDigitalActionData(action.handle, this.digital, InputDigitalActionData.SIZEOF, hand != null ? this.getControllerHandle(hand) : 0L);

        if (j != 0) {
            throw new RuntimeException("Error reading digital data for '" + action.name + "': " + getInputErrorName(j));
        } else {
            action.digitalData[i].activeOrigin = this.digital.activeOrigin();
            action.digitalData[i].isActive = this.digital.bActive();
            action.digitalData[i].state = this.digital.bState();
            action.digitalData[i].isChanged = this.digital.bChanged();
        }
    }

    private void readAnalogData(VRInputAction action, ControllerType hand) {
        int i = 0;

        if (hand != null) {
            i = hand.ordinal();
        }

        int j = VRInput_GetAnalogActionData(action.handle, this.analog, InputAnalogActionData.SIZEOF, hand != null ? this.getControllerHandle(hand) : 0L);

        if (j != 0) {
            throw new RuntimeException("Error reading analog data for '" + action.name + "': " + getInputErrorName(j));
        } else {
            action.analogData[i].x = this.analog.x();
            action.analogData[i].y = this.analog.y();
            action.analogData[i].z = this.analog.z();
            action.analogData[i].deltaX = this.analog.deltaX();
            action.analogData[i].deltaY = this.analog.deltaY();
            action.analogData[i].deltaZ = this.analog.deltaZ();
            action.analogData[i].activeOrigin = this.analog.activeOrigin();
            action.analogData[i].isActive = this.analog.bActive();
        }
    }

    @Override
    public boolean hasThirdController() {
        return this.controllerDeviceIndex[2] != -1;
    }

    @Override
    public List<Long> getOrigins(VRInputAction action) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longbyreference = stack.callocLong(k_unMaxActionOriginCount);
            int i = VRInput_GetActionOrigins(this.getActionSetHandle(action.actionSet), action.handle, longbyreference);

            if (i != EVRInputError_VRInputError_None) {
                throw new RuntimeException("Error getting action origins for '" + action.name + "': " + getInputErrorName(i));
            } else {
                List<Long> list = new ArrayList<>();

                while (longbyreference.remaining() > 0) {
                    long j = longbyreference.get();
                    if (j != k_ulInvalidActionHandle) {
                        list.add(j);
                    }
                }

                return list;
            }
        }
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new OpenVRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        return switch (VRSystem_GetTrackedDeviceActivityLevel(k_unTrackedDeviceIndex_Hmd)) {
            case EDeviceActivityLevel_k_EDeviceActivityLevel_UserInteraction,
                EDeviceActivityLevel_k_EDeviceActivityLevel_UserInteraction_Timeout -> true;
            default -> false;
        };
    }
}
