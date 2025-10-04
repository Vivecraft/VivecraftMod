package org.vivecraft.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import org.vivecraft.Xloader;

import java.nio.file.Path;

public class XloaderImpl implements Xloader {

    public static ModLoader getModloader() {
        return ModLoader.NEOFORGE;
    }

    public static boolean isModLoaded(String name) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(name) != null;
    }

    public static String getModVersion() {
        if (Xloader.isModLoadedSuccess()) {
            return FMLLoader.getCurrent().getLoadingModList().getModFileById("vivecraft").versionString();
        }
        return "no version";
    }

    public static Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    public static Path getJarPath() {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById("vivecraft").getFile().getSecureJar()
            .getPrimaryPath();
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.getDist() == Dist.DEDICATED_SERVER;
    }
}
