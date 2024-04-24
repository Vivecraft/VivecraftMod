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
    public static final int THIRD_CONTROLLER = 2;
    protected static MCOpenVR ome;
    private final String ACTION_EXTERNAL_CAMERA = "/actions/mixedreality/in/externalcamera";
    private final String ACTION_LEFT_HAND = "/actions/global/in/lefthand";
    private final String ACTION_LEFT_HAPTIC = "/actions/global/out/lefthaptic";
    private final String ACTION_RIGHT_HAND = "/actions/global/in/righthand";
    private final String ACTION_RIGHT_HAPTIC = "/actions/global/out/righthaptic";
    private final Map<VRInputActionSet, Long> actionSetHandles = new EnumMap<>(VRInputActionSet.class);
    private VRActiveActionSet.Buffer activeActionSetsBuffer;
    private Map<Long, String> controllerComponentNames;
    private Map<String, Matrix4f[]> controllerComponentTransforms;
    private boolean dbg = true;
    private long externalCameraPoseHandle;
    private final int[] controllerDeviceIndex = new int[3];
    private boolean getXforms = true;
    private final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private final IntBuffer hmdErrorStore = MemoryUtil.memCallocInt(1);
    private IntBuffer hmdErrorStoreBuf;
    private TrackedDevicePose.Buffer hmdTrackedDevicePoses;
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
    private final VRTextureBounds texBounds = VRTextureBounds.calloc();
    private final Map<String, TrackpadSwipeSampler> trackpadSwipeSamplers = new HashMap<>();
    private boolean tried;
    private final Queue<VREvent> vrEvents = new LinkedList<>();
    final Texture texType0 = Texture.calloc();
    final Texture texType1 = Texture.calloc();
    InputDigitalActionData digital = InputDigitalActionData.calloc();
    InputAnalogActionData analog = InputAnalogActionData.calloc();

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

    public MCOpenVR(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh, VivecraftVRMod.INSTANCE);
        ome = this;
        this.hapticScheduler = new OpenVRHapticScheduler();

        for (int i = 0; i < 3; ++i) {
            this.controllerDeviceIndex[i] = -1;
        }

        this.poseData = InputPoseActionData.calloc();
        this.originInfo = InputOriginInfo.calloc();
    }

    public static MCOpenVR get() {
        return ome;
    }

    static String getInputErrorName(int code) {
        switch (code) {
            case 0:
                return "wat";

            case 1:
                return "NameNotFound";

            case 2:
                return "WrongType";

            case 3:
                return "InvalidHandle";

            case 4:
                return "InvalidParam";

            case 5:
                return "NoSteam";

            case 6:
                return "MaxCapacityReached";

            case 7:
                return "IPCError";

            case 8:
                return "NoActiveActionSet";

            case 9:
                return "InvalidDevice";

            case 10:
                return "InvalidSkeleton";

            case 11:
                return "InvalidBoneCount";

            case 12:
                return "InvalidCompressedData";

            case 13:
                return "NoData";

            case 14:
                return "BufferTooSmall";

            case 15:
                return "MismatchedActionManifest";

            case 16:
                return "MissingSkeletonData";

            case 17:
                return "InvalidBoneIndex";

            default:
                return "Unknown";
        }
    }

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
    }

    public String getID() {
        return "openvr_lwjgl";
    }

    public String getName() {
        return "OpenVR_LWJGL";
    }

    public Vector2f getPlayAreaSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (OpenVR.VRChaperone != null && OpenVR.VRChaperone.GetPlayAreaSize != 0) {
                FloatBuffer pSizeZ = stack.callocFloat(1);
                FloatBuffer pSizeX = stack.callocFloat(1);
                boolean b0 = VRChaperone.VRChaperone_GetPlayAreaSize(pSizeX, pSizeZ);
                return b0 ? new Vector2f(pSizeX.get(0) * this.dh.vrSettings.walkMultiplier, pSizeZ.get(0) * this.dh.vrSettings.walkMultiplier) : null;
            } else {
                return null;
            }
        }
    }

    public boolean init() {
        if (this.initialized) {
            return true;
        } else if (this.tried) {
            return this.initialized;
        } else {
            this.tried = true;
            this.mc = Minecraft.getInstance();
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
                System.out.println("Controller input not available. Forcing seated mode.");
                this.dh.vrSettings.seated = true;
            }

            System.out.println("OpenVR initialized & VR connected.");
            this.deviceVelocity = new Vec3[64];

            for (int i = 0; i < this.poseMatrices.length; ++i) {
                this.poseMatrices[i] = new Matrix4f();
                this.deviceVelocity[i] = new Vec3(0.0D, 0.0D, 0.0D);
            }

            this.initialized = true;

            if (ClientDataHolderVR.katvr) {
                try {
                    System.out.println("Waiting for KATVR....");
                    Utils.unpackNatives("katvr");
                    NativeLibrary.addSearchPath("WalkerBase.dll", (new File("openvr/katvr")).getAbsolutePath());
                    jkatvr.Init(1);
                    jkatvr.Launch();

                    if (jkatvr.CheckForLaunch()) {
                        System.out.println("KATVR Loaded");
                    } else {
                        System.out.println("KATVR Failed to load");
                    }
                } catch (Exception exception1) {
                    System.out.println("KATVR crashed: " + exception1.getMessage());
                }
            }

            if (ClientDataHolderVR.infinadeck) {
                try {
                    System.out.println("Waiting for Infinadeck....");
                    Utils.unpackNatives("infinadeck");
                    NativeLibrary.addSearchPath("InfinadeckAPI.dll", (new File("openvr/infinadeck")).getAbsolutePath());

                    if (jinfinadeck.InitConnection()) {
                        jinfinadeck.CheckConnection();
                        System.out.println("Infinadeck Loaded");
                    } else {
                        System.out.println("Infinadeck Failed to load");
                    }
                } catch (Exception exception) {
                    System.out.println("Infinadeck crashed: " + exception.getMessage());
                }
            }

            return true;
        }
    }

    public void poll(long frameIndex) {
        if (this.initialized) {
            this.paused = VRSystem_ShouldApplicationPause();
            this.mc.getProfiler().push("events");
            this.pollVREvents();

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
    }

    public void processInputs() {
        if (!this.dh.vrSettings.seated && !ClientDataHolderVR.viewonly && this.inputInitialized) {
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

            this.processScrollInput(GuiHandler.keyScrollAxis, () ->
            {
                InputSimulator.scrollMouse(0.0D, 1.0D);
            }, () ->
            {
                InputSimulator.scrollMouse(0.0D, -1.0D);
            });
            this.processScrollInput(VivecraftVRMod.INSTANCE.keyHotbarScroll, () ->
            {
                this.changeHotbar(-1);
            }, () ->
            {
                this.changeHotbar(1);
            });
            this.processSwipeInput(VivecraftVRMod.INSTANCE.keyHotbarSwipeX, () ->
            {
                this.changeHotbar(1);
            }, () ->
            {
                this.changeHotbar(-1);
            }, null, null);
            this.processSwipeInput(VivecraftVRMod.INSTANCE.keyHotbarSwipeY, null, null, () ->
            {
                this.changeHotbar(-1);
            }, () ->
            {
                this.changeHotbar(1);
            });
            this.ignorePressesNextFrame = false;
        }
    }

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
//        System.out.println("******************* VR DEVICE: " + deviceindex + " *************************");
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
//                System.out.println(s1.replace("ETrackedDeviceProperty_Prop_", ""));
//            } catch (IllegalAccessException illegalaccessexception) {
//                illegalaccessexception.printStackTrace();
//            }
//        }
//
//        System.out.println("******************* END VR DEVICE: " + deviceindex + " *************************");
    }

    protected ControllerType findActiveBindingControllerType(KeyMapping binding) {
        if (!this.inputInitialized) {
            return null;
        } else {
            long i = this.getInputAction(binding).getLastOrigin();
            return i != 0L ? this.getOriginControllerType(i) : null;
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
        List<VRInputAction> sortedActions = new ArrayList<>(this.inputActions.values());
        sortedActions.sort(Comparator.comparing((action) ->
        {
            return action.keyBinding;
        }));
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

        if (!gotRegistryValue && !mc.options.languageCode.startsWith("en_")) {
            // Try to find a Steam language matching the user's in-game language selection
            String ucLanguageCode = mc.options.languageCode.substring(0, mc.options.languageCode.indexOf('_')) + mc.options.languageCode.substring(mc.options.languageCode.indexOf('_')).toUpperCase();
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
            Language lang = ClientLanguage.loadFrom(mc.getResourceManager(), langs, false);

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
            LongBuffer longbyreference = stack.callocLong(1);
            int i = VRInput.VRInput_GetActionHandle(name, longbyreference);

            if (i != 0) {
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

        if (activeActionSetsBuffer == null) {
            activeActionSetsBuffer = VRActiveActionSet.calloc(arraylist.size());
        } else if (activeActionSetsBuffer.capacity() != arraylist.size()) {
            activeActionSetsBuffer.close();
            activeActionSetsBuffer = VRActiveActionSet.calloc(arraylist.size());
        }

        for (int i = 0; i < arraylist.size(); ++i) {
            VRInputActionSet vrinputactionset = arraylist.get(i);
            activeActionSetsBuffer.get(i).set(this.getActionSetHandle(vrinputactionset), 0L, 0, 0);
        }

        return !arraylist.isEmpty();
    }

    public Matrix4f getControllerComponentTransform(int controllerIndex, String componenetName) {
        return this.controllerComponentTransforms != null && this.controllerComponentTransforms.containsKey(componenetName) && ((Matrix4f[]) this.controllerComponentTransforms.get(componenetName))[controllerIndex] != null ? (this.controllerComponentTransforms.get(componenetName))[controllerIndex] : Utils.Matrix4fSetIdentity(new Matrix4f());
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

    public String getOriginName(long handle) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer str = stack.calloc(32768);
            // omit controller type
            int i = VRInput_GetOriginLocalizedName(handle, str, 5);

            if (i != 0) {
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

            int i = VRRenderModels_GetRenderModelCount();
            List<String> list = new ArrayList<>();
            list.add("tip");
            list.add("handgrip");
            boolean flag = false;

            for (String s : list) {
                this.controllerComponentTransforms.put(s, new Matrix4f[2]);

                for (int j = 0; j < 2; ++j) {
                    if (this.controllerDeviceIndex[j] == -1) {
                        flag = true;
                    } else {
                        try (MemoryStack stack = MemoryStack.stackPush()) {
                            var stringBuffer = stack.calloc(32768);
                            VRSystem_GetStringTrackedDeviceProperty(this.controllerDeviceIndex[j], VR.ETrackedDeviceProperty_Prop_RenderModelName_String, stringBuffer, this.hmdErrorStore);
                            String renderModelName = memUTF8NullTerminated(stringBuffer);
                            VRSystem_GetStringTrackedDeviceProperty(controllerDeviceIndex[j], VR.ETrackedDeviceProperty_Prop_InputProfilePath_String, stringBuffer, this.hmdErrorStore);
                            String inputProfilePath = memUTF8NullTerminated(stringBuffer);
                            boolean flag1 = inputProfilePath.contains("holographic");
                            boolean flag2 = inputProfilePath.contains("rifts");

                            var componentName = s;
                            if (flag1 && s.equals("handgrip")) {
                                componentName = "body";
                            }

                            long k = VRRenderModels_GetComponentButtonMask(renderModelName, componentName);

                            if (k > 0L) {
                                this.controllerComponentNames.put(k, s);
                            }

                            long l = j == 0 ? this.rightControllerHandle : this.leftControllerHandle;

                            if (l == 0L) {
                                flag = true;
                            } else {
                                var renderModelComponentState = RenderModelComponentState.calloc(stack);
                                boolean b0 = VRRenderModels_GetComponentStateForDevicePath(renderModelName, componentName, l, RenderModelControllerModeState.calloc(stack), renderModelComponentState);

                                if (!b0) {
                                    flag = true;
                                } else {
                                    Matrix4f matrix4f = new Matrix4f();
                                    OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(renderModelComponentState.mTrackingToComponentLocal(), matrix4f);
                                    (this.controllerComponentTransforms.get(s))[j] = matrix4f;

                                    if (j == 1 && flag2 && s.equals("handgrip")) {
                                        (this.controllerComponentTransforms.get(s))[1] = (this.controllerComponentTransforms.get(s))[0];
                                    }

                                    if (!flag && j == 0) {
                                        try {
                                            Matrix4f matrix4f1 = this.getControllerComponentTransform(0, "tip");
                                            Matrix4f matrix4f2 = this.getControllerComponentTransform(0, "handgrip");
                                            Vector3 vector3 = matrix4f1.transform(this.forward);
                                            Vector3 vector31 = matrix4f2.transform(this.forward);
                                            double d0 = Math.abs(vector3.normalized().dot(vector31.normalized()));
                                            double d1 = Math.acos(d0);
                                            double d2 = Math.toDegrees(d1);
                                            double d3 = Math.acos(vector3.normalized().dot(this.forward.normalized()));
                                            double d4 = Math.toDegrees(d3);
                                            this.gunStyle = d2 > 10.0D;
                                            this.gunAngle = d2;
                                        } catch (Exception exception) {
                                            flag = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                this.getXforms = flag;
            }
        }
    }

    private void initializeOpenVR() {
        this.hmdErrorStoreBuf = MemoryUtil.memCallocInt(1);
        int token = VR.VR_InitInternal(this.hmdErrorStoreBuf, 1);

        if (!this.isError()) {
            OpenVR.create(token);
        }

        // check that the needed openvr stuff actually initialized, those can fail when using outdated steamvr
        if (OpenVR.VRApplications == null ||
            OpenVR.VRCompositor == null ||
            OpenVR.VRInput == null ||
            OpenVR.VRRenderModels == null ||
            OpenVR.VRSystem == null ||
            this.isError())
        {
            if (this.isError()) {
                throw new RuntimeException(VR.VR_GetVRInitErrorAsEnglishDescription(this.getError()));
            } else {
                throw new RuntimeException(I18n.get("vivecraft.messages.outdatedsteamvr"));
            }
        }

        VRSettings.logger.info("OpenVR System Initialized OK.");
        this.hmdTrackedDevicePoses = TrackedDevicePose.calloc(64);
        this.poseMatrices = new Matrix4f[64];

        for (int i = 0; i < this.poseMatrices.length; ++i) {
            this.poseMatrices[i] = new Matrix4f();
        }

        this.initSuccess = true;
    }

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
//            System.out.println("OpenComposite initialized.");
//        } else {
//            System.out.println("OpenComposite not found: " + VR.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
//            this.vrOpenComposite = null;
//        }
//    }

    private void initOpenVRCompositor() {
        VRCompositor_SetTrackingSpace(1);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var pointer = stack.calloc(20);
            VRSettings.logger.info("TrackingSpace: {}", VRCompositor_GetTrackingSpace());
            VRSystem_GetStringTrackedDeviceProperty(0, 1005, pointer, this.hmdErrorStore);
            String s = memUTF8NullTerminated(pointer);
            VRSettings.logger.info("Device manufacturer is: {}", s);
            this.detectedHardware = HardwareType.fromManufacturer(s);
        }
        this.dh.vrSettings.loadOptions();
        VRHotkeys.loadExternalCameraConfig();

        this.texBounds.uMax(1.0F);
        this.texBounds.uMin(0.0F);
        this.texBounds.vMax(1.0F);
        this.texBounds.vMin(0.0F);
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
            throw new RenderConfigException(knownError, Component.empty().append(error).append(pathFormatted));
        }
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
                System.out.println("Error reading appkey from manifest");
                exception1.printStackTrace();
                return;
            }

            System.out.println("Appkey: " + s);

            // check if path is valid always, since if the application was already installed, it will not check it again
            checkPathValid(file1.getAbsolutePath(), "Failed to install application manifest", false);

            if (!force && VRApplications_IsApplicationInstalled(s)) {
                System.out.println("Application manifest already installed");
            } else {
                int i = VRApplications_AddApplicationManifest(file1.getAbsolutePath(), true);

                if (i != 0) {
                    // application needs to be installed, so abort
                    checkPathValid(file1.getAbsolutePath(), "Failed to install application manifest: " + VRApplications_GetApplicationsErrorNameFromEnum(i), true);
                }

                System.out.println("Application manifest installed successfully");
            }

            int j;

            try {
                String s1 = ManagementFactory.getRuntimeMXBean().getName();
                j = Integer.parseInt(s1.split("@")[0]);
            } catch (Exception exception) {
                System.out.println("Error getting process id");
                exception.printStackTrace();
                return;
            }

            int k = VRApplications_IdentifyApplication(j, s);

            if (k != 0) {
                System.out.println("Failed to identify application: " + VRApplications_GetApplicationsErrorNameFromEnum(k));
            } else {
                System.out.println("Application identified successfully");
            }
        }
    }

    private void loadActionHandles() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longbyreference = stack.callocLong(1);

            for (VRInputAction vrinputaction : this.inputActions.values()) {
                int i = VRInput_GetActionHandle(vrinputaction.name, longbyreference);

                if (i != 0) {
                    throw new RuntimeException("Error getting action handle for '" + vrinputaction.name + "': " + getInputErrorName(i));
                }

                vrinputaction.setHandle(longbyreference.get(0));
            }

            this.leftPoseHandle = this.getActionHandle(ACTION_LEFT_HAND);
            this.rightPoseHandle = this.getActionHandle(ACTION_RIGHT_HAND);
            this.leftHapticHandle = this.getActionHandle(ACTION_LEFT_HAPTIC);
            this.rightHapticHandle = this.getActionHandle(ACTION_RIGHT_HAPTIC);
            this.externalCameraPoseHandle = this.getActionHandle(ACTION_EXTERNAL_CAMERA);

            for (VRInputActionSet vrinputactionset : VRInputActionSet.values()) {
                int j = VRInput_GetActionSetHandle(vrinputactionset.name, longbyreference);

                if (j != 0) {
                    throw new RuntimeException("Error getting action set handle for '" + vrinputactionset.name + "': " + getInputErrorName(j));
                }

                this.actionSetHandles.put(vrinputactionset, longbyreference.get(0));
            }

            this.leftControllerHandle = this.getInputSourceHandle("/user/hand/left");
            this.rightControllerHandle = this.getInputSourceHandle("/user/hand/right");
        }
    }

    private void loadActionManifest() throws RenderConfigException {
        String actionsPath = (new File("openvr/input/action_manifest.json")).getAbsolutePath();
        // check if path is valid for steamvr, since it would just silently fail
        checkPathValid(actionsPath, "Failed to install action manifest", false);
        int i = VRInput_SetActionManifestPath(actionsPath);

        if (i != 0) {
            throw new RenderConfigException("Failed to load action manifest", Component.literal(getInputErrorName(i)));
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
            && (!ClientDataHolderVR.getInstance().vrSettings.ingameBindingsInGui
            || !(action.actionSet == VRInputActionSet.INGAME && action.keyBinding.key.getType() == InputConstants.Type.MOUSE && action.keyBinding.key.getValue() == 0 && mc.screen != null))) {
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

        if (vrinputaction.isEnabled() && vrinputaction.getLastOrigin() != 0L && vrinputaction.getAxis2D(true).getY() != 0.0F) {
            float f = vrinputaction.getAxis2D(false).getY();

            if (f > 0.0F) {
                upCallback.run();
            } else if (f < 0.0F) {
                downCallback.run();
            }
        }
    }

    private void processSwipeInput(KeyMapping keyBinding, Runnable leftCallback, Runnable rightCallback, Runnable upCallback, Runnable downCallback) {
        VRInputAction vrinputaction = this.getInputAction(keyBinding);

        if (vrinputaction.isEnabled() && vrinputaction.getLastOrigin() != 0L) {
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
                case 100:
                case 101:
                case 102:
                case 108:
                case 853:
                    this.getXforms = true;
                    break;

                case 700:
                    this.mc.stop();
            }
        }
    }

    private void readOriginInfo(long inputValueHandle) {
        int i = VRInput_GetOriginTrackedDeviceInfo(inputValueHandle, this.originInfo, InputOriginInfo.SIZEOF);

        if (i != 0) {
            throw new RuntimeException("Error reading origin info: " + getInputErrorName(i));
        }
    }

    private void readPoseData(long actionHandle) {
        int i = VRInput_GetPoseActionDataForNextFrame(actionHandle, 1, this.poseData, InputPoseActionData.SIZEOF, 0L);

        if (i != 0) {
            throw new RuntimeException("Error reading pose data: " + getInputErrorName(i));
        }
    }

    private void updateControllerPose(int controller, long actionHandle) {
        if (this.TPose) {
            if (controller == 0) {
                Utils.Matrix4fCopy(this.TPose_Right, this.controllerPose[controller]);
            } else if (controller == 1) {
                Utils.Matrix4fCopy(this.TPose_Left, this.controllerPose[controller]);
            }

            this.controllerTracking[controller] = true;
        } else {
            this.readPoseData(actionHandle);

            if (this.poseData.activeOrigin() != 0L) {
                this.readOriginInfo(this.poseData.activeOrigin());
                int i = this.originInfo.trackedDeviceIndex();

                if (i != this.controllerDeviceIndex[controller]) {
                    this.getXforms = true;
                }

                this.controllerDeviceIndex[controller] = i;

                if (i != -1) {
                    TrackedDevicePose trackeddevicepose = this.poseData.pose();

                    if (trackeddevicepose.bPoseIsValid()) {
                        OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(trackeddevicepose.mDeviceToAbsoluteTracking(), this.poseMatrices[i]);
                        this.deviceVelocity[i] = new Vec3(trackeddevicepose.vVelocity().v(0), trackeddevicepose.vVelocity().v(1), trackeddevicepose.vVelocity().v(2));
                        Utils.Matrix4fCopy(this.poseMatrices[i], this.controllerPose[controller]);
                        this.controllerTracking[controller] = true;
                        return;
                    }
                }
            } else {
                this.controllerDeviceIndex[controller] = -1;
            }

            this.controllerTracking[controller] = false;
        }
    }

    private void updatePose() {
        if (OpenVR.VRSystem != null && OpenVR.VRSystem != null) {
            int i = VRCompositor_WaitGetPoses(this.hmdTrackedDevicePoses, null);

            if (i > 0) {
                System.out.println("Compositor Error: GetPoseError " + OpenVRStereoRenderer.getCompostiorError(i));
            }

            if (i == 101) {
                this.triggerHapticPulse(0, 500);
                this.triggerHapticPulse(1, 500);
            }

            if (this.getXforms) {
                this.getTransforms();
            } else if (this.dbg) {
                this.dbg = false;
                this.debugOut(0);
                this.debugOut(this.controllerDeviceIndex[0]);
                this.debugOut(this.controllerDeviceIndex[1]);
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                var hmdmatrix34 = HmdMatrix34.calloc(stack);
                OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(VRSystem_GetEyeToHeadTransform(0, hmdmatrix34), this.hmdPoseLeftEye);
                OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(VRSystem_GetEyeToHeadTransform(1, hmdmatrix34), this.hmdPoseRightEye);
            }

            for (int j = 0; j < 64; ++j) {

                if (this.hmdTrackedDevicePoses.get(j).bPoseIsValid()) {
                    OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(this.hmdTrackedDevicePoses.get(j).mDeviceToAbsoluteTracking(), this.poseMatrices[j]);
                    this.deviceVelocity[j] = new Vec3(this.hmdTrackedDevicePoses.get(j).vVelocity().v(0), this.hmdTrackedDevicePoses.get(j).vVelocity().v(1), this.hmdTrackedDevicePoses.get(j).vVelocity().v(2));
                }
            }

            if (this.hmdTrackedDevicePoses.get(0).bPoseIsValid()) {
                Utils.Matrix4fCopy(this.poseMatrices[0], this.hmdPose);
                this.headIsTracking = true;
            } else {
                this.headIsTracking = false;
                Utils.Matrix4fSetIdentity(this.hmdPose);
                this.hmdPose.M[1][3] = 1.62F;
            }

            this.TPose = false;

            if (this.TPose) {
                this.TPose_Right.M[0][3] = 0.0F;
                this.TPose_Right.M[1][3] = 0.0F;
                this.TPose_Right.M[2][3] = 0.0F;
                Matrix4f matrix4f = this.TPose_Right;
                Utils.Matrix4fCopy(Matrix4f.rotationY(-120.0F), this.TPose_Right);
                this.TPose_Right.M[0][3] = 0.5F;
                this.TPose_Right.M[1][3] = 1.0F;
                this.TPose_Right.M[2][3] = -0.5F;
                this.TPose_Left.M[0][3] = 0.0F;
                this.TPose_Left.M[1][3] = 0.0F;
                this.TPose_Left.M[2][3] = 0.0F;
                matrix4f = this.TPose_Left;
                Utils.Matrix4fCopy(Matrix4f.rotationY(120.0F), this.TPose_Left);
                this.TPose_Left.M[0][3] = -0.5F;
                this.TPose_Left.M[1][3] = 1.0F;
                this.TPose_Left.M[2][3] = -0.5F;
                this.Neutral_HMD.M[0][3] = 0.0F;
                this.Neutral_HMD.M[1][3] = 1.8F;
                Utils.Matrix4fCopy(this.Neutral_HMD, this.hmdPose);
                this.headIsTracking = true;
            }

            if (this.inputInitialized) {
                this.mc.getProfiler().push("updateActionState");

                if (this.updateActiveActionSets()) {
                    int k = VRInput.VRInput_UpdateActionState(activeActionSetsBuffer, VRActiveActionSet.SIZEOF);

                    if (k != 0) {
                        throw new RuntimeException("Error updating action state: code " + getInputErrorName(k));
                    }
                }

                this.inputActions.values().forEach(this::readNewData);
                this.mc.getProfiler().pop();

                if (this.dh.vrSettings.reverseHands) {
                    this.updateControllerPose(0, this.leftPoseHandle);
                    this.updateControllerPose(1, this.rightPoseHandle);
                } else {
                    this.updateControllerPose(0, this.rightPoseHandle);
                    this.updateControllerPose(1, this.leftPoseHandle);
                }

                this.updateControllerPose(2, this.externalCameraPoseHandle);
            }

            this.updateAim();
        }
    }

    long getActionSetHandle(VRInputActionSet actionSet) {
        return this.actionSetHandles.get(actionSet);
    }

    long getControllerHandle(ControllerType hand) {
        if (this.dh.vrSettings.reverseHands) {
            return hand == ControllerType.RIGHT ? this.leftControllerHandle : this.rightControllerHandle;
        } else {
            return hand == ControllerType.RIGHT ? this.rightControllerHandle : this.leftControllerHandle;
        }
    }

    long getInputSourceHandle(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer longbyreference = stack.callocLong(1);
            int i = VRInput_GetInputSourceHandle(path, longbyreference);

            if (i != 0) {
                throw new RuntimeException("Error getting input source handle for '" + path + "': " + getInputErrorName(i));
            } else {
                return longbyreference.get(0);
            }
        }
    }

    ControllerType getOriginControllerType(long inputValueHandle) {
        if (inputValueHandle == 0L) {
            return null;
        } else {
            this.readOriginInfo(inputValueHandle);

            if (this.originInfo.trackedDeviceIndex() != -1) {
                if (this.originInfo.trackedDeviceIndex() == this.controllerDeviceIndex[0]) {
                    return ControllerType.RIGHT;
                }

                if (this.originInfo.trackedDeviceIndex() == this.controllerDeviceIndex[1]) {
                    return ControllerType.LEFT;
                }
            }

            return null;
        }
    }

    public void readNewData(VRInputAction action) {
        String s = action.type;

        switch (s) {
            case "boolean":
                if (action.isHanded()) {
                    for (ControllerType controllertype1 : ControllerType.values()) {
                        this.readDigitalData(action, controllertype1);
                    }
                } else {
                    this.readDigitalData(action, null);
                }

                break;

            case "vector1":
            case "vector2":
            case "vector3":
                if (action.isHanded()) {
                    for (ControllerType controllertype : ControllerType.values()) {
                        this.readAnalogData(action, controllertype);
                    }
                } else {
                    this.readAnalogData(action, null);
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

    public boolean hasThirdController() {
        return this.controllerDeviceIndex[2] != -1;
    }

    public List<Long> getOrigins(VRInputAction action) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var longbyreference = stack.callocLong(16);
            int i = VRInput_GetActionOrigins(this.getActionSetHandle(action.actionSet), action.handle, longbyreference);

            if (i != 0) {
                throw new RuntimeException("Error getting action origins for '" + action.name + "': " + getInputErrorName(i));
            } else {
                List<Long> list = new ArrayList<>();

                while (longbyreference.remaining() > 0) {
                    long j = longbyreference.get();
                    if (j != 0L) {
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
        int activityLevel = VRSystem_GetTrackedDeviceActivityLevel(0);
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
