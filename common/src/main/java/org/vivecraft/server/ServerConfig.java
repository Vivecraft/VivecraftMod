package org.vivecraft.server;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.client.Xplat;

import java.util.*;
import java.util.function.Predicate;

public class ServerConfig {

    // config keys
    public static final String debug                         = "general.debug";
    public static final String checkForUpdate                = "general.checkForUpdate";
    public static final String vr_only                       = "general.vr_only";
    public static final String vive_only                     = "general.vive_only";
    public static final String allow_op                      = "general.allow_op";
    public static final String messageKickDelay              = "general.messageAndKickDelay";
    public static final String vrFun                         = "general.vrFun";

    public static final String messagesEnabled               = "messages.enabled";
    public static final String messagesWelcomeVR             = "messages.welcomeVR";
    public static final String messagesWelcomeNonVR          = "messages.welcomeNonVR";
    public static final String messagesWelcomeSeated         = "messages.welcomeSeated";
    public static final String messagesWelcomeVanilla        = "messages.welcomeVanilla";
    public static final String messagesLeaveMessage          = "messages.leaveMessage";
    public static final String messagesKickViveOnly          = "messages.KickViveOnly";
    public static final String messagesKickVROnly            = "messages.KickVROnly";

    public static final String creeperSwellDistance          = "vrChanges.creeperSwellDistance";
    public static final String bowStandingMultiplier         = "vrChanges.bow.standingMultiplier";
    public static final String bowSeatedMultiplier           = "vrChanges.bow.seatedMultiplier";
    public static final String bowStandingHeadshotMultiplier = "vrChanges.bow.standingHeadshotMultiplier";
    public static final String bowSeatedHeadshotMultiplier   = "vrChanges.bow.seatedHeadshotMultiplier";
    public static final String bowVanillaHeadshotMultiplier = "vrChanges.bow.vanillaHeadshotMultiplier";

    public static final String pvpVRvsVR                     = "pvp.VRvsVR";
    public static final String pvpSEATEDVRvsSEATEDVR         = "pvp.SEATEDVRvsSEATEDVR";
    public static final String pvpVRvsNONVR                  = "pvp.VRvsNONVR";
    public static final String pvpSEATEDVRvsNONVR            = "pvp.SEATEDVRvsNONVR";
    public static final String pvpVRvsSEATEDVR               = "pvp.VRvsSEATEDVR";

    public static final String climbeyEnabled                = "climbey.enabled";
    public static final String climbeyBlockmode              = "climbey.blockmode";
    public static final String climbeyBlocklist              = "climbey.blocklist";

    public static final String crawlingEnabled               = "crawling.enabled";

    public static final String teleportEnabled               = "teleport.enabled";
    public static final String teleportLimitedSurvival       = "teleport.limitedSurvival";
    public static final String teleportUpLimit               = "teleport.upLimit";
    public static final String teleportDownLimit             = "teleport.downLimit";
    public static final String teleportHorizontalLimit       = "teleport.horizontalLimit";

    public static final String worldscaleLimited             = "worldScale.limitRange";
    public static final String worldscaleMax                 = "worldScale.max";
    public static final String worldscaleMin                 = "worldScale.min";

    public static final String vrSwitchingEnabled            = "vrSwitching.enabled";

    private static CommentedFileConfig config;
    private static ConfigSpec spec;

    public static List<String> getSettings(){
        return Arrays.asList(debug, checkForUpdate, vr_only, vive_only, allow_op, messageKickDelay, vrFun,
                messagesEnabled, messagesWelcomeVR, messagesWelcomeNonVR, messagesWelcomeSeated,
                messagesWelcomeVanilla, messagesLeaveMessage, messagesKickViveOnly, messagesKickVROnly,
                creeperSwellDistance, bowStandingMultiplier, bowSeatedMultiplier, bowStandingHeadshotMultiplier,
                bowSeatedHeadshotMultiplier, bowVanillaHeadshotMultiplier,
                pvpVRvsVR, pvpSEATEDVRvsSEATEDVR, pvpVRvsNONVR, pvpVRvsSEATEDVR, pvpSEATEDVRvsNONVR,
                climbeyEnabled, climbeyBlockmode, climbeyBlocklist,
                crawlingEnabled,
                teleportEnabled, teleportLimitedSurvival, teleportUpLimit, teleportDownLimit,
                teleportHorizontalLimit,
                worldscaleLimited, worldscaleMin, worldscaleMax,
                vrSwitchingEnabled);
    }

    public static void init(ConfigSpec.CorrectionListener listener){
        Config.setInsertionOrderPreserved(true);
        config = CommentedFileConfig
                .builder(Xplat.getConfigPath("vivecraft-server-config.toml"))
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

    public static void setSetting(String key, Object value) {
        config.set(key, value);
    }

    public static boolean getBoolean(String key) {
        return config.get(key);
    }

    public static String getString(String key) {
        return config.get(key);
    }

    public static int getInt(String key) {
        return config.get(key);
    }

    public static double getDouble(String key) {
        return config.get(key);
    }

    public static int getOrdinal(String key) {
        return ((Enum)config.get(key)).ordinal();
    }

    public static List<String> getList(String key) {
        return config.get(key);
    }

    public static Object getObject(String key) {
        return config.get(key);
    }

    public static Object resetOption(String key){
        Object defaultValue = spec.correct(key, null);
        config.set(key, defaultValue);
        return defaultValue;
    }

    private static void fixConfig(CommentedConfig config, ConfigSpec.CorrectionListener listener) {
        spec = new ConfigSpec();

        addBool(config, spec, debug, false,
                "will print clients that connect with vivecraft, and what version they are using, to the log");
        addBool(config, spec, checkForUpdate, true,
                "will check for a newer version and alert any OP when they login to the server");
        addBool(config, spec, vr_only, false,
                "Set to true to only allow VR players to play.");
        addBool(config, spec, vive_only, false,
                "Set to true to only allow vivecraft players to play.");
        addBool(config, spec, allow_op, true,
                "If true, will allow server ops to be in any mode. No effect if vive-only/vr-only is false.");
        addDouble(config, spec, messageKickDelay, 10.0, 0.0, 1000,
                "Seconds to wait before kicking a player or sending welcome messages. The player's client must send a Vivecraft VERSION info in that time.");


        addBool(config, spec, messagesEnabled, false,
                "Enable or disable all messages.");
        addString(config, spec, messagesWelcomeVR, "%s has joined with standing VR!",
                "set message to nothing to not send. ex: leaveMessage = \"\"");
        spec.define(messagesWelcomeNonVR, "%s has joined with Non-VR companion!");
        spec.define(messagesWelcomeSeated, "%s has joined with seated VR!");
        spec.define(messagesWelcomeVanilla, "%s has joined as a Muggle!");
        spec.define(messagesLeaveMessage, "%s has disconnected from the server!");
        addString(config, spec, messagesKickViveOnly, "This server is configured for Vivecraft players only.",
            "The message to show kicked non vivecraft players.");
        addString(config, spec, messagesKickVROnly, "This server is configured for VR players only.",
            "The message to show kicked non VR players.");
        addBool(config, spec, vrFun, true,
            "Gives Players fun cakes and drinks at random, when they respawn");


        config.setComment("vrChanges", "Vanilla modifications for VR players");
        addDouble(config, spec, creeperSwellDistance, 1.75, 0.1, 10.0,
                "Distance at which creepers swell and explode for VR players. Vanilla: 3");

        config.setComment("vrChanges.bow", "Bow damage adjustments");
        addDouble(config, spec, bowStandingMultiplier, 2.0, 1.0, 10.0,
                "Archery damage multiplier for Vivecraft (standing) users. Set to 1 to disable");
        addDouble(config, spec, bowSeatedMultiplier, 1.0, 1.0, 10.0,
                "Archery damage multiplier for Vivecraft (seated) users. Set to 1 to disable");
        addDouble(config, spec, bowStandingHeadshotMultiplier, 3.0, 1.0, 10.0,
                "Headshot damage multiplier for Vivecraft (standing) users. Set to 1 to disable");
        addDouble(config, spec, bowSeatedHeadshotMultiplier, 2.0, 1.0, 10.0,
                "Headshot damage multiplier for Vivecraft (seated) users. Set to 1 to disable");
        addDouble(config, spec, bowVanillaHeadshotMultiplier, 1.0, 1.0, 10.0,
            "Headshot damage multiplier for Vanilla/NonVR users. Set to 1 to disable");


        config.setComment("pvp", "VR vs. non-VR vs. seated player PVP settings");
        addBool(config, spec, pvpVRvsVR, true,
                "Allows Standing VR players to damage each other.");
        addBool(config, spec, pvpSEATEDVRvsSEATEDVR, true,
            "Allows Seated VR players to damage each other.");
        addBool(config, spec, pvpVRvsNONVR, true,
                "Allows Standing VR players and Non VR players to damage each other.");
        addBool(config, spec, pvpSEATEDVRvsNONVR, true,
                "Allows Seated VR players and Non VR players to damage each other.");
        addBool(config, spec, pvpVRvsSEATEDVR, true,
                "Allows Standing VR players and Seated VR Players to damage each other.");


        config.setComment("climbey", "Climbey motion settings");
        addBool(config, spec, climbeyEnabled, true,
                "Allows use of jump_boots and climb_claws.");
        addInList(config, spec, climbeyBlockmode, "DISABLED", Arrays.asList("DISABLED", "WHITELIST", "BLACKLIST"),
                "Sets which blocks are climb-able. Options are:\n \"DISABLED\" = List ignored. All blocks are climbable.\n \"WHITELIST\" = Only blocks on the list are climbable.\n \"BLACKLIST\" = All blocks are climbable except those on the list");
        addList(config, spec, climbeyBlocklist, Arrays.asList("white_wool","dirt","grass_block"),
                (s) -> s instanceof String && BuiltInRegistries.BLOCK.containsKey(new ResourceLocation((String) s)),
                "The list of block names for use with include/exclude block mode.");


        config.setComment("crawling", "Roomscale crawling settings");
        addBool(config, spec, crawlingEnabled, true,
                "Allows use of roomscale crawling. Disabling does not prevent vanilla crawling.");


        config.setComment("teleport", "Teleport settings");
        addBool(config, spec, teleportEnabled, true,
                "Whether direct teleport is enabled. It is recommended to leave this enabled for players prone to VR sickness.");
        addBool(config, spec, teleportLimitedSurvival, false,
                "Enforce limited teleport range and frequency in survival");
        addInt(config, spec, teleportUpLimit, 1, 1, 4,
                "Maximum blocks players can teleport up. Set to 0 to disable.");
        addInt(config, spec, teleportDownLimit, 4, 1, 16,
                "Maximum blocks players can teleport down. Set to 0 to disable.");
        addInt(config, spec, teleportHorizontalLimit, 16, 1, 32,
                "Maximum blocks players can teleport horizontally. Set to 0 to disable.");


        config.setComment("worldScale", "World scale settings");

        addBool(config, spec, worldscaleLimited, false,
                "Limit the range of world scale players can use");
        addDouble(config, spec, worldscaleMax, 2.0, 0.1, 100.0,
                "Upper limit of range");
        addDouble(config, spec, worldscaleMin, 0.5, 0.1, 100.0,
                "Lower limit of range");


        config.setComment("vrSwitching", "VR hotswitch settings");
        addBool(config, spec, vrSwitchingEnabled, true,
                "Allows players to switch between VR and NONVR on the fly.\n If disabled, they will be locked to the mode they joined with.");

        // if the config is outdated, or is missing keys, re add them
        spec.correct(config, listener);
    }

    private static void addBool(CommentedConfig config, ConfigSpec spec, String key, boolean defaultValue, String comment) {
        config.setComment(key, comment + "\n default: " + defaultValue);
        spec.define(key, defaultValue);
    }

    private static void addString(CommentedConfig config, ConfigSpec spec, String key, String defaultValue, String comment) {
        config.setComment(key, comment);
        spec.define(key, defaultValue);
    }

    private static void addInt(CommentedConfig config, ConfigSpec spec, String key, int defaultValue, int min, int max, String comment) {
        config.setComment(key, comment + "\n default: %d, min: %d, max: %d".formatted(defaultValue, min, max));
        spec.defineInRange(key, defaultValue, min, max);
    }

    private static void addDouble(CommentedConfig config, ConfigSpec spec, String key, double defaultValue, double min, double max, String comment) {
        config.setComment(key, comment + new Formatter(Locale.US).format("\n default: %.2f, min: %.2f, max: %.2f", defaultValue, min, max));
        spec.defineInRange(key, defaultValue, min, max);
    }
    private static void addInList(CommentedConfig config, ConfigSpec spec, String key, Object defaultValue, List<Object> values, String comment) {
        config.setComment(key, comment);
        spec.defineInList(key, defaultValue, values);
    }

    private static void addList(CommentedConfig config, ConfigSpec spec, String key, List<Object> defaultValue, Predicate<Object> validator, String comment) {
        config.setComment(key, comment);
        spec.defineList(key, defaultValue, validator);
    }



}
