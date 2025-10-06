package org.vivecraft.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.vivecraft.Xloader;

import java.nio.file.Path;

public class XloaderImpl implements Xloader {

    public static ModLoader getModloader() {
        return ModLoader.FORGE;
    }

    public static boolean isModLoaded(String name) {
        return FMLLoader.getLoadingModList().getModFileById(name) != null;
    }

    public static String getModVersion() {
        if (Xloader.isModLoadedSuccess()) {
            return FMLLoader.getLoadingModList().getModFileById("vivecraft").versionString();
        }
        return "no version";
    }

    public static Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    public static Path getJarPath() {
        return FMLLoader.getLoadingModList().getModFileById("vivecraft").getFile().getSecureJar().getPath("/");
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }
}
