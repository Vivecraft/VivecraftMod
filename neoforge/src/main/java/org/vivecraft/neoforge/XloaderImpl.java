package org.vivecraft.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import org.vivecraft.Xloader;

import java.nio.file.Path;

public class XloaderImpl implements Xloader {

    public static ModLoader getModloader() {
        return ModLoader.NEOFORGE;
    }

    public static boolean isModLoaded(String name) {
        return LoadingModList.get().getModFileById(name) != null;
    }

    public static String getModVersion() {
        if (Xloader.isModLoadedSuccess()) {
            return LoadingModList.get().getModFileById("vivecraft").versionString();
        }
        return "no version";
    }

    public static Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    public static Path getJarPath() {
        return LoadingModList.get().getModFileById("vivecraft").getFile().getSecureJar().getPath("/");
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }
}
