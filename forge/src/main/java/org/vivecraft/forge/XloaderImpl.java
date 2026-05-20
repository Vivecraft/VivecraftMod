package org.vivecraft.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import org.vivecraft.Xloader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class XloaderImpl implements Xloader {

    @Override
    public ModLoader getModloader() {
        return ModLoader.FORGE;
    }

    @Override
    public boolean isModLoaded(String name) {
        return LoadingModList.getModFileById(name) != null;
    }

    @Override
    public String getModVersion() {
        if (isModLoadedSuccess()) {
            return LoadingModList.getModFileById("vivecraft").versionString();
        }
        return "no version";
    }

    @Override
    public Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    private static Path getJarPath() {
        return LoadingModList.getModFileById("vivecraft").getFile().getSecureJar().getPath("/");
    }

    @Override
    public InputStream getInJarFile(String sourcePath) throws IOException {
        return Files.newInputStream(getJarPath().resolve(sourcePath));
    }

    @Override
    public List<Path> getInJarFolderFiles(String folder) throws IOException {
        List<Path> paths = new ArrayList<>();
        Path root = getJarPath();
        try (Stream<Path> natives = Files.list(root.resolve(folder))) {
            natives.forEach(file -> paths.add(root.relativize(file)));
        }
        return paths;
    }

    @Override
    public boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }
}
