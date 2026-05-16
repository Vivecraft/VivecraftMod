package org.vivecraft.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.vivecraft.Xloader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    private static Path getJarPath() {
        return FMLLoader.getLoadingModList().getModFileById("vivecraft").getFile().getSecureJar().getPath("/");
    }

    public static InputStream getInJarFile(String sourcePath) throws IOException {
        return Files.newInputStream(getJarPath().resolve(sourcePath));
    }

    public static List<Path> getInJarFolderFiles(String folder) throws IOException {
        List<Path> paths = new ArrayList<>();
        Path root = getJarPath();
        try (Stream<Path> natives = Files.list(root.resolve(folder))) {
            natives.forEach(file -> paths.add(root.relativize(file)));
        }
        return paths;
    }


    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }
}
