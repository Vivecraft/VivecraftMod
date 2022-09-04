package org.vivecraft.forge;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class XplatImpl {

    public static boolean isModLoaded(String name) {
        return FMLLoader.getLoadingModList().getModFileById(name) != null;
    }

    public static Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    public static String getModloader() {
        return "forge";
    }
    public static String getModVersion() {
        return FMLLoader.getLoadingModList().getModFileById("vivecraft").versionString();
    }
}
