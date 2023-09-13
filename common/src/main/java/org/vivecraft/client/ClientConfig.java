package org.vivecraft.client;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.vivecraft.client_vr.gui.PhysicalKeyboard;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.ConfigBuilder;
import org.vivecraft.common.utils.math.Angle;
import org.vivecraft.common.utils.math.Quaternion;

import java.util.List;

public class ClientConfig {

    //Keyboard
    public static ConfigBuilder.BooleanValue physicalKeyboard;
    public static ConfigBuilder.BooleanValue autoOpenKeyboard;
    public static ConfigBuilder.BooleanValue alwaysSimulateKeyboard;
    public static ConfigBuilder.StringValue keyboardKeys;
    public static ConfigBuilder.StringValue keyboardKeysShift;
    public static ConfigBuilder.DoubleValue physicalKeyboardScale;
    public static ConfigBuilder.EnumValue<PhysicalKeyboard.KeyboardTheme> physicalKeyboardTheme;

    //MR
    public static ConfigBuilder.DoubleValue mrMovingCamOffsetZ;
    public static ConfigBuilder.DoubleValue mrMovingCamOffsetY;
    public static ConfigBuilder.DoubleValue mrMovingCamOffsetX;
    public static ConfigBuilder.StringValue mixedRealityKeyColor;
    public static ConfigBuilder.BooleanValue mixedRealityAlphaMask;
    public static ConfigBuilder.DoubleValue mixedRealityFov;
    public static ConfigBuilder.DoubleValue mrMovingCamOffsetRotW;
    public static ConfigBuilder.DoubleValue mrMovingCamOffsetRotZ;
    public static ConfigBuilder.DoubleValue mrMovingCamOffsetRotY;
    public static ConfigBuilder.DoubleValue mrMovingCamOffsetRotX;
    public static ConfigBuilder.BooleanValue mixedRealityRenderCameraModel;
    public static ConfigBuilder.BooleanValue mixedRealityUndistorted;
    public static ConfigBuilder.BooleanValue mixedRealityUnityLike;
    public static ConfigBuilder.BooleanValue mixedRealityRenderHands;

    //Roomscale
    public static ConfigBuilder.BooleanValue realisticSneakEnabled;
    public static ConfigBuilder.DoubleValue sneakThreshold;
    public static ConfigBuilder.BooleanValue realisticJumpEnabled;
    public static ConfigBuilder.DoubleValue jumpThreshold;
    public static ConfigBuilder.BooleanValue autoSprint;
    public static ConfigBuilder.DoubleValue autoSprintThreshold;
    public static ConfigBuilder.BooleanValue allowCrawling;
    public static ConfigBuilder.DoubleValue crawlThreshold;
    public static ConfigBuilder.BooleanValue realisticClimbEnabled;
    public static ConfigBuilder.BooleanValue realisticRowEnabled;
    public static ConfigBuilder.BooleanValue realisticSwimEnabled;
    public static ConfigBuilder.DoubleValue movementSpeedMultiplier;
    public static ConfigBuilder.DoubleValue walkMultiplier;
    public static ConfigBuilder.EnumValue<VRSettings.WeaponCollision> weaponCollision;
    public static ConfigBuilder.EnumValue<VRSettings.BowMode> bowMode;
    public static ConfigBuilder.BooleanValue walkUpBlocks;
    public static ConfigBuilder.BooleanValue backpackSwitching;
    public static ConfigBuilder.BooleanValue vrTouchHotbar;
    public static ConfigBuilder.BooleanValue guiAppearOverBlock;
    public static ConfigBuilder.BooleanValue physicalGuiEnabled;
    public static ConfigBuilder.EnumValue<VRSettings.InertiaFactor> inertiaFactor; //TODO just use a number?
    public static ConfigBuilder.BooleanValue vehicleRotation;

    //Display
    public static ConfigBuilder.EnumValue<VRSettings.MirrorMode> displayMirrorMode;
    public static ConfigBuilder.BooleanValue useCrosshairOcclusion;
    public static ConfigBuilder.DoubleValue menuCrosshairScale;
    public static ConfigBuilder.BooleanValue crosshairScalesWithDistance;
    public static ConfigBuilder.BooleanValue menuBackground;
    public static ConfigBuilder.BooleanValue menuAlwaysFollowFace;
    public static ConfigBuilder.EnumValue<VRSettings.RenderPointerElement> renderInGameCrosshairMode;
    public static ConfigBuilder.EnumValue<VRSettings.ShaderGUIRender> shaderGUIRender;
    public static ConfigBuilder.BooleanValue hideGUI;
    public static ConfigBuilder.DoubleValue crosshairScale;
    public static ConfigBuilder.DoubleValue renderScaleFactor;


    //HUD
    public static ConfigBuilder.BooleanValue lowHealthIndicator;
    public static ConfigBuilder.DoubleValue hudDistance;
    public static ConfigBuilder.DoubleValue hudPitchOffset;
    public static ConfigBuilder.DoubleValue hudYawOffset;
    public static ConfigBuilder.BooleanValue stencilOn;
    public static ConfigBuilder.DoubleValue headHudScale;
    public static ConfigBuilder.DoubleValue headToHmdLength;
    public static ConfigBuilder.BooleanValue hudOcclusion;
    public static ConfigBuilder.DoubleValue hudOpacity;
    public static ConfigBuilder.EnumValue<VRSettings.HUDLock> vrHudLockMode;

    //Camera
    public static ConfigBuilder.DoubleValue handCameraResScale;
    public static ConfigBuilder.IntValue handCameraFov;
    public static ConfigBuilder.VectorValue vrFixedCampos;
    public static ConfigBuilder.QuatValue vrFixedCamrot;
    public static ConfigBuilder.EnumValue<Angle.Order> externalCameraAngleOrder;

    //Update
    public static ConfigBuilder.BooleanValue firstRun;
    public static ConfigBuilder.StringValue lastUpdate;
    public static ConfigBuilder.BooleanValue alwaysShowUpdates;
    public static ConfigBuilder.EnumValue<VRSettings.ChatNotifications> chatNotifications;
    public static ConfigBuilder.BooleanValue showServerPluginMessage;
    public static ConfigBuilder.StringValue chatNotificationSound;

    //Teleport
    public static ConfigBuilder.BooleanValue limitedTeleport; //Server setting?
    public static ConfigBuilder.IntValue teleportLimitDown;
    public static ConfigBuilder.IntValue teleportLimitHoriz;
    public static ConfigBuilder.IntValue teleportLimitUp;

    //Seated
    public static ConfigBuilder.BooleanValue seated;
    public static ConfigBuilder.BooleanValue seatedhmd;
    public static ConfigBuilder.BooleanValue seatedHudAltMode;
    public static ConfigBuilder.BooleanValue seatedFreeMove;
    public static ConfigBuilder.BooleanValue forceStandingFreeMove;
    public static ConfigBuilder.EnumValue<VRSettings.FreeMove> vrFreeMoveFlyMode; //TODO check
    public static ConfigBuilder.EnumValue<VRSettings.FreeMove> vrFreeMoveMode;


    //General
    public static ConfigBuilder.BooleanValue vrHotswitchingEnabled;
    public static ConfigBuilder.BooleanValue vrEnabled;
    public static ConfigBuilder.IntValue worldRotation;
    public static ConfigBuilder.DoubleValue vrWorldRotationIncrement;
    public static ConfigBuilder.IntValue worldScale;
    public static ConfigBuilder.VectorValue originOffset;
    public static ConfigBuilder.IntValue version;
    public static ConfigBuilder.BooleanValue reverseHands;
    public static ConfigBuilder.BooleanValue allowStandingOriginOffset;
    public static ConfigBuilder.IntValue forceHardwareDetection;
    public static ConfigBuilder.EnumValue<VRSettings.VRProvider> stereoProviderPluginID;
    public static ConfigBuilder.BooleanValue analogMovement;
    public static ConfigBuilder.DoubleValue manualCalibration;
    public static ConfigBuilder.EnumValue<VRSettings.RightClickDelay> rightclickDelay;
    public static ConfigBuilder.BooleanValue thirdPersonItems;
    public static ConfigBuilder.BooleanValue bcbOn; //TODO rename shadow
    public static ConfigBuilder.BooleanValue allowAdvancedBindings;
    public static ConfigBuilder.DoubleValue xSensitivity;
    public static ConfigBuilder.DoubleValue ySensitivity;
    public static ConfigBuilder.DoubleValue keyholeX;
    public static ConfigBuilder.BooleanValue useFsaa;
    public static ConfigBuilder.BooleanValue displayMirrorLeftEye;
    public static ConfigBuilder.BooleanValue disableFun;
    public static ConfigBuilder.EnumValue<VRSettings.MenuWorld> menuWorldSelection;
    public static ConfigBuilder.BooleanValue insideBlockSolidColor;
    public static ConfigBuilder.EnumValue<VRSettings.RenderPointerElement> renderBlockOutlineMode;
    public static ConfigBuilder.IntValue hrtfSelection; //TODO remove?


    //Radial
    public static ConfigBuilder.BooleanValue radialModeHold;
    public static ConfigBuilder.StringValue RADIAL_1;
    public static ConfigBuilder.StringValue RADIAL_2;
    public static ConfigBuilder.StringValue RADIAL_3;
    public static ConfigBuilder.StringValue RADIAL_4;
    public static ConfigBuilder.StringValue RADIAL_5;
    public static ConfigBuilder.StringValue RADIAL_6;
    public static ConfigBuilder.StringValue RADIAL_7;
    public static ConfigBuilder.StringValue RADIAL_8;
    public static ConfigBuilder.StringValue RADIAL_9;

    //QuickCommand
    public static ConfigBuilder.StringValue QUICKCOMMAND_0;
    public static ConfigBuilder.StringValue QUICKCOMMAND_1;
    public static ConfigBuilder.StringValue QUICKCOMMAND_2;
    public static ConfigBuilder.StringValue QUICKCOMMAND_3;
    public static ConfigBuilder.StringValue QUICKCOMMAND_4;
    public static ConfigBuilder.StringValue QUICKCOMMAND_5;
    public static ConfigBuilder.StringValue QUICKCOMMAND_6;
    public static ConfigBuilder.StringValue QUICKCOMMAND_7;
    public static ConfigBuilder.StringValue QUICKCOMMAND_8;
    public static ConfigBuilder.StringValue QUICKCOMMAND_9;

    //FOV
    public static ConfigBuilder.DoubleValue monoFOV; //TODO Dummy
    public static ConfigBuilder.DoubleValue fovReductionMin;
    public static ConfigBuilder.DoubleValue fovRedutioncOffset; //TODO typo
    public static ConfigBuilder.BooleanValue fovReduction;

    private static CommentedFileConfig config;
    private static ConfigBuilder builder;

    public static List<ConfigBuilder.ConfigValue> getConfigValues(){
        return builder.getConfigValues();
    }

    public static void init(ConfigSpec.CorrectionListener listener){
        Config.setInsertionOrderPreserved(true);
        config = CommentedFileConfig
                .builder(Xplat.getConfigPath("vivecraft-client-config.toml"))
                .autosave()
                .concurrent()
                .build();

        config.load();

        if (listener == null) {
            listener = (action, path, incorrectValue, correctedValue) -> {
                if (incorrectValue != null) {
                    System.out.println("Corrected " + String.join(".", path) + ": was " + incorrectValue + ", is now " + correctedValue);
                }
            };
        }

        fixConfig(config, listener);

        config.save();
    }

    private static void fixConfig(CommentedConfig config, ConfigSpec.CorrectionListener listener) {

        builder = new ConfigBuilder(config, new ConfigSpec());

        builder.push("general");
        vrEnabled = builder
                .push("vrEnabled")
                .comment("Enable VR")
                .define(false);
        vrHotswitchingEnabled = builder
                .push("hotswitch")
                .comment("Allow hotswitch between VR and nonVR")
                .define(true);
        worldScale = builder
                .push("worldscale")
                .comment("")
                .defineInRange(1,0, 29);
        worldRotation = builder
                .push("worldrotation")
                .comment("")
                .defineInRange(0,0, 360);
        vrWorldRotationIncrement = builder
                .push("vrWorldRotationIncrement")
                .comment("")
                .defineInRange(45d,-1d, 4d); //TODO Looks wrong?
        allowStandingOriginOffset = builder
                .push("allowStandingOriginOffset")
                .comment("")
                .define(false);
        originOffset = builder
                .push("originOffset")
                .comment("")
                .define(new Vector3d( 0d, 0d, 0d));
        forceHardwareDetection = builder
                .push("forceHardwareDetection")
                .comment("0 = off, 1 = vive, 2 = oculus") //TODO enum?
                .defineInRange(0, 0,2);
        stereoProviderPluginID = builder
                .push("stereoProviderPluginID")
                .comment("")
                .define(VRSettings.VRProvider.OPENVR);
        analogMovement = builder
                .push("analogMovement")
                .comment("")
                .define(true);
        manualCalibration = builder
                .push("manualCalibration")
                .comment("")
                .defineInRange(-1d, 0d, 5d); //TODO check
        rightclickDelay = builder
                .push("rightclickDelay")
                .comment("")
                .define(VRSettings.RightClickDelay.VANILLA);
        thirdPersonItems = builder
                .push("thirdPersonItems")
                .comment("")
                .define(false);
        bcbOn = builder
                .push("bcbOn")
                .comment("")
                .define(true);
        allowAdvancedBindings = builder
                .push("allowAdvancedBindings")
                .comment("")
                .define(false);
        xSensitivity = builder
                .push("xSensitivity")
                .comment("")
                .defineInRange(1d, 0.1d, 5d);
        ySensitivity = builder
                .push("ySensitivity")
                .comment("")
                .defineInRange(1d, 0.1d, 5d);
        keyholeX = builder
                .push("keyholeX")
                .comment("")
                .defineInRange(15d, 0d, 40d);
        useFsaa = builder
                .push("fsaa")
                .comment("Enable FSAA")
                .define(false);
        disableFun = builder
                .push("disableFun")
                .comment("")
                .define(false);
        menuWorldSelection = builder
                .push("menuWorldSelection")
                .comment("")
                .define(VRSettings.MenuWorld.BOTH);
        insideBlockSolidColor = builder
                .push("insideblock")
                .comment("If the the inside of a block should be solid black")
                .define(false);
        displayMirrorLeftEye = builder
                .push("lefteye")
                .comment("Use the left eye instead of the right eye for the mirror")
                .define(false);
        hrtfSelection = builder
                .push("hrtfSelection")
                .comment("")
                .defineInRange(0, -1, 0);
        builder.pop();

        builder
                .push("keyboard");
        physicalKeyboard = builder
                .push("physical")
                .comment("Enables the Physical Keyboard")
                .define(true);
        autoOpenKeyboard = builder
                .push("open")
                .comment("Automatically open the keyboard")
                .define(false);
        alwaysSimulateKeyboard = builder
                .push("simulate")
                .comment("Always simulate the Keyboard")
                .define(false);
        keyboardKeys = builder
                .push("keys")
                .comment("Keyboard key Layout")
                .define("`1234567890-=qwertyuiop[]\\asdfghjkl;':\"zxcvbnm,./?<>");
        keyboardKeysShift = builder
                .push("shiftkeys")
                .comment("Keyboard key Layout for shift")
                .define("~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL;':\\\"ZXCVBNM,./?<>");
        physicalKeyboardScale = builder
                .push("scale")
                .comment("The scale for the Physical Keyboard")
                .defineInRange(1.0d, 0.75d, 1.5d);
        physicalKeyboardTheme = builder
                .push("theme")
                .comment("Keyboard theme used for the Physical Keyboard")
                .define(PhysicalKeyboard.KeyboardTheme.DEFAULT);
        builder.pop();

        builder.push("roomscale");
        realisticSneakEnabled = builder
                .push("sneak")
                .comment("Enable room scale sneaking")
                .define(true);
        sneakThreshold = builder
                .push("sneakthreshold")
                .comment("Threshold for when the player starts sneaking")
                .defineInRange(0.4, 0, 2); //2 should be enough range
        realisticJumpEnabled = builder
                .push("jump")
                .comment("Enable room scale jumping")
                .define(true);
        jumpThreshold = builder
                .push("jumpthreshhold")
                .comment("Threshhold for automatically jumping")
                .defineInRange(0.05d, 0d, 2d); //2 should be enough
        autoSprint = builder
                .push("sprint")
                .comment("Automatically sprint")
                .define(true);
        autoSprintThreshold = builder
                .push("sprintthreshhold")
                .comment("Threshhold for automatically sprinting")
                .defineInRange(0.9d, 0.5d, 1d);
        allowCrawling = builder
                .push("crawl")
                .comment("Enable room scale crawling")
                .define(true);
        crawlThreshold = builder
                .push("crawlthreshold")
                .comment("Threshold for when the player starts crawling")
                .defineInRange(0.82, 0, 2); //2 should be enough range
        weaponCollision = builder
                .push("weaponcollision")
                .comment("Weapon Collision mode used")
                .define(VRSettings.WeaponCollision.AUTO);
        bowMode = builder
                .push("bow")
                .comment("Bow mode")
                .define(VRSettings.BowMode.ON);
        vrTouchHotbar = builder
                .push("hotbar")
                .comment("Allow selecting items from by touching the hotbar")
                .define(true);
        physicalGuiEnabled = builder
                .push("gui")
                .comment("Allow interactions by touching the gui")
                .define(true);
        realisticRowEnabled = builder
                .push("row")
                .comment("Enable room scale rowing")
                .define(true);
        realisticClimbEnabled = builder
                .push("climbing")
                .comment("Enable room scale climbing")
                .define(true);
        realisticSwimEnabled = builder
                .push("swimming")
                .comment("Enable room scale swimming")
                .define(true);
        guiAppearOverBlock = builder
                .push("guiblock")
                .comment("Places the gui over the accessed block")
                .define(true);
        movementSpeedMultiplier = builder
                .push("movementspeed")
                .comment("Movement Speed modifier")
                .defineInRange(1.0d, 0.15d, 1.3d);
        walkMultiplier = builder
                .push("walkspeed")
                .comment("Walk Speed modifier")
                .defineInRange(1.0d, 1d, 10d);
        backpackSwitching = builder
                .push("backpack")
                .comment("Enables backpack switching")
                .define(true);
        walkUpBlocks = builder
                .push("walkup")
                .comment("Enable auto step up")
                .define(true);
        inertiaFactor = builder
                .push("inertia")
                .comment("The inertia")
                .define(VRSettings.InertiaFactor.NORMAL);
        vehicleRotation = builder
                .push("vehicleRotation")
                .comment("Rotate vehicles")
                .define(true);
        builder.pop();

        builder.push("seated");
        seated = builder
                .push("enable")
                .comment("Enables seated mode")
                .define(false);
        seatedhmd = builder
                .push("hmd")
                .comment("")
                .define(false);
        seatedHudAltMode = builder
                .push("altmode")
                .comment("Enables the alternative HUD position when playing as seated")
                .define(false);
        seatedFreeMove = builder
                .push("freemove")
                .comment("Allow free move while playing as seated")
                .define(true);
        forceStandingFreeMove = builder
                .push("forceStandingFreeMove")
                .comment("")
                .define(false);
        vrFreeMoveMode = builder
                .push("vrFreeMoveMode")
                .comment("Movement mode when using free move")
                .define(VRSettings.FreeMove.CONTROLLER);
        vrFreeMoveFlyMode = builder
                .push("vrFreeMoveFlyMode")
                .comment("Movement mode when flying in free move")
                .define(VRSettings.FreeMove.AUTO);
        builder.pop();

        builder.push("display");
        useCrosshairOcclusion = builder
                .push("crosshairocclusion")
                .comment("Occludes the crosshair")
                .define(true);
        displayMirrorMode = builder
                .push("mirror")
                .comment("Mirror mode")
                .define(VRSettings.MirrorMode.SINGLE);
        renderScaleFactor = builder
                .push("scale")
                .comment("The scale used to render")
                .defineInRange(1.0d, 0.1d, 9d);
        shaderGUIRender = builder
                .push("shaders")
                .comment("Changes how shaders are applied on GUI's")
                .define(VRSettings.ShaderGUIRender.AFTER_SHADER);
        hideGUI = builder //TODO remove as it is a dummy storage field
                .push("hide")
                .comment("Hides the gui")
                .define(false);
        menuAlwaysFollowFace = builder
                .push("followface")
                .comment("Makes the menu follow the players head")
                .define(false);
        menuCrosshairScale = builder
                .push("menuCrosshairScale")
                .comment("The scale for the crosshair in the menus")
                .defineInRange(1.0d, 0.25d, 2.5d);
        crosshairScalesWithDistance = builder
                .push("crosshairScalesWithDistance")
                .comment("Scale for the crosshair with distance")
                .define(false);
        menuBackground = builder
                .push("menuBackground")
                .comment("Use a background in menus")
                .define(false);
        renderInGameCrosshairMode = builder
                .push("renderInGameCrosshairMode")
                .comment("The style of the crosshair")
                .define(VRSettings.RenderPointerElement.ALWAYS);
        crosshairScale = builder
                .push("crosshairScale")
                .comment("The scale of the crosshair")
                .defineInRange(1.0d, 0.25d, 1.0d);
        builder.pop();

        builder.push("hud");
        vrHudLockMode = builder
                .push("hudlock")
                .comment("Determines where the hotbar is locked")
                .define(VRSettings.HUDLock.HEAD);
        lowHealthIndicator = builder
                .push("health")
                .comment("Enables the flashing of red on screen when on low health")
                .define(true);
        hudDistance = builder
                .push("hud")
                .comment("The distance between the player and the HUD")
                .defineInRange(1.25d, 0.25d, 5.0d);
        hudPitchOffset = builder
                .push("hudpitch")
                .comment("The pitch offset of the hud")
                .defineInRange(-2.0d, -5d,5d); //TODO what limit is fine here? int.max seems to much?
        hudYawOffset = builder
                .push("hudyaw")
                .comment("The yaw offset of the hud")
                .defineInRange(0.0d, -5d,5d); //TODO what limit is fine here? int.max seems to much?
        headHudScale = builder
                .push("hudscale")
                .comment("The scale of the hud")
                .defineInRange(1.0d, 0.35d, 2.5d);
        hudOpacity = builder
                .push("hudopacity")
                .comment("The opacity of the hud")
                .defineInRange(1.0d, 0.15d, 1.0d);
        hudOcclusion = builder
                .push("hudOcclusion")
                .comment("Should the hud use occlusion")
                .define(true);
        stencilOn = builder
                .push("stencil")
                .comment("Enables the eye stencil")
                .define(true);
        headToHmdLength = builder
                .push("hmdhead")
                .comment("The distance between the head and the hmd")
                .defineInRange(0.10d, 0d, 1.0d);
        builder.pop();

        builder.push("fov");
        monoFOV = builder
                .push("monoFOV")
                .comment("")
                .defineInRange(0.0d, 0d, 1.0d);
        fovReduction = builder
                .push("fovReduction")
                .comment("")
                .define(false);
        fovReductionMin = builder
                .push("fovReductionMin")
                .comment("")
                .defineInRange(0.25d, 0.1d, 0.7d);
        fovRedutioncOffset = builder
                .push("fovReductionOffset")
                .comment("")
                .defineInRange(0.1f,0d, 0.3d);
        builder.pop();

        builder.push("camera");
        handCameraResScale = builder
                .push("resolution")
                .comment("")
                .defineInRange(1.0d, 0.5d, 3.0d);
        handCameraFov = builder
                .push("fov")
                .comment("")
                .defineInRange(70, 1, 179);
        vrFixedCampos = builder
                .push("vrFixedCampos")
                .comment("")
                .define(new Vector3d(-1.0d, 2.4d, 2.7d));
        vrFixedCamrot = builder
                .push("vrFixedCamrot")
                .comment("")
                .define(new Quaternionf(.962d, .125d, .239d, .041d));
        externalCameraAngleOrder = builder
                .push("externalCameraAngleOrder")
                .comment("")
                .define(Angle.Order.XYZ);
        builder.pop();

        builder.push("update");
        firstRun = builder
                .push("firstRun")
                .comment("Is this the first time the game is run")
                .define(true); //TODO remove?
        lastUpdate = builder
                .push("lastUpdate")
                .comment("the latest version available")
                .define("");
        alwaysShowUpdates = builder
                .push("alwaysShowUpdates")
                .comment("Show that updates are possible on each restart")
                .define(true);
        showServerPluginMessage = builder
                .push("showServerPluginMessage")
                .comment("Show messages from the server side plugin")
                .define(true);
        chatNotifications = builder
                .push("chatNotifications")
                .comment("Should messages in chat notify the player")
                .define(VRSettings.ChatNotifications.NONE);
        chatNotificationSound = builder
                .push("chatNotificationSound")
                .comment("The sound used for chat notifications")
                .define("block.note_block.bell");
        builder.pop();

        builder.push("teleport");
        limitedTeleport = builder
                .push("limitedTeleport")
                .comment("Limit teleportation")
                .define(false);
        teleportLimitUp = builder
                .push("teleportLimitUp")
                .comment("Limit for upwards teleportation")
                .defineInRange(1,0,4);
        teleportLimitDown = builder
                .push("teleportLimitDown")
                .comment("Limit for downwards teleportation")
                .defineInRange(4,0, 16);
        teleportLimitHoriz = builder
                .push("teleportLimitHoriz")
                .comment("Limit for side teleportation")
                .defineInRange(16,0, 32);
        builder.pop();

        builder.correct(listener);

    }
}
