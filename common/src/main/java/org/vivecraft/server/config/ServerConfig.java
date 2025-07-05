package org.vivecraft.server.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.vivecraft.Xloader;
import org.vivecraft.server.ServerNetworking;

import java.util.Arrays;
import java.util.List;

public class ServerConfig {

    // config keys
    // general
    public static ConfigBuilder.BooleanValue DEBUG;
    public static ConfigBuilder.BooleanValue DEBUG_PARTICLES;
    public static ConfigBuilder.BooleanValue DEBUG_PARTICLES_HEAD;
    public static ConfigBuilder.BooleanValue CHECK_FOR_UPDATES;
    public static ConfigBuilder.InListValue<String> CHECK_FOR_UPDATE_TYPE;
    public static ConfigBuilder.BooleanValue VR_ONLY;
    public static ConfigBuilder.BooleanValue VIVE_ONLY;
    public static ConfigBuilder.BooleanValue ALLOW_OP;
    public static ConfigBuilder.DoubleValue MESSAGE_KICK_DELAY;
    public static ConfigBuilder.BooleanValue VR_FUN;
    public static ConfigBuilder.BooleanValue SEND_DATA_TO_OWNER;

    // messages
    public static ConfigBuilder.BooleanValue MESSAGES_ENABLED;
    public static ConfigBuilder.StringValue MESSAGES_WELCOME_VR;
    public static ConfigBuilder.StringValue MESSAGES_WELCOME_NONVR;
    public static ConfigBuilder.StringValue MESSAGES_WELCOME_SEATED;
    public static ConfigBuilder.StringValue MESSAGES_WELCOME_VANILLA;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_VR;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_NONVR;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_SEATED;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_VANILLA;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_BY_MOB_VR;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_BY_MOB_NONVR;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_BY_MOB_SEATED;
    public static ConfigBuilder.StringValue MESSAGES_DEATH_BY_MOB_VANILLA;
    public static ConfigBuilder.StringValue MESSAGES_LEAVE_MESSAGE;
    public static ConfigBuilder.StringValue MESSAGES_KICK_VIVE_ONLY;
    public static ConfigBuilder.StringValue MESSAGES_KICK_VR_ONLY;

    // vrChanges
    public static ConfigBuilder.BooleanValue DUAL_WIELDING;
    public static ConfigBuilder.DoubleValue BOOTS_ARMOR_DAMAGE;
    public static ConfigBuilder.DoubleValue CREEPER_SWELL_DISTANCE;
    public static ConfigBuilder.DoubleValue PROJECTILE_INACCURACY_MULTIPLIER;
    public static ConfigBuilder.BooleanValue ALLOW_FASTER_BLOCK_BREAKING;
    // bow
    public static ConfigBuilder.DoubleValue BOW_STANDING_MULTIPLIER;
    public static ConfigBuilder.DoubleValue BOW_SEATED_MULTIPLIER;
    public static ConfigBuilder.DoubleValue BOW_STANDING_HEADSHOT_MULTIPLIER;
    public static ConfigBuilder.DoubleValue BOW_SEATED_HEADSHOT_MULTIPLIER;
    public static ConfigBuilder.DoubleValue BOW_VANILLA_HEADSHOT_MULTIPLIER;

    // pvp
    public static ConfigBuilder.BooleanValue PVP_VR_VS_VR;
    public static ConfigBuilder.BooleanValue PVP_SEATEDVR_VS_SEATEDVR;
    public static ConfigBuilder.BooleanValue PVP_VR_VS_NONVR;
    public static ConfigBuilder.BooleanValue PVP_SEATEDVR_VS_NONVR;
    public static ConfigBuilder.BooleanValue PVP_VR_VS_SEATEDVR;
    public static ConfigBuilder.BooleanValue PVP_NOTIFY_BLOCKED_DAMAGE;

    // climbey
    public static ConfigBuilder.BooleanValue CLIMBEY_ENABLED;
    public static ConfigBuilder.EnumValue<ClimbeyBlockmode> CLIMBEY_BLOCKMODE;
    public static ConfigBuilder.ListValue<String> CLIMBEY_BLOCKLIST;

    // crawling
    public static ConfigBuilder.BooleanValue CRAWLING_ENABLED;

    // teleport
    public static ConfigBuilder.BooleanValue TELEPORT_ENABLED;
    public static ConfigBuilder.BooleanValue TELEPORT_LIMITED_SURVIVAL;
    public static ConfigBuilder.IntValue TELEPORT_UP_LIMIT;
    public static ConfigBuilder.IntValue TELEPORT_DOWN_LIMIT;
    public static ConfigBuilder.IntValue TELEPORT_HORIZONTAL_LIMIT;

    // worldscale
    public static ConfigBuilder.BooleanValue WORLDSCALE_LIMITED;
    public static ConfigBuilder.DoubleValue WORLDSCALE_MAX;
    public static ConfigBuilder.DoubleValue WORLDSCALE_MIN;

    // settingOverrides
    public static ConfigBuilder.BooleanValue FORCE_THIRD_PERSON_ITEMS;
    public static ConfigBuilder.BooleanValue FORCE_THIRD_PERSON_ITEMS_CUSTOM;

    // vr switching
    public static ConfigBuilder.BooleanValue VR_SWITCHING_ENABLED;

    private static CommentedFileConfig CONFIG;
    private static ConfigBuilder BUILDER;

    public static List<ConfigBuilder.ConfigValue> getConfigValues() {
        return BUILDER.getConfigValues();
    }

    public static void init(ConfigSpec.CorrectionListener listener) {
        Config.setInsertionOrderPreserved(true);
        if (CONFIG != null) {
            // make sure to close the old one when reloading
            CONFIG.close();
        }
        CONFIG = CommentedFileConfig
            .builder(Xloader.getConfigPath("vivecraft-server-config.toml"))
            .autosave()
            .sync()
            .concurrent()
            .build();

        CONFIG.load();

        if (listener == null) {
            listener = (action, path, incorrectValue, correctedValue) -> {
                if (incorrectValue != null) {
                    ServerNetworking.LOGGER.info("Vivecraft: Corrected setting '{}': was '{}', is now '{}'",
                        String.join(".", path),
                        incorrectValue, correctedValue);
                }
            };
        }

        fixConfig(CONFIG, listener);

        CONFIG.save();
    }

    private static void fixConfig(CommentedConfig config, ConfigSpec.CorrectionListener listener) {

        BUILDER = new ConfigBuilder(config, new ConfigSpec());

        BUILDER
            .push("general");
        DEBUG = BUILDER
            .push("debug")
            .define(false);
        CHECK_FOR_UPDATES = BUILDER
            .push("checkForUpdate")
            .define(true);
        CHECK_FOR_UPDATE_TYPE = BUILDER
            .push("checkForUpdateType")
            .defineInList("r", Arrays.asList("r", "b", "a"));
        VR_ONLY = BUILDER
            .push("vr_only")
            .define(false);
        VIVE_ONLY = BUILDER
            .push("vive_only")
            .define(false);
        ALLOW_OP = BUILDER
            .push("allow_op")
            .define(true);
        MESSAGE_KICK_DELAY = BUILDER
            .push("messageAndKickDelay")
            .defineInRange(10.0, 0.0, 100.0);
        VR_FUN = BUILDER
            .push("vrFun")
            .define(true);
        SEND_DATA_TO_OWNER = BUILDER
            .push("sendDataToOwner")
            .define(false);
        // end general
        BUILDER.pop();

        BUILDER
            .push("messages", true);
        MESSAGES_ENABLED = BUILDER
            .push("enabled")
            .define(false);

        // welcome messages
        MESSAGES_WELCOME_VR = BUILDER
            .push("welcomeVR")
            .define("%s has joined with standing VR!");
        MESSAGES_WELCOME_NONVR = BUILDER
            .push("welcomeNonVR")
            .define("%s has joined with Non-VR companion!");
        MESSAGES_WELCOME_SEATED = BUILDER
            .push("welcomeSeated")
            .define("%s has joined with seated VR!");
        MESSAGES_WELCOME_VANILLA = BUILDER
            .push("welcomeVanilla")
            .define("%s has joined as a Muggle!");

        MESSAGES_LEAVE_MESSAGE = BUILDER
            .push("leaveMessage")
            .define("%s has disconnected from the server!");

        // general death messages
        MESSAGES_DEATH_VR = BUILDER
            .push("deathVR")
            .define("%s died in standing VR!");
        MESSAGES_DEATH_NONVR = BUILDER
            .push("deathNonVR")
            .define("%s died in Non-VR companion!");
        MESSAGES_DEATH_SEATED = BUILDER
            .push("deathSeated")
            .define("%s died in seated VR!");
        MESSAGES_DEATH_VANILLA = BUILDER
            .push("deathVanilla")
            .define("%s died as a Muggle!");

        // death messages by mobs
        MESSAGES_DEATH_BY_MOB_VR = BUILDER
            .push("deathByMobVR")
            .define("%1$s was slain by %2$s in standing VR!");
        MESSAGES_DEATH_BY_MOB_NONVR = BUILDER
            .push("deathByMobNonVR")
            .define("%1$s was slain by %2$s in Non-VR companion!");
        MESSAGES_DEATH_BY_MOB_SEATED = BUILDER
            .push("deathByMobSeated")
            .define("%1$s was slain by %2$s in seated VR!");
        MESSAGES_DEATH_BY_MOB_VANILLA = BUILDER
            .push("deathByMobVanilla")
            .define("%1$s was slain by %2$s as a Muggle!");

        // kick messages
        MESSAGES_KICK_VIVE_ONLY = BUILDER
            .push("KickViveOnly")
            .define("This server is configured for Vivecraft players only.");
        MESSAGES_KICK_VR_ONLY = BUILDER
            .push("KickVROnly")
            .define("This server is configured for VR players only.");
        // end messages
        BUILDER.pop();

        BUILDER
            .push("vrChanges");
        CREEPER_SWELL_DISTANCE = BUILDER
            .push("creeperSwellDistance")
            .defineInRange(1.75, 0.1, 10.0);
        DUAL_WIELDING = BUILDER
            .push("dualWielding")
            .define(true);
        BOOTS_ARMOR_DAMAGE = BUILDER
            .push("bootsArmorDamage")
            .defineInRange(0.0, 0.0, 5.0);
        PROJECTILE_INACCURACY_MULTIPLIER = BUILDER
            .push("projectileInaccuracyMultiplier")
            .defineInRange(1.0, 0.0, 1.0);
        ALLOW_FASTER_BLOCK_BREAKING = BUILDER
            .push("allowFasterBlockBreaking")
            .define(true);

        BUILDER
            .push("bow");
        BOW_STANDING_MULTIPLIER = BUILDER
            .push("standingMultiplier")
            .defineInRange(2.0, 1.0, 10.0);
        BOW_SEATED_MULTIPLIER = BUILDER
            .push("seatedMultiplier")
            .defineInRange(1.0, 1.0, 10.0);
        BOW_STANDING_HEADSHOT_MULTIPLIER = BUILDER
            .push("standingHeadshotMultiplier")
            .defineInRange(3.0, 1.0, 10.0);
        BOW_SEATED_HEADSHOT_MULTIPLIER = BUILDER
            .push("seatedHeadshotMultiplier")
            .defineInRange(2.0, 1.0, 10.0);
        BOW_VANILLA_HEADSHOT_MULTIPLIER = BUILDER
            .push("vanillaHeadshotMultiplier")
            .defineInRange(1.0, 1.0, 10.0);
        // end bow
        BUILDER.pop();
        // end vrChanges
        BUILDER.pop();

        BUILDER
            .push("pvp");
        PVP_NOTIFY_BLOCKED_DAMAGE = BUILDER
            .push("notifyBlockedDamage")
            .define(false);
        PVP_VR_VS_VR = BUILDER
            .push("VRvsVR")
            .define(true);
        PVP_SEATEDVR_VS_SEATEDVR = BUILDER
            .push("SEATEDVRvsSEATEDVR")
            .define(true);
        PVP_VR_VS_NONVR = BUILDER
            .push("VRvsNONVR")
            .define(true);
        PVP_SEATEDVR_VS_NONVR = BUILDER
            .push("SEATEDVRvsNONVR")
            .define(true);
        PVP_VR_VS_SEATEDVR = BUILDER
            .push("VRvsSEATEDVR")
            .define(true);
        // end pvp
        BUILDER.pop();

        BUILDER
            .push("climbey");
        CLIMBEY_ENABLED = BUILDER
            .push("enabled")
            .define(true);
        CLIMBEY_BLOCKMODE = BUILDER
            .push("blockmode")
            .defineEnum(ClimbeyBlockmode.DISABLED, ClimbeyBlockmode.class);
        CLIMBEY_BLOCKLIST = BUILDER
            .push("blocklist")
            .defineList(Arrays.asList("white_wool", "dirt", "grass_block"), (s) -> {
                boolean valid = true;
                try {
                    // check if valid block
                    Holder.Reference<Block> b = BuiltInRegistries.BLOCK.get(ResourceLocation.parse((String) s))
                        .orElseGet(() -> null);
                    if (b == null || b.value() == Blocks.AIR) {
                        valid = false;
                    }
                } catch (ResourceLocationException e) {
                    valid = false;
                }
                if (!valid) {
                    ServerNetworking.LOGGER.error("Vivecraft: Ignoring invalid/unknown block in climbey blocklist: {}",
                        s);
                }
                // return true or the whole list would be reset
                return true;
            });
        // end climbey
        BUILDER.pop();

        BUILDER
            .push("crawling");
        CRAWLING_ENABLED = BUILDER
            .push("enabled")
            .define(true);
        // end crawling
        BUILDER.pop();

        BUILDER
            .push("teleport");
        TELEPORT_ENABLED = BUILDER
            .push("enabled")
            .define(true);
        TELEPORT_LIMITED_SURVIVAL = BUILDER
            .push("limitedSurvival")
            .define(false);
        TELEPORT_UP_LIMIT = BUILDER
            .push("upLimit")
            .defineInRange(4, 1, 16);
        TELEPORT_DOWN_LIMIT = BUILDER
            .push("downLimit")
            .defineInRange(4, 1, 16);
        TELEPORT_HORIZONTAL_LIMIT = BUILDER
            .push("horizontalLimit")
            .defineInRange(16, 1, 32);
        // end teleport
        BUILDER.pop();

        BUILDER
            .push("worldScale");
        WORLDSCALE_LIMITED = BUILDER
            .push("limitRange")
            .define(false);
        WORLDSCALE_MIN = BUILDER
            .push("min")
            .defineInRange(0.5, 0.1, 100.0);
        WORLDSCALE_MAX = BUILDER
            .push("max")
            .defineInRange(2.0, 0.1, 100.0);
        // end worldScale
        BUILDER.pop();

        BUILDER
            .push("settingOverrides");
        FORCE_THIRD_PERSON_ITEMS = BUILDER
            .push("thirdPersonItems")
            .define(false);
        FORCE_THIRD_PERSON_ITEMS_CUSTOM = BUILDER
            .push("thirdPersonItemsCustom")
            .define(false);
        // end settingOverrides
        BUILDER.pop();

        BUILDER
            .push("vrSwitching");
        VR_SWITCHING_ENABLED = BUILDER
            .push("enabled")
            .define(true);
        // end vrSwitching
        BUILDER.pop();

        BUILDER
            .push("debug");
        DEBUG_PARTICLES = BUILDER
            .push("debugParticles")
            .define(false);
        DEBUG_PARTICLES_HEAD = BUILDER
            .push("debugParticlesHead")
            .define(false);
        BUILDER.pop();

        // fix any enums that are loaded as strings first
        for (ConfigBuilder.ConfigValue<?> configValue : BUILDER.getConfigValues()) {
            if (configValue instanceof ConfigBuilder.EnumValue enumValue && enumValue.get() != null) {
                enumValue.set(enumValue.getEnumValue(enumValue.get()));
            }
        }

        // if the config is outdated, or is missing keys, re add them
        BUILDER.correct(listener);
    }
}
