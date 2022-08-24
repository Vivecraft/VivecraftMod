package org.vivecraft.xplat;

import org.vivecraft.fabric.FabricXplatImpl;

import java.nio.file.Path;

public interface XplatImpl {
    static XplatImpl getInstance() {
        return new FabricXplatImpl();
    }

    boolean isModLoaded(String name);
    Path getConfigPath(String fileName);
}
