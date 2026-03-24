package org.vivecraft.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import org.vivecraft.Xloader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XloaderImpl implements Xloader {

    @Override
    public ModLoader getModloader() {
        return ModLoader.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String name) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(name) != null;
    }

    @Override
    public String getModVersion() {
        if (isModLoadedSuccess()) {
            return FMLLoader.getCurrent().getLoadingModList().getModFileById("vivecraft").versionString();
        }
        return "no version";
    }

    @Override
    public Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    @Override
    public InputStream getInJarFile(String sourcePath) throws IOException {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById("vivecraft").getFile().getContents()
            .openFile(sourcePath);
    }

    @Override
    public List<Path> getInJarFolderFiles(String folder) throws IOException {
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

    @Override
    public boolean isDedicatedServer() {
        return FMLEnvironment.getDist() == Dist.DEDICATED_SERVER;
    }
}
