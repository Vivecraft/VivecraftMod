package org.vivecraft;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

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
     * @return InputStream corresponding to the given filepath inside the mod jar
     */
    @ExpectPlatform
    static InputStream getInJarFile(String sourcePath) throws IOException {
        throw new AssertionError();
    }

    /**
     * @return List of all files in the given folder inside the mod jar
     */
    @ExpectPlatform
    static List<Path> getInJarFolderFiles(String folder) throws IOException {
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
