package org.vivecraft;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public interface Xplat {
    /**
     * <p>
     * This must be a <b>public static</b> method. The platform-implemented solution must be placed under a
     * platform sub-package, with its class suffixed with {@code Impl}.
     * <p>
     * Example:
     * Expect: net.examplemod.ExampleExpectPlatform#getConfigDirectory()
     * Actual Fabric: net.examplemod.fabric.ExampleExpectPlatformImpl#getConfigDirectory()
     * Actual Forge: net.examplemod.forge.ExampleExpectPlatformImpl#getConfigDirectory()
     * <p>
     * <a href="https://plugins.jetbrains.com/plugin/16210-architectury">You should also get the IntelliJ plugin to help with @ExpectPlatform.</a>
     */

    @ExpectPlatform
    static boolean isModLoaded(String name) {
        return false;
    }

    @ExpectPlatform
    static Path getConfigPath(String fileName) {
        return null;
    }

    @ExpectPlatform
    static boolean isDedicatedServer() {
        return false;
    }

    @ExpectPlatform
    static String getModloader() {
        return "";
    }

    @ExpectPlatform
    static String getModVersion() {
        return "";
    }

    @ExpectPlatform
    static boolean isModLoadedSuccess() {
        return false;
    }

    @ExpectPlatform
    static boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        return false;
    }
}
