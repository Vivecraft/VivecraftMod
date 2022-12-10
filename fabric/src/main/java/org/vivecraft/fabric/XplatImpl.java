package org.vivecraft.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class XplatImpl {

    public static boolean isModLoaded(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    public static Path getConfigPath(String fileName) {
        return FabricLoader.getInstance().getConfigDir().resolve(fileName);
    }
    public static boolean isDedicatedServer() {
       return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER);
    }

    public static String getModloader() {
        return "fabric";
    }

    public static String getModVersion() {
        if (isModLoadedSuccess()) {
            return FabricLoader.getInstance().getModContainer("vivecraft").get().getMetadata().getVersion().getFriendlyString();
        }
        return "no version";
    }

    public static boolean isModLoadedSuccess() {
        return FabricLoader.getInstance().isModLoaded("vivecraft");
    }

    public static boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        return false;
    }
}
