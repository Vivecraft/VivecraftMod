package org.vivecraft;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

/**
 * Xplat for stuff that only references modloader classes
 */
public interface Xloader {

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
    @ExpectPlatform
    static ModLoader getModloader() {
        throw new AssertionError();
    }

    /**
     * @return returns true, if the mod loader loaded everything without errors
     */
    static boolean isModLoadedSuccess() {
        return isModLoaded("vivecraft");
    }

    /**
     * @param name modId to check
     * @return if the mod {@code name} is loaded
     */
    @ExpectPlatform
    static boolean isModLoaded(String name) {
        return false;
    }

    /**
     * @return version number of the vivecraft mod
     */
    @ExpectPlatform
    static String getModVersion() {
        return "";
    }

    /**
     * asks the mod loader for the config folder, and resolves the given file there
     *
     * @param file file to get the path for
     * @return Path of {@code file} in the config folder
     */
    @ExpectPlatform
    static Path getConfigPath(String file) {
        throw new AssertionError();
    }

    /**
     * @return path to access files inside the mod jar
     */
    @ExpectPlatform
    static Path getJarPath() {
        throw new AssertionError();
    }

    /**
     * @return true if this is a dedicated server
     */
    @ExpectPlatform
    static boolean isDedicatedServer() {
        return false;
    }
}
