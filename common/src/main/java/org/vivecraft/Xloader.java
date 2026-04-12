package org.vivecraft;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Xplat for stuff that only references modloader classes
 */
public interface Xloader {

    Xloader INSTANCE = Services.load(Xloader.class);

    enum ModLoader {
        FABRIC("fabric"),
        FORGE("forge"),
        NEOFORGE("neoforge"),
        QUILT("quilt");

        public final String name;

        ModLoader(String name) {
            this.name = name;
        }
    }

    /**
     * @return mod loader enum that the game is running on
     */
    ModLoader getModloader();

    /**
     * @return returns true, if the mod loader loaded everything without errors
     */
    default boolean isModLoadedSuccess() {
        return isModLoaded("vivecraft");
    }

    /**
     * @param name modId to check
     * @return if the mod {@code name} is loaded
     */
    boolean isModLoaded(String name);

    /**
     * @return version number of the vivecraft mod
     */
    String getModVersion();

    /**
     * asks the mod loader for the config folder, and resolves the given file there
     *
     * @param file file to get the path for
     * @return Path of {@code file} in the config folder
     */
    Path getConfigPath(String file);

    /**
     * @return InputStream corresponding to the given filepath inside the mod jar
     */
    InputStream getInJarFile(String sourcePath) throws IOException;

    /**
     * @return List of all files in the given folder inside the mod jar
     */
    List<Path> getInJarFolderFiles(String folder) throws IOException;

    /**
     * @return true if this is a dedicated server
     */
    boolean isDedicatedServer();
}
