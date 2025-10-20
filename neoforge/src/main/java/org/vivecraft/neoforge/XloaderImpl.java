package org.vivecraft.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import org.vivecraft.Xloader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    public static InputStream getInJarFile(String sourcePath) throws IOException {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById("vivecraft").getFile().getContents()
            .openFile(sourcePath);
    }

    public static List<Path> getInJarFolderFiles(String folder) throws IOException {
        List<Path> paths = new ArrayList<>();
        Path target = Path.of(folder);
        FMLLoader.getCurrent().getLoadingModList().getModFileById("vivecraft").getFile().getContents()
            .visitContent(folder, (relPath, resource) -> {
                Path file = Path.of(relPath);
                if (target.equals(file.getParent())) {
                    paths.add(file);
                }
            });
        return paths;
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }
}
