package org.vivecraft.server.config;

import org.vivecraft.client.Xplat;
import org.vivecraft.server.config.ConfigBuilder.*;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.ConfigSpec.CorrectionListener;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

import static org.vivecraft.common.utils.Utils.logger;

public class ServerConfig {

    // config keys
    public static BooleanValue debug;
    public static BooleanValue checkForUpdate;
    public static BooleanValue vr_only;
    public static BooleanValue vive_only;
    public static BooleanValue allow_op;
    public static DoubleValue messageKickDelay;
    public static BooleanValue vrFun;

    public static BooleanValue messagesEnabled;
    public static StringValue messagesWelcomeVR;
    public static StringValue messagesWelcomeNonVR;
    public static StringValue messagesWelcomeSeated;
    public static StringValue messagesWelcomeVanilla;
    public static StringValue messagesLeaveMessage;
    public static StringValue messagesKickViveOnly;
    public static StringValue messagesKickVROnly;

    public static DoubleValue creeperSwellDistance;
    public static DoubleValue bowStandingMultiplier;
    public static DoubleValue bowSeatedMultiplier;
    public static DoubleValue bowStandingHeadshotMultiplier;
    public static DoubleValue bowSeatedHeadshotMultiplier;
    public static DoubleValue bowVanillaHeadshotMultiplier;

    public static BooleanValue pvpVRvsVR;
    public static BooleanValue pvpSEATEDVRvsSEATEDVR;
    public static BooleanValue pvpVRvsNONVR;
    public static BooleanValue pvpSEATEDVRvsNONVR;
    public static BooleanValue pvpVRvsSEATEDVR;

    public static BooleanValue climbeyEnabled;
    public static ConfigValue<String> climbeyBlockmode;
    public static ConfigValue<List<? extends String>> climbeyBlocklist;

    public static BooleanValue crawlingEnabled;

    public static BooleanValue teleportEnabled;
    public static BooleanValue teleportLimitedSurvival;
    public static IntValue teleportUpLimit;
    public static IntValue teleportDownLimit;
    public static IntValue teleportHorizontalLimit;

    public static BooleanValue worldscaleLimited;
    public static DoubleValue worldscaleMax;
    public static DoubleValue worldscaleMin;

    public static BooleanValue vrSwitchingEnabled;

    private static CommentedFileConfig config;
    private static ConfigBuilder builder;
    public static List<ConfigValue> getConfigValues(){
        return builder.getConfigValues();
    }

    public static void init(CorrectionListener listener){
        Config.setInsertionOrderPreserved(true);
        config = CommentedFileConfig
                .builder(Xplat.getConfigPath("vivecraft-server-config.toml"))
                .autosave()
                .sync()
                .concurrent()
                .build();

        config.load();

        if (listener == null) {
            listener = (action, path, incorrectValue, correctedValue) -> {
                if (incorrectValue != null) {
                    logger.warn("Corrected " + String.join(".", path) + ": was " + incorrectValue + ", is now " + correctedValue);
                }
            };
        }

        fixConfig(config, listener);

        config.save();
    }

    private static void fixConfig(CommentedConfig config, CorrectionListener listener) {

        builder = new ConfigBuilder(config, new ConfigSpec());

        builder
            .push("general");
        debug = builder
            .push("debug")
            .comment("will print clients that connect with vivecraft, and what version they are using, to the log.")
            .define(false);
        checkForUpdate = builder
            .push("checkForUpdate")
            .comment("will check for a newer version and alert any OP when they login to the server.")
            .define(true);
        vr_only = builder
            .push("vr_only")
            .comment("Set to true to only allow VR players to play.\n If enabled, VR hotswitching will be automatically disabled.")
            .define(false);
        vive_only = builder
            .push("vive_only")
            .comment("Set to true to only allow vivecraft players to play.")
            .define(false);
        allow_op = builder
            .push("allow_op")
            .comment("If true, will allow server ops to be in any mode. No effect if vive-only/vr-only is false.")
            .define(true);
        messageKickDelay = builder
            .push("messageAndKickDelay")
            .comment("Seconds to wait before kicking a player or sending welcome messages. The player's client must send a Vivecraft VERSION info in that time.")
            .defineInRange(10.0, 0.0, 100.0);
        vrFun = builder
            .push("vrFun")
            .comment("Gives VR Players fun cakes and drinks at random, when they respawn.")
            .define(true);
        // end general
        builder.pop();
        
        builder
            .push("messages");
        messagesEnabled = builder
            .push("enabled")
            .comment("Enable or disable all messages.")
            .define(false);
        messagesWelcomeVR = builder
            .push("welcomeVR")
            .comment("set message to nothing to not send. ex: leaveMessage = \"\"")
            .define("%s has joined with standing VR!");
        messagesWelcomeNonVR = builder
            .push("welcomeNonVR")
            .define("%s has joined with Non-VR companion!");
        messagesWelcomeSeated = builder
            .push("welcomeSeated")
            .define("%s has joined with seated VR!");
        messagesWelcomeVanilla = builder
            .push("welcomeVanilla")
            .define("%s has joined as a Muggle!");
        messagesLeaveMessage = builder
            .push("leaveMessage")
            .define("%s has disconnected from the server!");
        messagesKickViveOnly = builder
            .push("KickViveOnly")
            .comment("The message to show kicked non vivecraft players.")
            .define("This server is configured for Vivecraft players only.");
        messagesKickVROnly = builder
            .push("KickVROnly")
            .comment("The message to show kicked non VR players.")
            .define("This server is configured for VR players only.");
        // end messages
        builder.pop();

        builder
            .push("vrChanges")
            .comment("Vanilla modifications for VR players");
        creeperSwellDistance = builder
            .push("creeperSwellDistance")
            .comment("Distance at which creepers swell and explode for VR players. Vanilla: 3")
            .defineInRange(1.75, 0.1, 10.0);

        builder
            .push("bow")
            .comment("Bow damage adjustments");
        bowStandingMultiplier = builder
            .push("standingMultiplier")
            .comment("Archery damage multiplier for Vivecraft (standing) users. Set to 1.0 to disable")
            .defineInRange(2.0, 1.0, 10.0);
        bowSeatedMultiplier = builder
            .push("seatedMultiplier")
            .comment("Archery damage multiplier for Vivecraft (seated) users. Set to 1.0 to disable")
            .defineInRange(1.0, 1.0, 10.0);
        bowStandingHeadshotMultiplier = builder
            .push("standingHeadshotMultiplier")
            .comment("Headshot damage multiplier for Vivecraft (standing) users. Set to 1.0 to disable")
            .defineInRange(3.0, 1.0, 10.0);
        bowSeatedHeadshotMultiplier = builder
            .push("seatedHeadshotMultiplier")
            .comment("Headshot damage multiplier for Vivecraft (seated) users. Set to 1.0 to disable")
            .defineInRange(2.0, 1.0, 10.0);
        bowVanillaHeadshotMultiplier = builder
            .push("vanillaHeadshotMultiplier")
            .comment("Headshot damage multiplier for Vanilla/NonVR users. Set to 1.0 to disable")
            .defineInRange(1.0, 1.0, 10.0);
        // end bow
        builder.pop();
        // end vrChanges
        builder.pop();

        builder
            .push("pvp")
            .comment("VR vs. non-VR vs. seated player PVP settings");
        pvpVRvsVR = builder
            .push("VRvsVR")
            .comment("Allows Standing VR players to damage each other.")
            .define(true);
        pvpSEATEDVRvsSEATEDVR = builder
            .push("SEATEDVRvsSEATEDVR")
            .comment("Allows Seated VR players to damage each other.")
            .define(true);
        pvpVRvsNONVR = builder
            .push("VRvsNONVR")
            .comment("Allows Standing VR players and Non VR players to damage each other.")
            .define(true);
        pvpSEATEDVRvsNONVR = builder
            .push("SEATEDVRvsNONVR")
            .comment("Allows Seated VR players and Non VR players to damage each other.")
            .define(true);
        pvpVRvsSEATEDVR = builder
            .push("VRvsSEATEDVR")
            .comment("Allows Standing VR players and Seated VR Players to damage each other.")
            .define(true);
        // end pvp
        builder.pop();

        builder
            .push("climbey")
            .comment("Climbey motion settings");
        climbeyEnabled = builder
            .push("enabled")
            .comment("Allows use of jump_boots and climb_claws.")
            .define(true);
        climbeyBlockmode = builder
            .push("blockmode")
            .comment("Sets which blocks are climb-able. Options are:\n \"DISABLED\" = List ignored. All blocks are climbable.\n \"WHITELIST\" = Only blocks on the list are climbable.\n \"BLACKLIST\" = All blocks are climbable except those on the list")
            .defineInList("DISABLED", Arrays.asList("DISABLED", "WHITELIST", "BLACKLIST"));
        climbeyBlocklist = builder
            .push("blocklist")
            .comment("The list of block names for use with include/exclude block mode.")
            .defineList(Arrays.asList("white_wool","dirt","grass_block"), (s) -> s instanceof String str && BuiltInRegistries.BLOCK.containsKey(new ResourceLocation(str)));
        // end climbey
        builder.pop();

        builder
            .push("crawling")
            .comment("Roomscale crawling settings");
        crawlingEnabled = builder.
            push("enabled")
            .comment("Allows use of roomscale crawling. Disabling does not prevent vanilla crawling.")
            .define(true);
        // end crawling
        builder.pop();

        builder
            .push("teleport")
            .comment("Teleport settings");
        teleportEnabled = builder
            .push("enabled")
            .comment("Whether direct teleport is enabled. It is recommended to leave this enabled for players prone to VR sickness.")
            .define(true);
        teleportLimitedSurvival = builder
            .push("limitedSurvival")
            .comment("Enforce limited teleport range and frequency in survival.")
            .define(false);
        teleportUpLimit = builder
            .push("upLimit")
            .comment("Maximum blocks players can teleport up. Set to 0 to disable.")
            .defineInRange(4, 1, 16);
        teleportDownLimit = builder
            .push("downLimit")
            .comment("Maximum blocks players can teleport down. Set to 0 to disable.")
            .defineInRange(4, 1, 16);
        teleportHorizontalLimit = builder
            .push("horizontalLimit")
            .comment("Maximum blocks players can teleport horizontally. Set to 0 to disable.")
            .defineInRange(16, 1, 32);
        // end teleport
        builder.pop();

        builder
            .push("worldScale")
            .comment("World scale settings");
        worldscaleLimited = builder
            .push("limitRange")
            .comment("Limit the range of world scale players can use")
            .define(false);
        worldscaleMin = builder
            .push("min")
            .comment("Lower limit of range")
            .defineInRange(0.5, 0.1, 100.0);
        worldscaleMax = builder
            .push("max")
            .comment("Upper limit of range")
            .defineInRange(2.0, 0.1, 100.0);
        // end worldScale
        builder.pop();

        builder
            .push("vrSwitching")
            .comment("VR hotswitch settings");
        vrSwitchingEnabled = builder
            .push("enabled")
            .comment("Allows players to switch between VR and NONVR on the fly.\n If disabled, they will be locked to the mode they joined with.")
            .define(true);
        // end vrSwitching
        builder.pop();

        // if the config is outdated, or is missing keys, re add them
        builder.correct(listener);
    }



}
