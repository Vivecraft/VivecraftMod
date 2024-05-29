package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.sun.jna.NativeLibrary;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.*;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.TrackpadSwipeSampler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.external.jinfinadeck;
import org.vivecraft.client_vr.utils.external.jkatvr;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Vector3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.lwjgl.openvr.VR.*;
import static org.lwjgl.openvr.VRApplications.*;
import static org.lwjgl.openvr.VRCompositor.*;
import static org.lwjgl.openvr.VRInput.*;
import static org.lwjgl.openvr.VRRenderModels.*;
import static org.lwjgl.openvr.VRSettings.VRSettings_GetFloat;
import static org.lwjgl.openvr.VRSystem.*;

public class MCOpenVR extends MCVR {
    public static final int LEFT_CONTROLLER = 1;
    public static final int RIGHT_CONTROLLER = 0;
    public static final int CAMERA_TRACKER = 2;
    protected static MCOpenVR ome;
    private static final String ACTION_EXTERNAL_CAMERA = "/actions/mixedreality/in/externalcamera";
    private static final String ACTION_LEFT_HAND = "/actions/global/in/lefthand";
    private static final String ACTION_LEFT_HAPTIC = "/actions/global/out/lefthaptic";
    private static final String ACTION_RIGHT_HAND = "/actions/global/in/righthand";
    private static final String ACTION_RIGHT_HAPTIC = "/actions/global/out/righthaptic";
    private final Map<VRInputActionSet, Long> actionSetHandles = new EnumMap<>(VRInputActionSet.class);
    private VRActiveActionSet.Buffer activeActionSetsBuffer;
    private Map<Long, String> controllerComponentNames;
    private Map<String, Matrix4f[]> controllerComponentTransforms;
    private boolean dbg = true;
    private long externalCameraPoseHandle;
    private final int[] controllerDeviceIndex = new int[3];
    private boolean getXforms = true;
    private final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private final TrackedDevicePose.Buffer hmdTrackedDevicePoses;
    private boolean inputInitialized;
    private long leftControllerHandle;
    private long leftHapticHandle;
    private long leftPoseHandle;
    private final InputOriginInfo originInfo;
    private boolean paused = false;
    private final InputPoseActionData poseData;
    private long rightControllerHandle;
    private long rightHapticHandle;
    private long rightPoseHandle;
    private final Map<String, TrackpadSwipeSampler> trackpadSwipeSamplers = new HashMap<>();
    private boolean tried;
    private final Queue<VREvent> vrEvents = new LinkedList<>();
    private final VRTextureBounds texBounds;
    protected final Texture texType0;
    protected final Texture texType1;
    private final InputDigitalActionData digital;
    private final InputAnalogActionData analog;
    private final IntBuffer hmdErrorStore;

    // Last updated 10/29/2023
    // Hard-coded list of languages Steam supports
    private static final Map<String, String> steamLanguages = Map.ofEntries(
        Map.entry("english", "en_US"),
        Map.entry("bulgarian", "bg_BG"),
        Map.entry("schinese", "zh_CN"),
        Map.entry("tchinese", "zh_TW"),
        Map.entry("czech", "cs_CZ"),
        Map.entry("danish", "da_DK"),
        Map.entry("dutch", "nl_NL"),
        Map.entry("finnish", "fi_FI"),
        Map.entry("french", "fr_FR"),
        Map.entry("german", "de_DE"),
        Map.entry("greek", "el_GR"),
        Map.entry("hungarian", "hu_HU"),
        Map.entry("indonesian", "id_ID"),
        Map.entry("italian", "it_IT"),
        Map.entry("japanese", "ja_JP"),
        Map.entry("koreana", "ko_KR"),
        Map.entry("norwegian", "no_NO"),
        Map.entry("polish", "pl_PL"),
        Map.entry("portuguese", "pt_PT"),
        Map.entry("brazilian", "pt_BR"),
        Map.entry("romanian", "ro_RO"),
        Map.entry("russian", "ru_RU"),
        Map.entry("spanish", "es_ES"),
        Map.entry("latam", "es_MX"),
        Map.entry("swedish", "sv_SE"),
        Map.entry("thai", "th_TH"),
        Map.entry("turkish", "tr_TR"),
        Map.entry("ukrainian", "uk_UA"),
        Map.entry("vietnamese", "vi_VN")
    );

    // Steam uses some incorrect language codes, this remaps to those
    // SteamVR itself is also not translated into all languages Steam supports yet, so in those cases English may be used regardless
    private static final Map<String, String> steamLanguageWrongMappings = Map.ofEntries(
        Map.entry("cs_CZ", "cs_CS"),
        Map.entry("da_DK", "da_DA"),
        Map.entry("el_GR", "el_EL"),
        Map.entry("sv_SE", "sv_SV")
    );

    public static MCOpenVR get() {
        return ome;
    }

    public MCOpenVR(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh, VivecraftVRMod.INSTANCE);
        ome = this;
        this.hapticScheduler = new OpenVRHapticScheduler();

        for (int i = 0; i < 3; i++) {
            this.controllerDeviceIndex[i] = -1;
        }

        // allocate memory
        this.poseData = InputPoseActionData.calloc();
        this.originInfo = InputOriginInfo.calloc();

        this.hmdErrorStore = MemoryUtil.memCallocInt(1);

        this.texBounds = VRTextureBounds.calloc();
        this.texType0 = Texture.calloc();
        this.texType1 = Texture.calloc();

        this.digital = InputDigitalActionData.calloc();
        this.analog = InputAnalogActionData.calloc();

        this.hmdTrackedDevicePoses = TrackedDevicePose.calloc(k_unMaxTrackedDeviceCount);
    }

    @Override
    public void destroy() {
        if (this.initialized) {
            try {
                VR_ShutdownInternal();
                this.initialized = false;

                if (ClientDataHolderVR.katvr) {
                    jkatvr.Halt();
                }

                if (ClientDataHolderVR.infinadeck) {
                    jinfinadeck.Destroy();
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        // free memory
        this.hmdTrackedDevicePoses.free();

        this.poseData.free();
        this.originInfo.free();

        this.texBounds.free();
        this.texType0.free();
        this.texType1.free();

        this.digital.free();
        this.analog.free();

        if (this.activeActionSetsBuffer != null) {
            this.activeActionSetsBuffer.free();
        }
    }

    static String getInputErrorName(int code) {
        return switch (code) {
            case 0 -> "wat";
            case 1 -> "NameNotFound";
            case 2 -> "WrongType";
            case 3 -> "InvalidHandle";
            case 4 -> "InvalidParam";
            case 5 -> "NoSteam";
            case 6 -> "MaxCapacityReached";
            case 7 -> "IPCError";
            case 8 -> "NoActiveActionSet";
            case 9 -> "InvalidDevice";
            case 10 -> "InvalidSkeleton";
            case 11 -> "InvalidBoneCount";
            case 12 -> "InvalidCompressedData";
            case 13 -> "NoData";
            case 14 -> "BufferTooSmall";
            case 15 -> "MismatchedActionManifest";
            case 16 -> "MissingSkeletonData";
            case 17 -> "InvalidBoneIndex";
            case 18 -> "InvalidPriority";
            case 19 -> "PermissionDenied";
            case 20 -> "InvalidRenderModel";
            default -> "Unknown";
        };
    }

    @Override
    public String getName() {
        return "OpenVR_LWJGL";
    }

    /**
     * @return Play area size or null if not valid
     */
    @Override
    public Vector2f getPlayAreaSize() {
        if (OpenVR.VRChaperone != null && OpenVR.VRChaperone.GetPlayAreaSize != 0) {
            return null;
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer pSizeZ = stack.callocFloat(1);
                FloatBuffer pSizeX = stack.callocFloat(1);
                boolean valid = VRChaperone.VRChaperone_GetPlayAreaSize(pSizeX, pSizeZ);
                return valid ? new Vector2f(pSizeX.get(0) * this.dh.vrSettings.walkMultiplier,
                    pSizeZ.get(0) * this.dh.vrSettings.walkMultiplier) : null;
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
            this.mc = Minecraft.getInstance();
            try {
                // inits openvr, and loads the function tables
                this.initializeOpenVR();

                // sets up the tracking space and generates the render textures
                this.initOpenVRCompositor();
            } catch (Exception exception2) {
                exception2.printStackTrace();
                this.initSuccess = false;
                this.initStatus = exception2.getLocalizedMessage();
                return false;
            }

            if (OpenVR.VRInput == null) {
                VRSettings.logger.error("Controller input not available. Forcing seated mode.");
                this.dh.vrSettings.seated = true;
            }

            VRSettings.logger.info("OpenVR initialized & VR connected.");
            this.deviceVelocity = new Vec3[k_unMaxTrackedDeviceCount];

            for (int i = 0; i < this.poseMatrices.length; ++i) {
                this.poseMatrices[i] = new Matrix4f();
                this.deviceVelocity[i] = new Vec3(0.0D, 0.0D, 0.0D);
            }

            this.initialized = true;

            if (ClientDataHolderVR.katvr) {
                try {
                    VRSettings.logger.info("Waiting for KATVR....");
                    Utils.unpackNatives("katvr");
                    NativeLibrary.addSearchPath("WalkerBase.dll", (new File("openvr/katvr")).getAbsolutePath());
                    jkatvr.Init(1);
                    jkatvr.Launch();

                    if (jkatvr.CheckForLaunch()) {
                        VRSettings.logger.info("KATVR Loaded");
                    } else {
                        VRSettings.logger.error("KATVR Failed to load");
                    }
                } catch (Exception exception1) {
                    VRSettings.logger.error("KATVR crashed: {}", exception1.getMessage());
                }
            }

            if (ClientDataHolderVR.infinadeck) {
                try {
                    VRSettings.logger.info("Waiting for Infinadeck....");
                    Utils.unpackNatives("infinadeck");
                    NativeLibrary.addSearchPath("InfinadeckAPI.dll", (new File("openvr/infinadeck")).getAbsolutePath());

                    if (jinfinadeck.InitConnection()) {
                        jinfinadeck.CheckConnection();
                        VRSettings.logger.info("Infinadeck Loaded");
                    } else {
                        VRSettings.logger.error("Infinadeck Failed to load");
                    }
                } catch (Exception exception) {
                    VRSettings.logger.error("Infinadeck crashed: {}", exception.getMessage());
                }
            }

            return true;
        }
    }

    @Override
    public void poll(long frameIndex) {
        if (!this.initialized) return;

        this.paused = VRSystem_ShouldApplicationPause();
        this.mc.getProfiler().push("events");
        this.pollVREvents();

        if (!this.dh.vrSettings.seated) {
            this.mc.getProfiler().popPush("controllers");
            this.mc.getProfiler().push("gui");

            if (this.mc.screen == null && this.dh.vrSettings.vrTouchHotbar) {
                if (this.dh.vrSettings.vrHudLockMode != VRSettings.HUDLock.HEAD && this.hudPopup) {
                    this.processHotbar();
                }
            }

            this.mc.getProfiler().pop();
        }

        this.mc.getProfiler().popPush("processEvents");
        this.processVREvents();
        this.mc.getProfiler().popPush("updatePose/Vsync");
        this.updatePose();
        this.mc.getProfiler().popPush("processInputs");
        this.processInputs();
        this.mc.getProfiler().popPush("hmdSampling");
        this.hmdSampling();
        this.mc.getProfiler().pop();
    }

    @Override
    public void processInputs() {
        if (this.dh.vrSettings.seated || ClientDataHolderVR.viewonly || !this.inputInitialized) return;

        for (VRInputAction action : this.inputActions.values()) {
            if (action.isHanded()) {
                for (ControllerType controllertype : ControllerType.values()) {
                    action.setCurrentHand(controllertype);
                    this.processInputAction(action);
                }
            } else {
                this.processInputAction(action);
            }
        }

        this.processScrollInput(GuiHandler.keyScrollAxis,
            () -> InputSimulator.scrollMouse(0.0D, 1.0D),
            () -> InputSimulator.scrollMouse(0.0D, -1.0D));
        this.processScrollInput(VivecraftVRMod.INSTANCE.keyHotbarScroll,
            () -> this.changeHotbar(-1),
            () -> this.changeHotbar(1));
        this.processSwipeInput(VivecraftVRMod.INSTANCE.keyHotbarSwipeX,
            () -> this.changeHotbar(1),
            () -> this.changeHotbar(-1), null, null);
        this.processSwipeInput(VivecraftVRMod.INSTANCE.keyHotbarSwipeY, null, null,
            () -> this.changeHotbar(-1),
            () -> this.changeHotbar(1));
        this.ignorePressesNextFrame = false;
    }

    private boolean isError() {
        return this.hmdErrorStore.get(0) != EVRInitError_VRInitError_None;
    }

    private void debugOut(int deviceindex) {
//        VRSettings.logger.info("******************* VR DEVICE: " + deviceindex + " *************************");
//
//        for (Field field : VR.ETrackedDeviceProperty.class.getDeclaredFields()) {
//            try {
//                String[] astring = field.getName().split("_");
//                String s = astring[astring.length - 1];
//                String s1 = "";
//
//                VRSystem_ShouldApplicationPause()
//
//                if (s.equals("Float")) {
//                    s1 = s1 + field.getName() + " " + this.vrsystem.GetFloatTrackedDeviceProperty.apply(deviceindex, field.getInt((Object) null), this.hmdErrorStore);
//                } else if (s.equals("String")) {
//                    Pointer pointer = new Memory(32768L);
//                    this.vrsystem.GetStringTrackedDeviceProperty.apply(deviceindex, field.getInt((Object) null), pointer, 32767, this.hmdErrorStore);
//                    s1 = s1 + field.getName() + " " + pointer.getString(0L);
//                } else if (s.equals("Bool")) {
//                    s1 = s1 + field.getName() + " " + this.vrsystem.GetBoolTrackedDeviceProperty.apply(deviceindex, field.getInt((Object) null), this.hmdErrorStore);
//                } else if (s.equals("Int32")) {
//                    s1 = s1 + field.getName() + " " + this.vrsystem.GetInt32TrackedDeviceProperty.apply(deviceindex, field.getInt((Object) null), this.hmdErrorStore);
//                } else if (s.equals("Uint64")) {
//                    s1 = s1 + field.getName() + " " + this.vrsystem.GetUint64TrackedDeviceProperty.apply(deviceindex, field.getInt((Object) null), this.hmdErrorStore);
//                } else {
//                    s1 = s1 + field.getName() + " (skipped)";
//                }
//
//                VRSettings.logger.info(s1.replace("ETrackedDeviceProperty_Prop_", ""));
//            } catch (IllegalAccessException illegalaccessexception) {
//                illegalaccessexception.printStackTrace();
//            }
//        }
//
//        VRSettings.logger.info("******************* END VR DEVICE: " + deviceindex + " *************************");
    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping keyMapping) {
        if (!this.inputInitialized) {
            return null;
        } else {
            long origin = this.getInputAction(keyMapping).getLastOrigin();
            return origin != k_ulInvalidInputValueHandle ? this.getOriginControllerType(origin) : null;
        }
    }

    private void generateActionManifest() {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> actionSets = new ArrayList<>();

        for (VRInputActionSet actionSet : VRInputActionSet.values()) {
            String usage = actionSet.usage;

            if (actionSet.advanced && !this.dh.vrSettings.allowAdvancedBindings) {
                usage = "hidden";
            }

            actionSets.add(ImmutableMap.<String, Object>builder().put("name", actionSet.name).put("usage", usage).build());
        }

        map.put("action_sets", actionSets);

        // Sort the bindings, so they're easy to look through in SteamVR
        List<VRInputAction> sortedActions = new ArrayList<>(this.inputActions.values());
        sortedActions.sort(Comparator.comparing((action) -> action.keyBinding));

        List<Map<String, Object>> actions = new ArrayList<>();

        for (VRInputAction action : sortedActions) {
            actions.add(ImmutableMap.<String, Object>builder().put("name", action.name).put("requirement", action.requirement).put("type", action.type).build());
        }

        actions.add(ImmutableMap.<String, Object>builder().put("name", ACTION_LEFT_HAND).put("requirement", "suggested").put("type", "pose").build());
        actions.add(ImmutableMap.<String, Object>builder().put("name", ACTION_RIGHT_HAND).put("requirement", "suggested").put("type", "pose").build());
        actions.add(ImmutableMap.<String, Object>builder().put("name", ACTION_EXTERNAL_CAMERA).put("requirement", "optional").put("type", "pose").build());
        actions.add(ImmutableMap.<String, Object>builder().put("name", ACTION_LEFT_HAPTIC).put("requirement", "suggested").put("type", "vibration").build());
        actions.add(ImmutableMap.<String, Object>builder().put("name", ACTION_RIGHT_HAPTIC).put("requirement", "suggested").put("type", "vibration").build());
        map.put("actions", actions);

        // TODO: revert to exporting all Steam languages when Valve fixes the crash with large action manifests
        List<String> languages = new ArrayList<>();
        languages.add("en_US");

        boolean gotRegistryValue = false;
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            // Try to read the user's Steam language setting from the registry
            String language = Utils.readWinRegistry("HKCU\\SOFTWARE\\Valve\\Steam\\Language");
            if (language != null) {
                gotRegistryValue = true;
                VRSettings.logger.info("Steam language setting: {}", language);
                if (!language.equals("english") && steamLanguages.containsKey(language)) {
                    languages.add(steamLanguages.get(language));
                }
            } else {
                VRSettings.logger.warn("Unable to read Steam language setting");
            }
        }

        if (!gotRegistryValue && !this.mc.options.languageCode.startsWith("en_")) {
            // Try to find a Steam language matching the user's in-game language selection
            String ucLanguageCode = this.mc.options.languageCode.substring(0, this.mc.options.languageCode.indexOf('_')) + this.mc.options.languageCode.substring(this.mc.options.languageCode.indexOf('_')).toUpperCase();
            if (steamLanguages.containsValue(ucLanguageCode)) {
                languages.add(ucLanguageCode);
            } else {
                Optional<String> langCode = steamLanguages.values().stream().filter(s -> ucLanguageCode.substring(0, ucLanguageCode.indexOf('_')).equals(s.substring(0, s.indexOf('_')))).findFirst();
                langCode.ifPresent(languages::add);
            }
        }

        List<Map<String, Object>> localeList = new ArrayList<>();
        for (String langCode : languages) {
            Map<String, Object> localeMap = new HashMap<>();

            // Load the language
            List<String> langs = new ArrayList<>();
            langs.add("en_us");
            if (!langCode.equals("en_US")) {
                langs.add(langCode.toLowerCase());
            }
            Language lang = ClientLanguage.loadFrom(this.mc.getResourceManager(), langs, false);

            for (VRInputAction action : sortedActions) {
                localeMap.put(action.name, lang.getOrDefault(action.keyBinding.getCategory()) + " - " + lang.getOrDefault(action.keyBinding.getName()));
            }

            for (VRInputActionSet actionSet : VRInputActionSet.values()) {
                localeMap.put(actionSet.name, lang.getOrDefault(actionSet.localizedName));
            }

            // We don't really care about localizing these
            localeMap.put(ACTION_LEFT_HAND, "Left Hand Pose");
            localeMap.put(ACTION_RIGHT_HAND, "Right Hand Pose");
            localeMap.put(ACTION_EXTERNAL_CAMERA, "External Camera");
            localeMap.put(ACTION_LEFT_HAPTIC, "Left Hand Haptic");
            localeMap.put(ACTION_RIGHT_HAPTIC, "Right Hand Haptic");

            localeMap.put("language_tag", steamLanguageWrongMappings.getOrDefault(langCode, langCode));
            localeList.add(localeMap);
        }
        map.put("localization", localeList);

        List<Map<String, Object>> defaults = new ArrayList<>();
        defaults.add(ImmutableMap.<String, Object>builder().put("controller_type", "vive_controller").put("binding_url", "vive_defaults.json").build());
        defaults.add(ImmutableMap.<String, Object>builder().put("controller_type", "oculus_touch").put("binding_url", "oculus_defaults.json").build());
        defaults.add(ImmutableMap.<String, Object>builder().put("controller_type", "holographic_controller").put("binding_url", "wmr_defaults.json").build());
        defaults.add(ImmutableMap.<String, Object>builder().put("controller_type", "knuckles").put("binding_url", "knuckles_defaults.json").build());
        defaults.add(ImmutableMap.<String, Object>builder().put("controller_type", "vive_cosmos_controller").put("binding_url", "cosmos_defaults.json").build());
        defaults.add(ImmutableMap.<String, Object>builder().put("controller_type", "vive_tracker_camera").put("binding_url", "tracker_defaults.json").build());
        map.put("default_bindings", defaults);

        try {
            (new File("openvr/input")).mkdirs();

            try (OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream("openvr/input/action_manifest.json"), StandardCharsets.UTF_8)) {
                this.GSON.toJson(map, outputstreamwriter);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to write action manifest", exception);
        }

        String rev = this.dh.vrSettings.reverseHands ? "_reversed" : "";
        Utils.loadAssetToFile("input/vive_defaults" + rev + ".json", new File("openvr/input/vive_defaults.json"), false);
        Utils.loadAssetToFile("input/oculus_defaults" + rev + ".json", new File("openvr/input/oculus_defaults.json"), false);
        Utils.loadAssetToFile("input/wmr_defaults" + rev + ".json", new File("openvr/input/wmr_defaults.json"), false);
        Utils.loadAssetToFile("input/knuckles_defaults" + rev + ".json", new File("openvr/input/knuckles_defaults.json"), false);
        Utils.loadAssetToFile("input/cosmos_defaults" + rev + ".json", new File("openvr/input/cosmos_defaults.json"), false);
        Utils.loadAssetToFile("input/tracker_defaults.json", new File("openvr/input/tracker_defaults.json"), false);
    }

    private long getActionHandle(String name) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longRef = stack.callocLong(1);
            int error = VRInput_GetActionHandle(name, longRef);

            if (error != 0) {
                throw new RuntimeException("Error getting action handle for '" + name + "': " + getInputErrorName(error));
            } else {
                return longRef.get(0);
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
            this.activeActionSetsBuffer = VRActiveActionSet.calloc(arraylist.size());
        } else if (this.activeActionSetsBuffer.capacity() != arraylist.size()) {
            this.activeActionSetsBuffer.free();
            this.activeActionSetsBuffer = VRActiveActionSet.calloc(arraylist.size());
        }

        for (int i = 0; i < arraylist.size(); ++i) {
            VRInputActionSet vrinputactionset = arraylist.get(i);
            this.activeActionSetsBuffer.get(i).set(this.getActionSetHandle(vrinputactionset), k_ulInvalidInputValueHandle, 0, 0);
        }

        return !arraylist.isEmpty();
    }

    @Override
    public Matrix4f getControllerComponentTransform(int controllerIndex, String componentName) {
        return this.controllerComponentTransforms != null &&
            this.controllerComponentTransforms.containsKey(componentName) &&
            this.controllerComponentTransforms.get(componentName)[controllerIndex] != null ?
            this.controllerComponentTransforms.get(componentName)[controllerIndex]
            : new Matrix4f();
    }

    private Matrix4f getControllerComponentTransformFromButton(int controllerIndex, long button) {
        return this.controllerComponentNames != null && this.controllerComponentNames.containsKey(button) ? this.getControllerComponentTransform(controllerIndex, this.controllerComponentNames.get(button)) : new Matrix4f();
    }

    private int getError() {
        return this.hmdErrorStore.get(0);
    }

    long getHapticHandle(ControllerType hand) {
        return hand == ControllerType.RIGHT ? this.rightHapticHandle : this.leftHapticHandle;
    }

    public String memUTF8NullTerminated(ByteBuffer buf) {
        return MemoryUtil.memUTF8(MemoryUtil.memAddress(buf));
    }

    protected float getSuperSampling() {
        return VRSettings_GetFloat("steamvr", "supersampleScale", this.hmdErrorStore);
    }


    private void getTransforms() {
        if (this.getXforms) {
            this.controllerComponentTransforms = new HashMap<>();
        }

        if (this.controllerComponentNames == null) {
            this.controllerComponentNames = new HashMap<>();
        }

        int count = VRRenderModels_GetRenderModelCount();

        // TODO get the controller-specific list
        List<String> componentNames = new ArrayList<>();
        componentNames.add("tip");
        // wmr doesn't define these...
        // componentNames.add("base");
        // componentNames.add("status");
        componentNames.add("handgrip");

        boolean failed = false;

        for (String component : componentNames) {
            this.controllerComponentTransforms.put(component, new Matrix4f[2]);

            for (int c = 0; c < 2; c++) {
                if (this.controllerDeviceIndex[c] == -1) {
                    failed = true;
                    continue;
                }
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    var stringBuffer = stack.calloc(32768);

                    VRSystem_GetStringTrackedDeviceProperty(this.controllerDeviceIndex[c], VR.ETrackedDeviceProperty_Prop_RenderModelName_String, stringBuffer, this.hmdErrorStore);

                    String renderModelName = memUTF8NullTerminated(stringBuffer);

                    VRSystem_GetStringTrackedDeviceProperty(this.controllerDeviceIndex[c], VR.ETrackedDeviceProperty_Prop_InputProfilePath_String, stringBuffer, this.hmdErrorStore);

                    String inputProfilePath = memUTF8NullTerminated(stringBuffer);
                    boolean isWMR = inputProfilePath.contains("holographic");
                    boolean isRifts = inputProfilePath.contains("rifts");

                    String componentName = component;
                    if (isWMR && component.equals("handgrip")) {
                        // I have no idea, Microsoft, none.
                        componentName = "body";
                    }

                    long button = VRRenderModels_GetComponentButtonMask(renderModelName, componentName);

                    if (button > 0L) {
                        // see now... wtf openvr, '0' is the system button, it cant also be the error value!
                        // (hint: it's a mask, not an index)
                        // u get 1 button per component, nothing more
                        this.controllerComponentNames.put(button, component);
                    }

                    long sourceHandle = c == RIGHT_CONTROLLER ? this.rightControllerHandle : this.leftControllerHandle;

                    if (sourceHandle == k_ulInvalidInputValueHandle) {
                        failed = true;
                        continue;
                    }

                    var renderModelComponentState = RenderModelComponentState.calloc(stack);
                    boolean valid = VRRenderModels_GetComponentStateForDevicePath(renderModelName, componentName, sourceHandle, RenderModelControllerModeState.calloc(stack), renderModelComponentState);

                    if (!valid) {
                        failed = true;
                        continue;
                    }

                    Matrix4f matrix4f = new Matrix4f();
                    OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(renderModelComponentState.mTrackingToComponentLocal(), matrix4f);
                    this.controllerComponentTransforms.get(component)[c] = matrix4f;

                    if (c == LEFT_CONTROLLER && isRifts && component.equals("handgrip")) {
                        // I have no idea, Valve, none.
                        this.controllerComponentTransforms.get(component)[LEFT_CONTROLLER] = this.controllerComponentTransforms.get(component)[RIGHT_CONTROLLER];
                    }

                    if (!failed && c == RIGHT_CONTROLLER) {
                        try {
                            Matrix4f tip = this.getControllerComponentTransform(RIGHT_CONTROLLER, "tip");
                            Matrix4f hand = this.getControllerComponentTransform(RIGHT_CONTROLLER, "handgrip");

                            Vector3 tipVec = tip.transform(forward);
                            Vector3 handVec = hand.transform(forward);

                            double dot = Math.abs(tipVec.normalized().dot(handVec.normalized()));

                            double angleRad = Math.acos(dot);
                            double angleDeg = Math.toDegrees(angleRad);

                            this.gunStyle = angleDeg > 10.0D;
                            this.gunAngle = angleDeg;
                        } catch (Exception exception) {
                            failed = true;
                        }
                    }
                }
            }

            this.getXforms = failed;
        }
    }

    private void initializeOpenVR() {
        int token = VR_InitInternal(this.hmdErrorStore, EVRApplicationType_VRApplication_Scene);

        if (!this.isError()) {
            OpenVR.create(token);
        }

        // check that the needed openvr stuff actually initialized, those can fail when using outdated steamvr
        if (OpenVR.VRApplications == null ||
            OpenVR.VRCompositor == null ||
            OpenVR.VRRenderModels == null ||
            OpenVR.VRSystem == null ||
            this.isError())
        {
            if (this.isError()) {
                throw new RuntimeException(VR_GetVRInitErrorAsEnglishDescription(this.getError()));
            } else {
                throw new RuntimeException(I18n.get("vivecraft.messages.outdatedsteamvr"));
            }
        }

        VRSettings.logger.info("OpenVR System Initialized OK.");
        this.poseMatrices = new Matrix4f[k_unMaxTrackedDeviceCount];

        for (int i = 0; i < this.poseMatrices.length; i++) {
            this.poseMatrices[i] = new Matrix4f();
        }

        this.initSuccess = true;
    }

    @Override
    public boolean postInit() throws RenderConfigException {
        //y is this called later, I forget.
        this.initInputAndApplication();
        return this.inputInitialized;
    }

    private void initInputAndApplication() throws RenderConfigException {
        this.populateInputActions();

        if (OpenVR.VRInput == null) return;

        // generate and submit bindings
        this.generateActionManifest();
        this.loadActionManifest();
        this.loadActionHandles();
        this.installApplicationManifest(false);
        this.inputInitialized = true;
    }

    private void initOpenVRCompositor() {
        VRCompositor_SetTrackingSpace(ETrackingUniverseOrigin_TrackingUniverseStanding);
        String actualTrackingSpace = switch(VRCompositor_GetTrackingSpace()) {
            case ETrackingUniverseOrigin_TrackingUniverseSeated -> "seated";
            case ETrackingUniverseOrigin_TrackingUniverseStanding -> "standing";
            case ETrackingUniverseOrigin_TrackingUniverseRawAndUncalibrated -> "raw uncalibrated";
            default -> "unknown";
        };
        VRSettings.logger.info("TrackingSpace: {}", actualTrackingSpace);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer stringBuffer = stack.calloc(20);

            VRSystem_GetStringTrackedDeviceProperty(k_unTrackedDeviceIndex_Hmd, ETrackedDeviceProperty_Prop_ManufacturerName_String, stringBuffer, this.hmdErrorStore);

            String deviceName = memUTF8NullTerminated(stringBuffer);
            VRSettings.logger.info("Device manufacturer is: {}", deviceName);
            this.detectedHardware = HardwareType.fromManufacturer(deviceName);
        }

        VRHotkeys.loadExternalCameraConfig();

        // texture bounds, currently unused, since we use the full texture anyway
        this.texBounds.uMax(1.0F);
        this.texBounds.uMin(0.0F);
        this.texBounds.vMax(1.0F);
        this.texBounds.vMin(0.0F);

        // set up texture types
        this.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.texType0.eType(VR.ETextureType_TextureType_OpenGL);
        this.texType0.handle(-1);

        this.texType1.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.texType1.eType(VR.ETextureType_TextureType_OpenGL);
        this.texType1.handle(-1);

        VRSettings.logger.info("OpenVR Compositor initialized OK.");
    }

    private void checkPathValid(String path, String knownError, boolean alwaysThrow) throws RenderConfigException {
        String pathFormatted = "";
        boolean hasInvalidChars = false;
        for (char c : path.toCharArray()) {
            if (c > 127) {
                hasInvalidChars = true;
                pathFormatted += "§c" + c + "§r";
            } else {
                pathFormatted += c;
            }
        }

        if (hasInvalidChars || alwaysThrow) {
            String error = knownError + (hasInvalidChars ? "\nInvalid characters in path: \n" : "\n");
            System.out.println(error + path);
            if (hasInvalidChars) {
                throw new RenderConfigException(knownError, Component.translatable("vivecraft.messages.steamvrInvalidCharacters", pathFormatted));
            } else {
                throw new RenderConfigException(knownError, Component.empty().append(error).append(pathFormatted));
            }
        }
    }


    private void installApplicationManifest(boolean force) throws RenderConfigException {
        File file = new File("openvr/vivecraft.vrmanifest");
        Utils.loadAssetToFile("vivecraft.vrmanifest", file, true);

        File customFile = new File("openvr/custom.vrmanifest");
        if (customFile.exists()) {
            file = customFile;
        }

        String appKey;

        try {
            Map<?, ?> map = (new Gson()).fromJson(new FileReader(file), Map.class);
            appKey = ((Map<?, ?>) ((List<?>) map.get("applications")).get(0)).get("app_key").toString();
        } catch (Exception e) {
            // TODO: should we abort here?
            VRSettings.logger.error("Error reading appkey from manifest: {}", e.getMessage());
            e.printStackTrace();
            return;
        }

        VRSettings.logger.info("Appkey: " + appKey);

        // check if path is valid always, since if the application was already installed, it will not check it again
        checkPathValid(file.getAbsolutePath(), "Failed to install application manifest", false);

        if (!force && VRApplications_IsApplicationInstalled(appKey)) {
            VRSettings.logger.info("Application manifest already installed");
        } else {
            int error = VRApplications_AddApplicationManifest(file.getAbsolutePath(), true);
            if (error != 0) {
                // application needs to be installed, so abort
                checkPathValid(file.getAbsolutePath(), "Failed to install application manifest: " + VRApplications_GetApplicationsErrorNameFromEnum(error), true);
            }

            VRSettings.logger.info("Application manifest installed successfully");
        }

        // OpenVR doc says pid = 0 will use the calling process, but it actually doesn't, so we
        // have to use this dumb hack that *probably* works on all relevant platforms.
        // TODO: could be replaced with ProcessHandle.current().pid()
        int pid;
        try {
            String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
            pid = Integer.parseInt(runtimeName.split("@")[0]);
        } catch (Exception e) {
            // TODO: should we abort here?
            VRSettings.logger.error("Error getting process id: {}", e.getMessage());
            e.printStackTrace();
            return;
        }

        int error = VRApplications_IdentifyApplication(pid, appKey);
        if (error != 0) {
            VRSettings.logger.error("Failed to identify application: {}", VRApplications_GetApplicationsErrorNameFromEnum(error));
        } else {
            VRSettings.logger.info("Application identified successfully");
        }
    }

    private void loadActionHandles() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longRef = stack.callocLong(1);

            for (VRInputAction action : this.inputActions.values()) {
                int error = VRInput_GetActionHandle(action.name, longRef);

                if (error != 0) {
                    throw new RuntimeException("Error getting action handle for '" + action.name + "': " + getInputErrorName(error));
                }

                action.setHandle(longRef.get(0));
            }

            this.leftPoseHandle = this.getActionHandle(ACTION_LEFT_HAND);
            this.rightPoseHandle = this.getActionHandle(ACTION_RIGHT_HAND);
            this.leftHapticHandle = this.getActionHandle(ACTION_LEFT_HAPTIC);
            this.rightHapticHandle = this.getActionHandle(ACTION_RIGHT_HAPTIC);
            this.externalCameraPoseHandle = this.getActionHandle(ACTION_EXTERNAL_CAMERA);

            for (VRInputActionSet actionSet : VRInputActionSet.values()) {
                int error = VRInput_GetActionSetHandle(actionSet.name, longRef);

                if (error != 0) {
                    throw new RuntimeException("Error getting action set handle for '" + actionSet.name + "': " + getInputErrorName(error));
                }

                this.actionSetHandles.put(actionSet, longRef.get(0));
            }

            this.leftControllerHandle = this.getInputSourceHandle("/user/hand/left");
            this.rightControllerHandle = this.getInputSourceHandle("/user/hand/right");
        }
    }

    private void loadActionManifest() throws RenderConfigException {
        String actionsPath = (new File("openvr/input/action_manifest.json")).getAbsolutePath();
        // check if path is valid for steamvr, since it would just silently fail
        checkPathValid(actionsPath, "Failed to install action manifest", false);
        int error = VRInput_SetActionManifestPath(actionsPath);

        if (error != 0) {
            throw new RenderConfigException("Failed to load action manifest", Component.literal(getInputErrorName(error)));
        }
    }

    private void processInputAction(VRInputAction action) {
        if (action.isActive() && action.isEnabledRaw()
            // try to prevent double left clicks
            && (!ClientDataHolderVR.getInstance().vrSettings.ingameBindingsInGui
            || !(action.actionSet == VRInputActionSet.INGAME && action.keyBinding.key.getType() == InputConstants.Type.MOUSE && action.keyBinding.key.getValue() == 0 && this.mc.screen != null))) {
            if (action.isButtonChanged()) {
                if (action.isButtonPressed() && action.isEnabled()) {
                    // We do this, so shit like closing a GUI by clicking a button won't
                    // also click in the world immediately after.
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

    private void processScrollInput(KeyMapping keyMapping, Runnable upCallback, Runnable downCallback) {
        VRInputAction action = this.getInputAction(keyMapping);

        if (action.isEnabled() && action.getLastOrigin() != k_ulInvalidInputValueHandle && action.getAxis2D(true).getY() != 0.0F) {
            float value = action.getAxis2D(false).getY();

            if (value > 0.0F) {
                upCallback.run();
            } else if (value < 0.0F) {
                downCallback.run();
            }
        }
    }

    private void processSwipeInput(KeyMapping keyMapping, Runnable leftCallback, Runnable rightCallback, Runnable upCallback, Runnable downCallback) {
        VRInputAction action = this.getInputAction(keyMapping);

        if (action.isEnabled() && action.getLastOrigin() != k_ulInvalidInputValueHandle) {
            ControllerType controller = this.findActiveBindingControllerType(keyMapping);

            if (controller != null) {
                if (!this.trackpadSwipeSamplers.containsKey(keyMapping.getName())) {
                    this.trackpadSwipeSamplers.put(keyMapping.getName(), new TrackpadSwipeSampler());
                }

                TrackpadSwipeSampler trackpadswipesampler = this.trackpadSwipeSamplers.get(keyMapping.getName());
                trackpadswipesampler.update(controller, action.getAxis2D(false));

                if (trackpadswipesampler.isSwipedUp() && upCallback != null) {
                    this.triggerHapticPulse(controller, 0.001F, 400.0F, 0.5F);
                    upCallback.run();
                }

                if (trackpadswipesampler.isSwipedDown() && downCallback != null) {
                    this.triggerHapticPulse(controller, 0.001F, 400.0F, 0.5F);
                    downCallback.run();
                }

                if (trackpadswipesampler.isSwipedLeft() && leftCallback != null) {
                    this.triggerHapticPulse(controller, 0.001F, 400.0F, 0.5F);
                    leftCallback.run();
                }

                if (trackpadswipesampler.isSwipedRight() && rightCallback != null) {
                    this.triggerHapticPulse(controller, 0.001F, 400.0F, 0.5F);
                    rightCallback.run();
                }


            }
        }
    }

    private void pollVREvents() {
        VREvent vrEvent = VREvent.calloc();
        while (VRSystem_PollNextEvent(vrEvent, VREvent.SIZEOF)) {
            this.vrEvents.add(vrEvent);
            vrEvent = VREvent.calloc();
        }
        // free the one that didn't get used
        vrEvent.free();
    }

    private void processVREvents() {
        while (!this.vrEvents.isEmpty()) {
            // try with, to free the event buffer after use
            try (VREvent event = this.vrEvents.poll()) {
                switch (event.eventType()) {
                    case EVREventType_VREvent_TrackedDeviceActivated:
                    case EVREventType_VREvent_TrackedDeviceDeactivated:
                    case EVREventType_VREvent_TrackedDeviceUpdated:
                    case EVREventType_VREvent_TrackedDeviceRoleChanged:
                    case EVREventType_VREvent_ModelSkinSettingsHaveChanged:
                        this.getXforms = true;
                        break;

                    case EVREventType_VREvent_Quit:
                        this.mc.stop();
                }
            }
        }
    }

    private void readOriginInfo(long inputValueHandle) {
        int error = VRInput_GetOriginTrackedDeviceInfo(inputValueHandle, this.originInfo, InputOriginInfo.SIZEOF);

        if (error != 0) {
            throw new RuntimeException("Error reading origin info: " + getInputErrorName(error));
        }
    }

    private void readPoseData(long actionHandle) {
        int error = VRInput_GetPoseActionDataForNextFrame(actionHandle, ETrackingUniverseOrigin_TrackingUniverseStanding, this.poseData, InputPoseActionData.SIZEOF, k_ulInvalidInputValueHandle);

        if (error != 0) {
            throw new RuntimeException("Error reading pose data: " + getInputErrorName(error));
        }
    }

    private void updateControllerPose(int controller, long actionHandle) {
        this.readPoseData(actionHandle);

        if (this.poseData.activeOrigin() != k_ulInvalidInputValueHandle) {
            this.readOriginInfo(this.poseData.activeOrigin());
            int deviceIndex = this.originInfo.trackedDeviceIndex();

            if (deviceIndex != this.controllerDeviceIndex[controller]) {
                this.getXforms = true;
            }

            this.controllerDeviceIndex[controller] = deviceIndex;

            if (deviceIndex != k_unTrackedDeviceIndexInvalid) {
                TrackedDevicePose pose = this.poseData.pose();

                if (pose.bPoseIsValid()) {
                    OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(pose.mDeviceToAbsoluteTracking(), this.poseMatrices[deviceIndex]);
                    HmdVector3 velocity = pose.vVelocity();
                    this.deviceVelocity[deviceIndex] = new Vec3(
                        velocity.v(0),
                        velocity.v(1),
                        velocity.v(2));
                    Utils.Matrix4fCopy(this.poseMatrices[deviceIndex], this.controllerPose[controller]);
                    this.controllerTracking[controller] = true;
                    // controller is tracking, don't execute the code below
                    return;
                }
            }
        } else {
            this.controllerDeviceIndex[controller] = k_unTrackedDeviceIndexInvalid;
        }

        // not tracking
        this.controllerTracking[controller] = false;
    }

    private void updatePose() {
        int error = VRCompositor_WaitGetPoses(this.hmdTrackedDevicePoses, null);

        if (error > 0) {
            VRSettings.logger.error("Compositor Error: GetPoseError {}", OpenVRStereoRenderer.getCompositorError(error));
        }

        if (error == EVRCompositorError_VRCompositorError_DoNotHaveFocus) {
            // this is so dumb, but it works
            // apparently there is/was a bug, that needed that to make the haptics work
            this.triggerHapticPulse(0, 500);
            this.triggerHapticPulse(1, 500);
        }

        if (this.getXforms) {
            // set to null by events.
            // do we want the dynamic info? I don't think so...
            this.getTransforms();
        } else if (this.dbg) {
            this.dbg = false;
            this.debugOut(k_unTrackedDeviceIndex_Hmd);
            this.debugOut(this.controllerDeviceIndex[RIGHT_CONTROLLER]);
            this.debugOut(this.controllerDeviceIndex[LEFT_CONTROLLER]);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            HmdMatrix34 temp = HmdMatrix34.calloc(stack);
            OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(VRSystem_GetEyeToHeadTransform(EVREye_Eye_Left, temp), this.hmdPoseLeftEye);
            OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(VRSystem_GetEyeToHeadTransform(EVREye_Eye_Right, temp), this.hmdPoseRightEye);
        }

        for (int device = 0; device < k_unMaxTrackedDeviceCount; device++) {
            if (this.hmdTrackedDevicePoses.get(device).bPoseIsValid()) {
                OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(this.hmdTrackedDevicePoses.get(device).mDeviceToAbsoluteTracking(), this.poseMatrices[device]);
                HmdVector3 velocity = this.hmdTrackedDevicePoses.get(device).vVelocity();
                this.deviceVelocity[device] = new Vec3(
                    velocity.v(0),
                    velocity.v(1),
                    velocity.v(2));
            }
        }

        if (this.hmdTrackedDevicePoses.get(k_unTrackedDeviceIndex_Hmd).bPoseIsValid()) {
            Utils.Matrix4fCopy(this.poseMatrices[k_unTrackedDeviceIndex_Hmd], this.hmdPose);
            this.headIsTracking = true;
        } else {
            this.headIsTracking = false;
            this.hmdPose.SetIdentity();
            this.hmdPose.M[1][3] = 1.62F;
        }

        // Gotta do this here so we can get the poses
        if (this.inputInitialized) {
            this.mc.getProfiler().push("updateActionState");

            if (this.updateActiveActionSets()) {
                int updateError = VRInput_UpdateActionState(this.activeActionSetsBuffer, VRActiveActionSet.SIZEOF);

                if (updateError != 0) {
                    throw new RuntimeException("Error updating action state: code " + getInputErrorName(updateError));
                }
            }

            this.inputActions.values().forEach(this::readNewData);

            this.mc.getProfiler().pop();

            if (this.dh.vrSettings.reverseHands) {
                this.updateControllerPose(RIGHT_CONTROLLER, this.leftPoseHandle);
                this.updateControllerPose(LEFT_CONTROLLER, this.rightPoseHandle);
            } else {
                this.updateControllerPose(RIGHT_CONTROLLER, this.rightPoseHandle);
                this.updateControllerPose(LEFT_CONTROLLER, this.leftPoseHandle);
            }

            this.updateControllerPose(CAMERA_TRACKER, this.externalCameraPoseHandle);
        }

        this.updateAim();
    }

    private long getActionSetHandle(VRInputActionSet actionSet) {
        return this.actionSetHandles.get(actionSet);
    }

    private long getControllerHandle(ControllerType hand) {
        if (this.dh.vrSettings.reverseHands) {
            return hand == ControllerType.RIGHT ? this.leftControllerHandle : this.rightControllerHandle;
        } else {
            return hand == ControllerType.RIGHT ? this.rightControllerHandle : this.leftControllerHandle;
        }
    }

    private long getInputSourceHandle(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longRef = stack.callocLong(1);
            int error = VRInput_GetInputSourceHandle(path, longRef);

            if (error != 0) {
                throw new RuntimeException("Error getting input source handle for '" + path + "': " + getInputErrorName(error));
            } else {
                return longRef.get(0);
            }
        }
    }

    protected ControllerType getOriginControllerType(long inputValueHandle) {
        if (inputValueHandle != k_ulInvalidInputValueHandle) {
            this.readOriginInfo(inputValueHandle);

            if (this.originInfo.trackedDeviceIndex() != k_unTrackedDeviceIndexInvalid) {
                if (this.originInfo.trackedDeviceIndex() == this.controllerDeviceIndex[RIGHT_CONTROLLER]) {
                    return ControllerType.RIGHT;
                } else if (this.originInfo.trackedDeviceIndex() == this.controllerDeviceIndex[LEFT_CONTROLLER]) {
                    return ControllerType.LEFT;
                }
            }
        }
        return null;
    }

    private void readNewData(VRInputAction action) {
        switch (action.type) {
            case "boolean" -> {
                if (action.isHanded()) {
                    for (ControllerType type : ControllerType.values()) {
                        this.readDigitalData(action, type);
                    }
                } else {
                    this.readDigitalData(action, null);
                }
            }

            case "vector1", "vector2", "vector3" -> {
                if (action.isHanded()) {
                    for (ControllerType type : ControllerType.values()) {
                        this.readAnalogData(action, type);
                    }
                } else {
                    this.readAnalogData(action, null);
                }
            }
        }
    }

    private void readDigitalData(VRInputAction action, ControllerType hand) {
        int index = hand != null ? hand.ordinal() : RIGHT_CONTROLLER;

        int error = VRInput_GetDigitalActionData(action.handle, this.digital, InputDigitalActionData.SIZEOF, hand != null ? this.getControllerHandle(hand) : k_ulInvalidInputValueHandle);

        if (error != 0) {
            throw new RuntimeException("Error reading digital data for '" + action.name + "': " + getInputErrorName(error));
        } else {
            action.digitalData[index].activeOrigin = this.digital.activeOrigin();
            action.digitalData[index].isActive = this.digital.bActive();
            action.digitalData[index].state = this.digital.bState();
            action.digitalData[index].isChanged = this.digital.bChanged();
        }
    }

    private void readAnalogData(VRInputAction action, ControllerType hand) {
        int index = hand != null ? hand.ordinal() : RIGHT_CONTROLLER;

        int error = VRInput_GetAnalogActionData(action.handle, this.analog, InputAnalogActionData.SIZEOF, hand != null ? this.getControllerHandle(hand) : k_ulInvalidInputValueHandle);

        if (error != 0) {
            throw new RuntimeException("Error reading analog data for '" + action.name + "': " + getInputErrorName(error));
        } else {
            action.analogData[index].x = this.analog.x();
            action.analogData[index].y = this.analog.y();
            action.analogData[index].z = this.analog.z();

            action.analogData[index].deltaX = this.analog.deltaX();
            action.analogData[index].deltaY = this.analog.deltaY();
            action.analogData[index].deltaZ = this.analog.deltaZ();

            action.analogData[index].activeOrigin = this.analog.activeOrigin();
            action.analogData[index].isActive = this.analog.bActive();
        }
    }

    @Override
    public boolean hasCameraTracker() {
        return this.controllerDeviceIndex[CAMERA_TRACKER] != -1;
    }

    @Override
    public List<Long> getOrigins(VRInputAction action) {
        List<Long> list = new ArrayList<>();
        if (OpenVR.VRInput != null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                LongBuffer longRef = stack.callocLong(16);
                int error = VRInput_GetActionOrigins(this.getActionSetHandle(action.actionSet), action.handle, longRef);

                if (error != 0) {
                    throw new RuntimeException(
                        "Error getting action origins for '" + action.name + "': " + getInputErrorName(error));
                } else {

                    while (longRef.remaining() > 0) {
                        long handle = longRef.get();
                        if (handle != k_ulInvalidActionHandle) {
                            list.add(handle);
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public String getOriginName(long origin) {
        if (OpenVR.VRInput == null) {
            return "";
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer str = stack.calloc(32768);
                // omit controller type
                int error = VRInput_GetOriginLocalizedName(origin, str,
                    EVRInputStringBits_VRInputString_Hand | EVRInputStringBits_VRInputString_InputSource);

                if (error != 0) {
                    throw new RuntimeException("Error getting origin name: " + getInputErrorName(error));
                } else {
                    return memUTF8NullTerminated(str);
                }
            }
        }
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new OpenVRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        int activityLevel = VRSystem_GetTrackedDeviceActivityLevel(k_unTrackedDeviceIndex_Hmd);
        return activityLevel == EDeviceActivityLevel_k_EDeviceActivityLevel_UserInteraction || activityLevel == EDeviceActivityLevel_k_EDeviceActivityLevel_UserInteraction_Timeout;
    }

    @Override
    public float getIPD() {
        return VRSystem_GetFloatTrackedDeviceProperty(k_unTrackedDeviceIndex_Hmd, ETrackedDeviceProperty_Prop_UserIpdMeters_Float, this.hmdErrorStore);
    }

    /**
     * this should query the actual name from the runtime, but openvr doesn't seem to have an api for that
     */
    @Override
    public String getRuntimeName() {
        return "SteamVR";
    }
}
