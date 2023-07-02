package org.vivecraft.common.serverconfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

//Not used. Possible VFE integration, but not planned for now
public class ServerConfig {

    public static BooleanValue vrOnly = new BooleanValue("vrOnly", false);
    public static DoubleValue vrOnlyKickDelay = new DoubleValue("vrOnlyKickDelay", 20D);
    public static BooleanValue printMoney = new BooleanValue("printMoney", false);
    public static StringValue vrOnlyKickMessage = new StringValue("vrOnlyKickMessage", "");
    public static BooleanValue enableJoinMessages = new BooleanValue("enableJoinMessages", true);
    public static StringValue joinMessageVR = new StringValue("joinMessageVR", "");
    public static StringValue joinMessageNonVR = new StringValue("joinMessageNonVR", "");
    public static DoubleValue creeperSwellDistance = new DoubleValue("creeperSwellDistance", 2D);
    public static BooleanValue vrVsVR = new BooleanValue("vrVsVR", true);
    public static BooleanValue vrVsSeatedVR = new BooleanValue("vrVsSeatedVR", true);
    public static BooleanValue vrVsNonVR = new BooleanValue("vrVsNonVR", true);
    public static BooleanValue seatedVrVsSeatedVR = new BooleanValue("seatedVrVsSeatedVR", true);
    public static BooleanValue seatedVrVsNonVR = new BooleanValue("seatedVrVsNonVR",true);
    public static DoubleValue bowStandingMul = new DoubleValue("bowStandingMul", 2D);
    public static DoubleValue bowSeatedMul = new DoubleValue("bowSeatedMul", 1D);
    public static DoubleValue bowStandingHeadshotMul = new DoubleValue("bowStandingHeadshotMul", 3D);
    public static DoubleValue bowSeatedHeadshotMul = new DoubleValue("bowSeatedHeadshotMul", 2D);
    public static BooleanValue climbeyEnabled = new BooleanValue("climbeyEnabled", false);
    public static StringValue blockListMode = new StringValue("blockListMode", "");
    public static List<? extends String> blockList;
    public static BooleanValue crawlingEnabled = new BooleanValue("crawlingEnabled", false);
    public static BooleanValue teleportEnabled = new BooleanValue("teleportEnabled", false);
    public static BooleanValue teleportLimited = new BooleanValue("teleportLimited", true);
    public static IntValue teleportLimitUp = new IntValue("teleportLimitUp", 5);
    public static IntValue teleportLimitDown = new IntValue("teleportLimitDown", 10);
    public static IntValue teleportLimitHoriz = new IntValue("teleportLimitHoriz", 7);
    public static BooleanValue worldScaleLimited = new BooleanValue("worldScaleLimited", false);
    public static DoubleValue worldScaleMin = new DoubleValue("worldScaleMin", 5D);
    public static DoubleValue worldScaleMax = new DoubleValue("worldScaleMax", 5D);

    static Properties properties;

    public static void loadConfig(MinecraftServer server) {
        properties = new Properties();
        try {
            Path file = server.getWorldPath(LevelResource.ROOT).resolve("serverconfigs").resolve("vivecraft-serverconfig.properties");
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            properties.load(Files.newInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static List<? extends String> getList(String blockList, List<? extends String> objects) {
        String[] array = properties.getProperty(blockList, "").replace(" ", "").split(",");
        return Arrays.asList(array);
    }

    static boolean getBoolean(String value, Boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(value, String.valueOf(defaultValue)));
    }

    static double getDouble(String value, double defaultValue) {
        return Double.parseDouble(properties.getProperty(value, String.valueOf(defaultValue)));
    }

    static int getInt(String value, int defaultValue) {
        return Integer.parseInt(properties.getProperty(value, String.valueOf(defaultValue)));
    }

    static String getString(String value, String defaultValue) {
        return properties.getProperty(value, String.valueOf(defaultValue));
    }


}
