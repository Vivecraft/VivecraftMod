package org.vivecraft.fabric;

import net.fabricmc.loader.api.FabricLoader;
import org.vivecraft.xplat.XplatImpl;

import java.nio.file.Path;

public class FabricXplatImpl implements XplatImpl {
    @Override
    public boolean isModLoaded(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    @Override
    public Path getConfigPath(String fileName) {
        return FabricLoader.getInstance().getConfigDir().resolve(fileName);
    }
}
