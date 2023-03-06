package org.vivecraft.provider.openvr_jna;

import org.vivecraft.ClientDataHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import jopenvr.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.settings.VRHotkeys;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.provider.HardwareType;
import org.vivecraft.provider.InputSimulator;
import org.vivecraft.provider.MCVR;
import org.vivecraft.provider.openvr_jna.control.TrackpadSwipeSampler;
import org.vivecraft.provider.openvr_jna.control.VRInputActionSet;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.external.jinfinadeck;
import org.vivecraft.utils.external.jkatvr;
import org.vivecraft.utils.math.Matrix4f;
import org.vivecraft.utils.math.Vector3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MCOpenVR extends MCVR
{
    public static final int LEFT_CONTROLLER = 1;
    public static final int RIGHT_CONTROLLER = 0;
    public static final int THIRD_CONTROLLER = 2;
    protected static MCOpenVR ome;
    private final String ACTION_EXTERNAL_CAMERA = "/actions/mixedreality/in/externalcamera";
    private final String ACTION_LEFT_HAND = "/actions/global/in/lefthand";
    private final String ACTION_LEFT_HAPTIC = "/actions/global/out/lefthaptic";
    private final String ACTION_RIGHT_HAND = "/actions/global/in/righthand";
    private final String ACTION_RIGHT_HAPTIC = "/actions/global/out/righthaptic";
    private Map<VRInputActionSet, Long> actionSetHandles = new EnumMap<>(VRInputActionSet.class);
    private VRActiveActionSet_t.ByReference activeActionSetsReference;
    private Map<Long, String> controllerComponentNames;
    private Map<String, Matrix4f[]> controllerComponentTransforms;
    private boolean dbg = true;
    private long externalCameraPoseHandle;
    private int[] controllerDeviceIndex = new int[3];
    private boolean getXforms = true;
    private final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private IntByReference hmdErrorStore = new IntByReference();
    private IntBuffer hmdErrorStoreBuf;
    private TrackedDevicePose_t.ByReference hmdTrackedDevicePoseReference;
    private TrackedDevicePose_t[] hmdTrackedDevicePoses;
    private boolean inputInitialized;
    private long leftControllerHandle;
    private long leftHapticHandle;
    private long leftPoseHandle;
    private InputOriginInfo_t.ByReference originInfo;
    private boolean paused = false;
    private InputPoseActionData_t.ByReference poseData;
    private long rightControllerHandle;
    private long rightHapticHandle;
    private long rightPoseHandle;
    private final VRTextureBounds_t texBounds = new VRTextureBounds_t();
    private Map<String, TrackpadSwipeSampler> trackpadSwipeSamplers = new HashMap<>();
    private boolean tried;
    private Queue<VREvent_t> vrEvents = new LinkedList<>();
    private VR_IVRApplications_FnTable vrApplications;
    private VR_IVRChaperone_FnTable vrChaperone;
    private VR_IVROCSystem_FnTable vrOpenComposite;
    private VR_IVROverlay_FnTable vrOverlay;
    private VR_IVRRenderModels_FnTable vrRenderModels;
    private VR_IVRSettings_FnTable vrSettings;
    VR_IVRCompositor_FnTable vrCompositor;
    VR_IVRInput_FnTable vrInput;
    VR_IVRSystem_FnTable vrsystem;
    final Texture_t texType0 = new Texture_t();
    final Texture_t texType1 = new Texture_t();
    InputDigitalActionData_t digital = new InputDigitalActionData_t.ByReference();
    InputAnalogActionData_t analog = new InputAnalogActionData_t.ByReference();

    public MCOpenVR(Minecraft mc, ClientDataHolder dh)
    {
        super(mc, dh);
        ome = this;
        this.hapticScheduler = new OpenVRHapticScheduler();

        for (int i = 0; i < 3; ++i)
        {
            this.controllerDeviceIndex[i] = -1;
            this.poseData = new InputPoseActionData_t.ByReference();
            this.poseData.setAutoRead(false);
            this.poseData.setAutoWrite(false);
            this.poseData.setAutoSynch(false);
            this.originInfo = new InputOriginInfo_t.ByReference();
            this.originInfo.setAutoRead(false);
            this.originInfo.setAutoWrite(false);
            this.originInfo.setAutoSynch(false);
        }

        this.digital.setAutoRead(false);
        this.digital.setAutoWrite(false);
        this.digital.setAutoSynch(false);
        this.analog.setAutoRead(false);
        this.analog.setAutoWrite(false);
        this.analog.setAutoSynch(false);
    }

    public static MCOpenVR get()
    {
        return ome;
    }

    static String getInputErrorName(int code)
    {
        switch (code)
        {
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

    public void destroy()
    {
        if (this.initialized)
        {
            try
            {
                JOpenVRLibrary.VR_ShutdownInternal();
                this.initialized = false;

                if (ClientDataHolder.katvr)
                {
                    jkatvr.Halt();
                }

                if (ClientDataHolder.infinadeck)
                {
                    jinfinadeck.Destroy();
                }
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
            }
        }
    }

    public String getID()
    {
        return "openvr_jna";
    }

    public String getName()
    {
        return "OpenVR_JNA";
    }

    public float[] getPlayAreaSize()
    {
        if (this.vrChaperone != null && this.vrChaperone.GetPlayAreaSize != null)
        {
            FloatByReference floatbyreference = new FloatByReference();
            FloatByReference floatbyreference1 = new FloatByReference();
            byte b0 = this.vrChaperone.GetPlayAreaSize.apply(floatbyreference1, floatbyreference);
            return b0 == 1 ? new float[] {floatbyreference1.getValue() * this.dh.vrSettings.walkMultiplier, floatbyreference.getValue() * this.dh.vrSettings.walkMultiplier} : null;
        }
        else
        {
            return null;
        }
    }

    public boolean init()
    {
        if (this.initialized)
        {
            return true;
        }
        else if (this.tried)
        {
            return this.initialized;
        }
        else
        {
            this.tried = true;
            this.mc = Minecraft.getInstance();
                try
                {
                    this.initializeJOpenVR();
                    this.initOpenVRCompositor();
                    this.initOpenVRSettings();
                    this.initOpenVRRenderModels();
                    this.initOpenVRChaperone();
                    this.initOpenVRApplications();
                    this.initOpenVRInput();
                    this.initOpenComposite();
                }
                catch (Exception exception2)
                {
                    exception2.printStackTrace();
                    this.initSuccess = false;
                    this.initStatus = exception2.getLocalizedMessage();
                    return false;
                }

                if (this.vrInput == null)
                {
                    System.out.println("Controller input not available. Forcing seated mode.");
                    this.dh.vrSettings.seated = true;
                }

                System.out.println("OpenVR initialized & VR connected.");
                this.deviceVelocity = new Vec3[64];

                for (int i = 0; i < this.poseMatrices.length; ++i)
                {
                    this.poseMatrices[i] = new Matrix4f();
                    this.deviceVelocity[i] = new Vec3(0.0D, 0.0D, 0.0D);
                }

                this.initialized = true;

                if (ClientDataHolder.katvr)
                {
                    try
                    {
                        System.out.println("Waiting for KATVR....");
                        Utils.unpackNatives("katvr");
                        NativeLibrary.addSearchPath("WalkerBase.dll", (new File("openvr/katvr")).getAbsolutePath());
                        jkatvr.Init(1);
                        jkatvr.Launch();

                        if (jkatvr.CheckForLaunch())
                        {
                            System.out.println("KATVR Loaded");
                        }
                        else
                        {
                            System.out.println("KATVR Failed to load");
                        }
                    }
                    catch (Exception exception1)
                    {
                        System.out.println("KATVR crashed: " + exception1.getMessage());
                    }
                }

                if (ClientDataHolder.infinadeck)
                {
                    try
                    {
                        System.out.println("Waiting for Infinadeck....");
                        Utils.unpackNatives("infinadeck");
                        NativeLibrary.addSearchPath("InfinadeckAPI.dll", (new File("openvr/infinadeck")).getAbsolutePath());

                        if (jinfinadeck.InitConnection())
                        {
                            jinfinadeck.CheckConnection();
                            System.out.println("Infinadeck Loaded");
                        }
                        else
                        {
                            System.out.println("Infinadeck Failed to load");
                        }
                    }
                    catch (Exception exception)
                    {
                        System.out.println("Infinadeck crashed: " + exception.getMessage());
                    }
                }

                return true;
            }
    }

    public void poll(long frameIndex)
    {
        if (this.initialized)
        {
            this.paused = this.vrsystem.ShouldApplicationPause.apply() != 0;
            this.mc.getProfiler().push("events");
            this.pollVREvents();

            if (!this.dh.vrSettings.seated)
            {
                this.mc.getProfiler().popPush("controllers");
                this.mc.getProfiler().push("gui");

                if (this.mc.screen == null && this.dh.vrSettings.vrTouchHotbar)
                {
                    VRSettings vrsettings = this.dh.vrSettings;

                    if (this.dh.vrSettings.vrHudLockMode != VRSettings.HUDLock.HEAD && this.hudPopup)
                    {
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

    public void processInputs()
    {
        if (!this.dh.vrSettings.seated && !ClientDataHolder.viewonly && this.inputInitialized)
        {
            for (VRInputAction vrinputaction : this.inputActions.values())
            {
                if (vrinputaction.isHanded())
                {
                    for (ControllerType controllertype : ControllerType.values())
                    {
                        vrinputaction.setCurrentHand(controllertype);
                        this.processInputAction(vrinputaction);
                    }
                }
                else
                {
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
            this.processScrollInput(this.keyHotbarScroll, () ->
            {
                this.changeHotbar(-1);
            }, () ->
            {
                this.changeHotbar(1);
            });
            this.processSwipeInput(this.keyHotbarSwipeX, () ->
            {
                this.changeHotbar(1);
            }, () ->
            {
                this.changeHotbar(-1);
            }, (Runnable)null, (Runnable)null);
            this.processSwipeInput(this.keyHotbarSwipeY, (Runnable)null, (Runnable)null, () ->
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
    protected void triggerBindingHapticPulse(KeyMapping binding, int duration)
    {
        ControllerType controllertype = this.findActiveBindingControllerType(binding);

        if (controllertype != null)
        {
            this.triggerHapticPulse(controllertype, duration);
        }
    }

    private boolean isError()
    {
        return this.hmdErrorStore.getValue() != 0 || this.hmdErrorStoreBuf.get(0) != 0;
    }

    private void debugOut(int deviceindex)
    {
        System.out.println("******************* VR DEVICE: " + deviceindex + " *************************");

        for (Field field : JOpenVRLibrary.ETrackedDeviceProperty.class.getDeclaredFields())
        {
            try
            {
                String[] astring = field.getName().split("_");
                String s = astring[astring.length - 1];
                String s1 = "";

                if (s.equals("Float"))
                {
                    s1 = s1 + field.getName() + " " + this.vrsystem.GetFloatTrackedDeviceProperty.apply(deviceindex, field.getInt((Object)null), this.hmdErrorStore);
                }
                else if (s.equals("String"))
                {
                    Pointer pointer = new Memory(32768L);
                    this.vrsystem.GetStringTrackedDeviceProperty.apply(deviceindex, field.getInt((Object)null), pointer, 32767, this.hmdErrorStore);
                    s1 = s1 + field.getName() + " " + pointer.getString(0L);
                }
                else if (s.equals("Bool"))
                {
                    s1 = s1 + field.getName() + " " + this.vrsystem.GetBoolTrackedDeviceProperty.apply(deviceindex, field.getInt((Object)null), this.hmdErrorStore);
                }
                else if (s.equals("Int32"))
                {
                    s1 = s1 + field.getName() + " " + this.vrsystem.GetInt32TrackedDeviceProperty.apply(deviceindex, field.getInt((Object)null), this.hmdErrorStore);
                }
                else if (s.equals("Uint64"))
                {
                    s1 = s1 + field.getName() + " " + this.vrsystem.GetUint64TrackedDeviceProperty.apply(deviceindex, field.getInt((Object)null), this.hmdErrorStore);
                }
                else
                {
                    s1 = s1 + field.getName() + " (skipped)";
                }

                System.out.println(s1.replace("ETrackedDeviceProperty_Prop_", ""));
            }
            catch (IllegalAccessException illegalaccessexception)
            {
                illegalaccessexception.printStackTrace();
            }
        }

        System.out.println("******************* END VR DEVICE: " + deviceindex + " *************************");
    }

    protected ControllerType findActiveBindingControllerType(KeyMapping binding)
    {
        if (!this.inputInitialized)
        {
            return null;
        }
        else
        {
            long i = this.getInputAction(binding).getLastOrigin();
            return i != 0L ? this.getOriginControllerType(i) : null;
        }
    }

    private String findEvent(int eventcode)
    {
        Field[] afield = JOpenVRLibrary.EVREventType.class.getFields();

        for (Field field : afield)
        {
            if (field.getType() == Integer.TYPE)
            {
                String s = field.getName();

                try
                {
                    int i = field.getInt((Object)null);

                    if (i == eventcode)
                    {
                        return s;
                    }
                }
                catch (IllegalArgumentException illegalargumentexception)
                {
                    illegalargumentexception.printStackTrace();
                }
                catch (IllegalAccessException illegalaccessexception)
                {
                    illegalaccessexception.printStackTrace();
                }
            }
        }

        return "";
    }

    private void generateActionManifest()
    {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        for (VRInputActionSet vrinputactionset : VRInputActionSet.values())
        {
            String s = vrinputactionset.usage;

            if (vrinputactionset.advanced && !this.dh.vrSettings.allowAdvancedBindings)
            {
                s = "hidden";
            }

            list.add(ImmutableMap.<String, Object>builder().put("name", vrinputactionset.name).put("usage", s).build());
        }

        map.put("action_sets", list);
        List<VRInputAction> list1 = new ArrayList<>(this.inputActions.values());
        list1.sort(Comparator.comparing((action) ->
        {
            return action.keyBinding;
        }));
        List<Map<String, Object>> list2 = new ArrayList<>();

        for (VRInputAction vrinputaction : list1)
        {
            list2.add(ImmutableMap.<String, Object>builder().put("name", vrinputaction.name).put("requirement", vrinputaction.requirement).put("type", vrinputaction.type).build());
        }

        list2.add(ImmutableMap.<String, Object>builder().put("name", "/actions/global/in/lefthand").put("requirement", "suggested").put("type", "pose").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", "/actions/global/in/righthand").put("requirement", "suggested").put("type", "pose").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", "/actions/mixedreality/in/externalcamera").put("requirement", "optional").put("type", "pose").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", "/actions/global/out/lefthaptic").put("requirement", "suggested").put("type", "vibration").build());
        list2.add(ImmutableMap.<String, Object>builder().put("name", "/actions/global/out/righthaptic").put("requirement", "suggested").put("type", "vibration").build());
        map.put("actions", list2);
        Map<String, Object> map1 = new HashMap<>();

        for (VRInputAction vrinputaction1 : list1)
        {
            MutableComponent component = Component.translatable(vrinputaction1.keyBinding.getCategory()).append(" - ").append(Component.translatable(vrinputaction1.keyBinding.getName()));
            map1.put(vrinputaction1.name, component.getString());
        }

        for (VRInputActionSet vrinputactionset1 : VRInputActionSet.values())
        {
            MutableComponent component = Component.translatable(vrinputactionset1.localizedName);
            map1.put(vrinputactionset1.name, component.getString());
        }

        map1.put("/actions/global/in/lefthand", "Left Hand Pose");
        map1.put("/actions/global/in/righthand", "Right Hand Pose");
        map1.put("/actions/mixedreality/in/externalcamera", "External Camera");
        map1.put("/actions/global/out/lefthaptic", "Left Hand Haptic");
        map1.put("/actions/global/out/righthaptic", "Right Hand Haptic");
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

        try
        {
            (new File("openvr/input")).mkdirs();

            try (OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream("openvr/input/action_manifest.json"), StandardCharsets.UTF_8))
            {
                this.GSON.toJson(map, outputstreamwriter);
            }
        }
        catch (Exception exception)
        {
            throw new RuntimeException("Failed to write action manifest", exception);
        }

        String s1 = this.dh.vrSettings.reverseHands ? "_reversed" : "";
        Utils.loadAssetToFile("input/vive_defaults" + s1 + ".json", new File("openvr/input/vive_defaults.json"), false);
        Utils.loadAssetToFile("input/oculus_defaults" + s1 + ".json", new File("openvr/input/oculus_defaults.json"), false);
        Utils.loadAssetToFile("input/wmr_defaults" + s1 + ".json", new File("openvr/input/wmr_defaults.json"), false);
        Utils.loadAssetToFile("input/knuckles_defaults" + s1 + ".json", new File("openvr/input/knuckles_defaults.json"), false);
        Utils.loadAssetToFile("input/cosmos_defaults" + s1 + ".json", new File("openvr/input/cosmos_defaults.json"), false);
        Utils.loadAssetToFile("input/tracker_defaults.json", new File("openvr/input/tracker_defaults.json"), false);
    }

    private long getActionHandle(String name)
    {
        LongByReference longbyreference = new LongByReference();
        int i = this.vrInput.GetActionHandle.apply(this.ptrFomrString(name), longbyreference);

        if (i != 0)
        {
            throw new RuntimeException("Error getting action handle for '" + name + "': " + getInputErrorName(i));
        }
        else
        {
            return longbyreference.getValue();
        }
    }

    private VRActiveActionSet_t[] getActiveActionSets()
    {
        ArrayList<VRInputActionSet> arraylist = new ArrayList<>();
        arraylist.add(VRInputActionSet.GLOBAL);

        // we are always modded
        arraylist.add(VRInputActionSet.MOD);

        arraylist.add(VRInputActionSet.MIXED_REALITY);
        arraylist.add(VRInputActionSet.TECHNICAL);

        if (this.mc.screen == null)
        {
            arraylist.add(VRInputActionSet.INGAME);
            arraylist.add(VRInputActionSet.CONTEXTUAL);
        }
        else
        {
            arraylist.add(VRInputActionSet.GUI);
        }

        if (KeyboardHandler.Showing || RadialHandler.isShowing())
        {
            arraylist.add(VRInputActionSet.KEYBOARD);
        }

        this.activeActionSetsReference = new VRActiveActionSet_t.ByReference();
        this.activeActionSetsReference.setAutoRead(false);
        this.activeActionSetsReference.setAutoWrite(false);
        this.activeActionSetsReference.setAutoSynch(false);
        VRActiveActionSet_t[] avractiveactionset_t = (VRActiveActionSet_t[])this.activeActionSetsReference.toArray(arraylist.size());

        for (int i = 0; i < arraylist.size(); ++i)
        {
            VRInputActionSet vrinputactionset = arraylist.get(i);
            avractiveactionset_t[i].ulActionSet = this.getActionSetHandle(vrinputactionset);
            avractiveactionset_t[i].ulRestrictedToDevice = 0L;
            avractiveactionset_t[i].nPriority = 0;
            avractiveactionset_t[i].setAutoRead(false);
            avractiveactionset_t[i].setAutoWrite(false);
            avractiveactionset_t[i].setAutoSynch(false);
            avractiveactionset_t[i].write();
        }

        this.activeActionSetsReference.write();
        return avractiveactionset_t;
    }

    public Matrix4f getControllerComponentTransform(int controllerIndex, String componenetName)
    {
        return this.controllerComponentTransforms != null && this.controllerComponentTransforms.containsKey(componenetName) && ((Matrix4f[])this.controllerComponentTransforms.get(componenetName))[controllerIndex] != null ? (this.controllerComponentTransforms.get(componenetName))[controllerIndex] : Utils.Matrix4fSetIdentity(new Matrix4f());
    }

    private Matrix4f getControllerComponentTransformFromButton(int controllerIndex, long button)
    {
        return this.controllerComponentNames != null && this.controllerComponentNames.containsKey(button) ? this.getControllerComponentTransform(controllerIndex, this.controllerComponentNames.get(button)) : new Matrix4f();
    }

    private int getError()
    {
        return this.hmdErrorStore.getValue() != 0 ? this.hmdErrorStore.getValue() : this.hmdErrorStoreBuf.get(0);
    }

    long getHapticHandle(ControllerType hand)
    {
        return hand == ControllerType.RIGHT ? this.rightHapticHandle : this.leftHapticHandle;
    }

    private InputOriginInfo_t getOriginInfo(long inputValueHandle)
    {
        InputOriginInfo_t inputorigininfo_t = new InputOriginInfo_t();
        this.readOriginInfo(inputValueHandle);
        inputorigininfo_t.devicePath = this.originInfo.devicePath;
        inputorigininfo_t.trackedDeviceIndex = this.originInfo.trackedDeviceIndex;
        inputorigininfo_t.rchRenderModelComponentName = this.originInfo.rchRenderModelComponentName;
        return inputorigininfo_t;
    }

    private String getOriginName(long handle)
    {
        Pointer pointer = new Memory(32769L);
        int i = this.vrInput.GetOriginLocalizedName.apply(handle, pointer, 32768, -1);

        if (i != 0)
        {
            throw new RuntimeException("Error getting origin name: " + getInputErrorName(i));
        }
        else
        {
            return pointer.getString(0L);
        }
    }

    float getSuperSampling()
    {
        return this.vrSettings == null ? -1.0F : this.vrSettings.GetFloat.apply(this.ptrFomrString("steamvr"), this.ptrFomrString("supersampleScale"), this.hmdErrorStore);
    }

    private void getTransforms()
    {
        if (this.vrRenderModels != null)
        {
            if (this.getXforms)
            {
                this.controllerComponentTransforms = new HashMap<>();
            }

            if (this.controllerComponentNames == null)
            {
                this.controllerComponentNames = new HashMap<>();
            }

            int i = this.vrRenderModels.GetRenderModelCount.apply();
            Pointer pointer = new Memory(32768L);
            List<String> list = new ArrayList<>();
            list.add("tip");
            list.add("handgrip");
            boolean flag = false;

            for (String s : list)
            {
                this.controllerComponentTransforms.put(s, new Matrix4f[2]);

                for (int j = 0; j < 2; ++j)
                {
                    if (this.controllerDeviceIndex[j] == -1)
                    {
                        flag = true;
                    }
                    else
                    {
                        this.vrsystem.GetStringTrackedDeviceProperty.apply(this.controllerDeviceIndex[j], 1003, pointer, 32767, this.hmdErrorStore);
                        String s1 = pointer.getString(0L);
                        Pointer pointer1 = this.ptrFomrString(s);
                        Pointer pointer2 = new Memory(32768L);
                        this.vrsystem.GetStringTrackedDeviceProperty.apply(this.controllerDeviceIndex[j], 1037, pointer2, 32767, this.hmdErrorStore);
                        String s2 = pointer2.getString(0L);
                        boolean flag1 = s2.contains("holographic");
                        boolean flag2 = s2.contains("rifts");

                        if (flag1 && s.equals("handgrip"))
                        {
                            pointer1 = this.ptrFomrString("body");
                        }

                        long k = this.vrRenderModels.GetComponentButtonMask.apply(pointer, pointer1);

                        if (k > 0L)
                        {
                            this.controllerComponentNames.put(k, s);
                        }

                        long l = j == 0 ? this.rightControllerHandle : this.leftControllerHandle;

                        if (l == 0L)
                        {
                            flag = true;
                        }
                        else
                        {
                            RenderModel_ControllerMode_State_t.ByReference rendermodel_controllermode_state_t$byreference = new RenderModel_ControllerMode_State_t.ByReference();
                            RenderModel_ComponentState_t.ByReference rendermodel_componentstate_t$byreference = new RenderModel_ComponentState_t.ByReference();
                            byte b0 = this.vrRenderModels.GetComponentStateForDevicePath.apply(pointer, pointer1, l, rendermodel_controllermode_state_t$byreference, rendermodel_componentstate_t$byreference);

                            if (b0 == 0)
                            {
                                flag = true;
                            }
                            else
                            {
                                Matrix4f matrix4f = new Matrix4f();
                                OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(rendermodel_componentstate_t$byreference.mTrackingToComponentLocal, matrix4f);
                                (this.controllerComponentTransforms.get(s))[j] = matrix4f;

                                if (j == 1 && flag2 && s.equals("handgrip"))
                                {
                                    (this.controllerComponentTransforms.get(s))[1] = (this.controllerComponentTransforms.get(s))[0];
                                }

                                if (!flag && j == 0)
                                {
                                    try
                                    {
                                        Matrix4f matrix4f1 = this.getControllerComponentTransform(0, "tip");
                                        Matrix4f matrix4f2 = this.getControllerComponentTransform(0, "handgrip");
                                        Vector3 vector3 = matrix4f1.transform(this.forward);
                                        Vector3 vector31 = matrix4f2.transform(this.forward);
                                        double d0 = (double)Math.abs(vector3.normalized().dot(vector31.normalized()));
                                        double d1 = Math.acos(d0);
                                        double d2 = Math.toDegrees(d1);
                                        double d3 = Math.acos((double)vector3.normalized().dot(this.forward.normalized()));
                                        double d4 = Math.toDegrees(d3);
                                        this.gunStyle = d2 > 10.0D;
                                        this.gunAngle = d2;
                                    }
                                    catch (Exception exception)
                                    {
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

    private boolean hasOpenComposite()
    {
        return this.vrOpenComposite != null;
    }

    private void initializeJOpenVR()
    {
        this.hmdErrorStoreBuf = IntBuffer.allocate(1);
        this.vrsystem = null;
        JOpenVRLibrary.VR_InitInternal(this.hmdErrorStoreBuf, 1);

        if (!this.isError())
        {
            this.vrsystem = new VR_IVRSystem_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSystem_Version, this.hmdErrorStoreBuf));
        }

        if (this.vrsystem != null && !this.isError())
        {
            this.vrsystem.setAutoSynch(false);
            this.vrsystem.read();
            System.out.println("OpenVR System Initialized OK.");
            this.hmdTrackedDevicePoseReference = new TrackedDevicePose_t.ByReference();
            this.hmdTrackedDevicePoses = (TrackedDevicePose_t[])this.hmdTrackedDevicePoseReference.toArray(64);
            this.poseMatrices = new Matrix4f[64];

            for (int i = 0; i < this.poseMatrices.length; ++i)
            {
                this.poseMatrices[i] = new Matrix4f();
            }

            this.hmdTrackedDevicePoseReference.setAutoRead(false);
            this.hmdTrackedDevicePoseReference.setAutoWrite(false);
            this.hmdTrackedDevicePoseReference.setAutoSynch(false);

            for (int j = 0; j < 64; ++j)
            {
                this.hmdTrackedDevicePoses[j].setAutoRead(false);
                this.hmdTrackedDevicePoses[j].setAutoWrite(false);
                this.hmdTrackedDevicePoses[j].setAutoSynch(false);
            }

            this.initSuccess = true;
        }
        else
        {
            throw new RuntimeException(JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
        }
    }

    public boolean postinit()
    {
        this.initInputAndApplication();
        return this.inputInitialized;
    }

    private void initInputAndApplication()
    {
        this.populateInputActions();

        if (this.vrInput != null)
        {
            this.generateActionManifest();
            this.loadActionManifest();
            this.loadActionHandles();
            this.installApplicationManifest(false);
            this.inputInitialized = true;
        }
    }

    private void initOpenComposite()
    {
        this.vrOpenComposite = new VR_IVROCSystem_FnTable(JOpenVRLibrary.VR_GetGenericInterface("FnTable:IVROCSystem_001", this.hmdErrorStoreBuf));

        if (!this.isError())
        {
            this.vrOpenComposite.setAutoSynch(false);
            this.vrOpenComposite.read();
            System.out.println("OpenComposite initialized.");
        }
        else
        {
            System.out.println("OpenComposite not found: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
            this.vrOpenComposite = null;
        }
    }

    private void initOpenVRApplications()
    {
        this.vrApplications = new VR_IVRApplications_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRApplications_Version, this.hmdErrorStoreBuf));

        if (!this.isError())
        {
            this.vrApplications.setAutoSynch(false);
            this.vrApplications.read();
            System.out.println("OpenVR Applications initialized OK");
        }
        else
        {
            System.out.println("VRApplications init failed: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
            this.vrApplications = null;
        }
    }

    private void initOpenVRChaperone()
    {
        this.vrChaperone = new VR_IVRChaperone_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRChaperone_Version, this.hmdErrorStoreBuf));

        if (!this.isError())
        {
            this.vrChaperone.setAutoSynch(false);
            this.vrChaperone.read();
            System.out.println("OpenVR chaperone initialized.");
        }
        else
        {
            System.out.println("VRChaperone init failed: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
            this.vrChaperone = null;
        }
    }

    private void initOpenVRCompositor() throws Exception
    {
        if (this.vrsystem != null)
        {
            this.vrCompositor = new VR_IVRCompositor_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRCompositor_Version, this.hmdErrorStoreBuf));

            if (this.vrCompositor == null || this.isError())
            {
                throw new Exception(JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
            }

            System.out.println("OpenVR Compositor initialized OK.");
            this.vrCompositor.setAutoSynch(false);
            this.vrCompositor.read();
            this.vrCompositor.SetTrackingSpace.apply(1);
            int i = 20;
            Pointer pointer = new Memory((long)i);
            System.out.println("TrackingSpace: " + this.vrCompositor.GetTrackingSpace.apply());
            this.vrsystem.GetStringTrackedDeviceProperty.apply(0, 1005, pointer, i, this.hmdErrorStore);
            String s = pointer.getString(0L);
            System.out.println("Device manufacturer is: " + s);
            this.detectedHardware = HardwareType.fromManufacturer(s);
            this.dh.vrSettings.loadOptions();
            VRHotkeys.loadExternalCameraConfig();
        }

        if (this.vrCompositor == null)
        {
            System.out.println("Skipping VR Compositor...");
        }

        this.texBounds.uMax = 1.0F;
        this.texBounds.uMin = 0.0F;
        this.texBounds.vMax = 1.0F;
        this.texBounds.vMin = 0.0F;
        this.texBounds.setAutoSynch(false);
        this.texBounds.setAutoRead(false);
        this.texBounds.setAutoWrite(false);
        this.texBounds.write();
        this.texType0.eColorSpace = 1;
        this.texType0.eType = 1;
        this.texType0.handle = Pointer.createConstant(-1);
        this.texType0.setAutoSynch(false);
        this.texType0.setAutoRead(false);
        this.texType0.setAutoWrite(false);
        this.texType0.write();
        this.texType1.eColorSpace = 1;
        this.texType1.eType = 1;
        this.texType1.handle = Pointer.createConstant(-1);
        this.texType1.setAutoSynch(false);
        this.texType1.setAutoRead(false);
        this.texType1.setAutoWrite(false);
        this.texType1.write();
        System.out.println("OpenVR Compositor initialized OK.");
    }

    private boolean initOpenVRControlPanel()
    {
        return true;
    }

    private void initOpenVRInput()
    {
        this.vrInput = new VR_IVRInput_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRInput_Version, this.hmdErrorStoreBuf));

        if (!this.isError())
        {
            this.vrInput.setAutoSynch(false);
            this.vrInput.read();
            System.out.println("OpenVR Input initialized OK");
        }
        else
        {
            System.out.println("VRInput init failed: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
            this.vrInput = null;
        }
    }

    private void initOpenVRRenderModels()
    {
        this.vrRenderModels = new VR_IVRRenderModels_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRRenderModels_Version, this.hmdErrorStoreBuf));

        if (!this.isError())
        {
            this.vrRenderModels.setAutoSynch(false);
            this.vrRenderModels.read();
            System.out.println("OpenVR RenderModels initialized OK");
        }
        else
        {
            System.out.println("VRRenderModels init failed: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
            this.vrRenderModels = null;
        }
    }

    private void initOpenVRSettings()
    {
        this.vrSettings = new VR_IVRSettings_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSettings_Version, this.hmdErrorStoreBuf));

        if (!this.isError())
        {
            this.vrSettings.setAutoSynch(false);
            this.vrSettings.read();
            System.out.println("OpenVR Settings initialized OK");
        }
        else
        {
            System.out.println("VRSettings init failed: " + JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(this.getError()).getString(0L));
            this.vrSettings = null;
        }
    }

    private void installApplicationManifest(boolean force)
    {
        File file1 = new File("openvr/vivecraft.vrmanifest");
        Utils.loadAssetToFile("vivecraft.vrmanifest", file1, true);
        File file2 = new File("openvr/custom.vrmanifest");

        if (file2.exists())
        {
            file1 = file2;
        }

        if (this.vrApplications != null)
        {
            String s;

            try
            {
                Map map = (new Gson()).fromJson(new FileReader(file1), Map.class);
                s = ((Map)((List)map.get("applications")).get(0)).get("app_key").toString();
            }
            catch (Exception exception1)
            {
                System.out.println("Error reading appkey from manifest");
                exception1.printStackTrace();
                return;
            }

            System.out.println("Appkey: " + s);

            if (!force && this.vrApplications.IsApplicationInstalled.apply(this.ptrFomrString(s)) != 0)
            {
                System.out.println("Application manifest already installed");
            }
            else
            {
                int i = this.vrApplications.AddApplicationManifest.apply(this.ptrFomrString(file1.getAbsolutePath()), (byte)1);

                if (i != 0)
                {
                    System.out.println("Failed to install application manifest: " + this.vrApplications.GetApplicationsErrorNameFromEnum.apply(i).getString(0L));
                    return;
                }

                System.out.println("Application manifest installed successfully");
            }

            int j;

            try
            {
                String s1 = ManagementFactory.getRuntimeMXBean().getName();
                j = Integer.parseInt(s1.split("@")[0]);
            }
            catch (Exception exception)
            {
                System.out.println("Error getting process id");
                exception.printStackTrace();
                return;
            }

            int k = this.vrApplications.IdentifyApplication.apply(j, this.ptrFomrString(s));

            if (k != 0)
            {
                System.out.println("Failed to identify application: " + this.vrApplications.GetApplicationsErrorNameFromEnum.apply(k).getString(0L));
            }
            else
            {
                System.out.println("Application identified successfully");
            }
        }
    }

    private void loadActionHandles()
    {
        LongByReference longbyreference = new LongByReference();

        for (VRInputAction vrinputaction : this.inputActions.values())
        {
            int i = this.vrInput.GetActionHandle.apply(this.ptrFomrString(vrinputaction.name), longbyreference);

            if (i != 0)
            {
                throw new RuntimeException("Error getting action handle for '" + vrinputaction.name + "': " + getInputErrorName(i));
            }

            vrinputaction.setHandle(longbyreference.getValue());
        }

        this.leftPoseHandle = this.getActionHandle("/actions/global/in/lefthand");
        this.rightPoseHandle = this.getActionHandle("/actions/global/in/righthand");
        this.leftHapticHandle = this.getActionHandle("/actions/global/out/lefthaptic");
        this.rightHapticHandle = this.getActionHandle("/actions/global/out/righthaptic");
        this.externalCameraPoseHandle = this.getActionHandle("/actions/mixedreality/in/externalcamera");

        for (VRInputActionSet vrinputactionset : VRInputActionSet.values())
        {
            int j = this.vrInput.GetActionSetHandle.apply(this.ptrFomrString(vrinputactionset.name), longbyreference);

            if (j != 0)
            {
                throw new RuntimeException("Error getting action set handle for '" + vrinputactionset.name + "': " + getInputErrorName(j));
            }

            this.actionSetHandles.put(vrinputactionset, longbyreference.getValue());
        }

        this.leftControllerHandle = this.getInputSourceHandle("/user/hand/left");
        this.rightControllerHandle = this.getInputSourceHandle("/user/hand/right");
    }

    private void loadActionManifest()
    {
        int i = this.vrInput.SetActionManifestPath.apply(this.ptrFomrString((new File("openvr/input/action_manifest.json")).getAbsolutePath()));

        if (i != 0)
        {
            throw new RuntimeException("Failed to load action manifest: " + getInputErrorName(i));
        }
    }

    private void pollVREvents()
    {
        if (this.vrsystem != null)
        {
            for (VREvent_t vrevent_t = new VREvent_t(); this.vrsystem.PollNextEvent.apply(vrevent_t, vrevent_t.size()) > 0; vrevent_t = new VREvent_t())
            {
                this.vrEvents.add(vrevent_t);
            }
        }
    }

    private void processInputAction(VRInputAction action)
    {
        if (action.isActive() && action.isEnabledRaw())
        {
            if (action.isButtonChanged())
            {
                if (action.isButtonPressed() && action.isEnabled())
                {
                    if (!this.ignorePressesNextFrame)
                    {
                        action.pressBinding();
                    }
                }
                else
                {
                    action.unpressBinding();
                }
            }
        }
        else
        {
            action.unpressBinding();
        }
    }

    private void processScrollInput(KeyMapping keyBinding, Runnable upCallback, Runnable downCallback)
    {
        VRInputAction vrinputaction = this.getInputAction(keyBinding);

        if (vrinputaction.isEnabled() && vrinputaction.getLastOrigin() != 0L && vrinputaction.getAxis2D(true).getY() != 0.0F)
        {
            float f = vrinputaction.getAxis2D(false).getY();

            if (f > 0.0F)
            {
                upCallback.run();
            }
            else if (f < 0.0F)
            {
                downCallback.run();
            }
        }
    }

    private void processSwipeInput(KeyMapping keyBinding, Runnable leftCallback, Runnable rightCallback, Runnable upCallback, Runnable downCallback)
    {
        VRInputAction vrinputaction = this.getInputAction(keyBinding);

        if (vrinputaction.isEnabled() && vrinputaction.getLastOrigin() != 0L)
        {
            ControllerType controllertype = this.findActiveBindingControllerType(keyBinding);

            if (controllertype != null)
            {
                if (!this.trackpadSwipeSamplers.containsKey(keyBinding.getName()))
                {
                    this.trackpadSwipeSamplers.put(keyBinding.getName(), new TrackpadSwipeSampler());
                }

                TrackpadSwipeSampler trackpadswipesampler = this.trackpadSwipeSamplers.get(keyBinding.getName());
                trackpadswipesampler.update(controllertype, vrinputaction.getAxis2D(false));

                if (trackpadswipesampler.isSwipedUp() && upCallback != null)
                {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    upCallback.run();
                }

                if (trackpadswipesampler.isSwipedDown() && downCallback != null)
                {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    downCallback.run();
                }

                if (trackpadswipesampler.isSwipedLeft() && leftCallback != null)
                {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    leftCallback.run();
                }

                if (trackpadswipesampler.isSwipedRight() && rightCallback != null)
                {
                    this.triggerHapticPulse(controllertype, 0.001F, 400.0F, 0.5F);
                    rightCallback.run();
                }
            }
        }
    }

    private void processVREvents()
    {
        while (!this.vrEvents.isEmpty())
        {
            VREvent_t vrevent_t = this.vrEvents.poll();

            switch (vrevent_t.eventType)
            {
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

    private Pointer ptrFomrString(String in)
    {
        Pointer pointer = new Memory((long)(in.getBytes(StandardCharsets.UTF_8).length + 1));
        pointer.setString(0L, in, StandardCharsets.UTF_8.name());
        return pointer;
    }

    private void readOriginInfo(long inputValueHandle)
    {
        int i = this.vrInput.GetOriginTrackedDeviceInfo.apply(inputValueHandle, this.originInfo, this.originInfo.size());

        if (i != 0)
        {
            throw new RuntimeException("Error reading origin info: " + getInputErrorName(i));
        }
        else
        {
            this.originInfo.read();
        }
    }

    private void readPoseData(long actionHandle)
    {
        int i = this.vrInput.GetPoseActionDataForNextFrame.apply(actionHandle, 1, this.poseData, this.poseData.size(), 0L);

        if (i != 0)
        {
            throw new RuntimeException("Error reading pose data: " + getInputErrorName(i));
        }
        else
        {
            this.poseData.read();
        }
    }

    private void unpackPlatformNatives()
    {
        String s = System.getProperty("os.name").toLowerCase();
        String s1 = System.getProperty("os.arch").toLowerCase();
        String s2 = "win";

        if (s.contains("linux"))
        {
            s2 = "linux";
        }
        else if (s.contains("mac"))
        {
            s2 = "osx";
        }

        if (!s.contains("mac"))
        {
            if (s1.contains("64"))
            {
                s2 = s2 + "64";
            }
            else
            {
                s2 = s2 + "32";
            }
        }

        try
        {
            Utils.unpackNatives(s2);
        }
        catch (Exception exception)
        {
            System.out.println("Native path not found");
            return;
        }

        String s3 = (new File("openvr/" + s2)).getAbsolutePath();
        System.out.println("Adding OpenVR search path: " + s3);
        NativeLibrary.addSearchPath("openvr_api", s3);
    }

    private void updateControllerPose(int controller, long actionHandle)
    {
        if (this.TPose)
        {
            if (controller == 0)
            {
                Utils.Matrix4fCopy(this.TPose_Right, this.controllerPose[controller]);
            }
            else if (controller == 1)
            {
                Utils.Matrix4fCopy(this.TPose_Left, this.controllerPose[controller]);
            }

            this.controllerTracking[controller] = true;
        }
        else
        {
            this.readPoseData(actionHandle);

            if (this.poseData.activeOrigin != 0L)
            {
                this.readOriginInfo(this.poseData.activeOrigin);
                int i = this.originInfo.trackedDeviceIndex;

                if (i != this.controllerDeviceIndex[controller])
                {
                    this.getXforms = true;
                }

                this.controllerDeviceIndex[controller] = i;

                if (i != -1)
                {
                    TrackedDevicePose_t trackeddevicepose_t = this.poseData.pose;

                    if (trackeddevicepose_t.bPoseIsValid != 0)
                    {
                        OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(trackeddevicepose_t.mDeviceToAbsoluteTracking, this.poseMatrices[i]);
                        this.deviceVelocity[i] = new Vec3((double)trackeddevicepose_t.vVelocity.v[0], (double)trackeddevicepose_t.vVelocity.v[1], (double)trackeddevicepose_t.vVelocity.v[2]);
                        Utils.Matrix4fCopy(this.poseMatrices[i], this.controllerPose[controller]);
                        this.controllerTracking[controller] = true;
                        return;
                    }
                }
            }
            else
            {
                this.controllerDeviceIndex[controller] = -1;
            }

            this.controllerTracking[controller] = false;
        }
    }

    private void updatePose()
    {
        if (this.vrsystem != null && this.vrCompositor != null)
        {
            int i = this.vrCompositor.WaitGetPoses.apply(this.hmdTrackedDevicePoseReference, 64, (TrackedDevicePose_t)null, 0);

            if (i > 0)
            {
                System.out.println("Compositor Error: GetPoseError " + OpenVRStereoRenderer.getCompostiorError(i));
            }

            if (i == 101)
            {
                this.triggerHapticPulse(0, 500);
                this.triggerHapticPulse(1, 500);
            }

            if (this.getXforms)
            {
                this.getTransforms();
            }
            else if (this.dbg)
            {
                this.dbg = false;
                this.debugOut(0);
                this.debugOut(this.controllerDeviceIndex[0]);
                this.debugOut(this.controllerDeviceIndex[1]);
            }

            HmdMatrix34_t hmdmatrix34_t = this.vrsystem.GetEyeToHeadTransform.apply(0);
            OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(hmdmatrix34_t, this.hmdPoseLeftEye);
            HmdMatrix34_t hmdmatrix34_t1 = this.vrsystem.GetEyeToHeadTransform.apply(1);
            OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(hmdmatrix34_t1, this.hmdPoseRightEye);

            for (int j = 0; j < 64; ++j)
            {
                this.hmdTrackedDevicePoses[j].read();

                if (this.hmdTrackedDevicePoses[j].bPoseIsValid != 0)
                {
                    OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(this.hmdTrackedDevicePoses[j].mDeviceToAbsoluteTracking, this.poseMatrices[j]);
                    this.deviceVelocity[j] = new Vec3((double)this.hmdTrackedDevicePoses[j].vVelocity.v[0], (double)this.hmdTrackedDevicePoses[j].vVelocity.v[1], (double)this.hmdTrackedDevicePoses[j].vVelocity.v[2]);
                }
            }

            if (this.hmdTrackedDevicePoses[0].bPoseIsValid != 0)
            {
                Utils.Matrix4fCopy(this.poseMatrices[0], this.hmdPose);
                this.headIsTracking = true;
            }
            else
            {
                this.headIsTracking = false;
                Utils.Matrix4fSetIdentity(this.hmdPose);
                this.hmdPose.M[1][3] = 1.62F;
            }

            this.TPose = false;

            if (this.TPose)
            {
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

            if (this.inputInitialized)
            {
                this.mc.getProfiler().push("updateActionState");
                VRActiveActionSet_t[] avractiveactionset_t = this.getActiveActionSets();

                if (avractiveactionset_t.length > 0)
                {
                    int k = this.vrInput.UpdateActionState.apply(this.activeActionSetsReference, avractiveactionset_t[0].size(), avractiveactionset_t.length);

                    if (k != 0)
                    {
                        throw new RuntimeException("Error updating action state: code " + getInputErrorName(k));
                    }
                }

                this.inputActions.values().forEach(this::readNewData);
                this.mc.getProfiler().pop();

                if (this.dh.vrSettings.reverseHands)
                {
                    this.updateControllerPose(0, this.leftPoseHandle);
                    this.updateControllerPose(1, this.rightPoseHandle);
                }
                else
                {
                    this.updateControllerPose(0, this.rightPoseHandle);
                    this.updateControllerPose(1, this.leftPoseHandle);
                }

                this.updateControllerPose(2, this.externalCameraPoseHandle);
            }

            this.updateAim();
        }
    }

    long getActionSetHandle(VRInputActionSet actionSet)
    {
        return this.actionSetHandles.get(actionSet);
    }

    long getControllerHandle(ControllerType hand)
    {
        if (this.dh.vrSettings.reverseHands)
        {
            return hand == ControllerType.RIGHT ? this.leftControllerHandle : this.rightControllerHandle;
        }
        else
        {
            return hand == ControllerType.RIGHT ? this.rightControllerHandle : this.leftControllerHandle;
        }
    }

    long getInputSourceHandle(String path)
    {
        LongByReference longbyreference = new LongByReference();
        int i = this.vrInput.GetInputSourceHandle.apply(this.ptrFomrString(path), longbyreference);

        if (i != 0)
        {
            throw new RuntimeException("Error getting input source handle for '" + path + "': " + getInputErrorName(i));
        }
        else
        {
            return longbyreference.getValue();
        }
    }

    ControllerType getOriginControllerType(long inputValueHandle)
    {
        if (inputValueHandle == 0L)
        {
            return null;
        }
        else
        {
            this.readOriginInfo(inputValueHandle);

            if (this.originInfo.trackedDeviceIndex != -1)
            {
                if (this.originInfo.trackedDeviceIndex == this.controllerDeviceIndex[0])
                {
                    return ControllerType.RIGHT;
                }

                if (this.originInfo.trackedDeviceIndex == this.controllerDeviceIndex[1])
                {
                    return ControllerType.LEFT;
                }
            }

            return null;
        }
    }

    public void readNewData(VRInputAction action)
    {
        String s = action.type;

        switch (s)
        {
            case "boolean":
                if (action.isHanded())
                {
                    for (ControllerType controllertype1 : ControllerType.values())
                    {
                        this.readDigitalData(action, controllertype1);
                    }
                }
                else
                {
                    this.readDigitalData(action, (ControllerType)null);
                }

                break;

            case "vector1":
            case "vector2":
            case "vector3":
                if (action.isHanded())
                {
                    for (ControllerType controllertype : ControllerType.values())
                    {
                        this.readAnalogData(action, controllertype);
                    }
                }
                else
                {
                    this.readAnalogData(action, (ControllerType)null);
                }
        }
    }

    private void readDigitalData(VRInputAction action, ControllerType hand)
    {
        int i = 0;

        if (hand != null)
        {
            i = hand.ordinal();
        }

        int j = this.vrInput.GetDigitalActionData.apply(action.handle, this.digital, this.digital.size(), hand != null ? this.getControllerHandle(hand) : 0L);

        if (j != 0)
        {
            throw new RuntimeException("Error reading digital data for '" + action.name + "': " + getInputErrorName(j));
        }
        else
        {
            this.digital.read();
            action.digitalData[i].activeOrigin = this.digital.activeOrigin;
            action.digitalData[i].isActive = this.digital.bActive != 0;
            action.digitalData[i].state = this.digital.bState != 0;
            action.digitalData[i].isChanged = this.digital.bChanged != 0;
        }
    }

    private void readAnalogData(VRInputAction action, ControllerType hand)
    {
        int i = 0;

        if (hand != null)
        {
            i = hand.ordinal();
        }

        int j = this.vrInput.GetAnalogActionData.apply(action.handle, this.analog, this.analog.size(), hand != null ? this.getControllerHandle(hand) : 0L);

        if (j != 0)
        {
            throw new RuntimeException("Error reading analog data for '" + action.name + "': " + getInputErrorName(j));
        }
        else
        {
            this.analog.read();
            action.analogData[i].x = this.analog.x;
            action.analogData[i].y = this.analog.y;
            action.analogData[i].z = this.analog.z;
            action.analogData[i].deltaX = this.analog.deltaX;
            action.analogData[i].deltaY = this.analog.deltaY;
            action.analogData[i].deltaZ = this.analog.deltaZ;
            action.analogData[i].activeOrigin = this.analog.activeOrigin;
            action.analogData[i].isActive = this.analog.bActive != 0;
        }
    }

    public boolean hasThirdController()
    {
        return this.controllerDeviceIndex[2] != -1;
    }

    public List<Long> getOrigins(VRInputAction action)
    {
        Pointer pointer = new Memory(128L);
        LongByReference longbyreference = new LongByReference();
        longbyreference.setPointer(pointer);
        int i = get().vrInput.GetActionOrigins.apply(this.getActionSetHandle(action.actionSet), action.handle, longbyreference, 16);

        if (i != 0)
        {
            throw new RuntimeException("Error getting action origins for '" + action.name + "': " + getInputErrorName(i));
        }
        else
        {
            List<Long> list = new ArrayList<>();

            for (long j : pointer.getLongArray(0L, 16))
            {
                if (j != 0L)
                {
                    list.add(j);
                }
            }

            return list;
        }
    }
}
