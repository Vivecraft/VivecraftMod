package org.vivecraft.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class XplatImpl {

    public static boolean isModLoaded(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    public static Path getConfigPath(String fileName) {
        return FabricLoader.getInstance().getConfigDir().resolve(fileName);
    }
}
