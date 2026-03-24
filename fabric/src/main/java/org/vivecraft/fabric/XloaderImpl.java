package org.vivecraft.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.vivecraft.Xloader;
import org.vivecraft.common.utils.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class XloaderImpl implements Xloader {

    private ModLoader currentModloader = null;

    public ModLoader getModloader() {
        if (this.currentModloader == null) {
            try {
                // check if the quilt loader exists, QuiltLoaderImpl for quilt pre 0.16
                ClassUtils.getClassWithAlternative("org.quiltmc.loader.api.QuiltLoader",
                    "org.quiltmc.loader.impl.QuiltLoaderImpl");
                this.currentModloader = ModLoader.QUILT;
            } catch (ClassNotFoundException e) {
                this.currentModloader = ModLoader.FABRIC;
            }
        }
        return this.currentModloader;
    }

    @Override
    public boolean isModLoaded(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    @Override
    public String getModVersion() {
        if (isModLoadedSuccess()) {
            return FabricLoader.getInstance().getModContainer("vivecraft").get().getMetadata().getVersion()
                .getFriendlyString();
        }
        return "no version";
    }

    @Override
    public Path getConfigPath(String fileName) {
        return FabricLoader.getInstance().getConfigDir().resolve(fileName);
    }

    private static Path getJarPath() {
        return FabricLoader.getInstance().getModContainer("vivecraft").get().getRootPaths().get(0);
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
        return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER);
    }
}
