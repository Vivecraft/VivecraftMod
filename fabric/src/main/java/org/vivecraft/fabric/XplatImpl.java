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

    public static Path getJarPath() {
        return FabricLoader.getInstance().getModContainer("vivecraft").get().getRootPaths().get(0);
    }

    public static String getUseMethodName() {
        return FabricLoader.getInstance().getMappingResolver().mapMethodName(
                "intermediary",
                "net.minecraft.class_4970", "method_9534",
                "(Lnet/minecraft/class_2680;"+
                        "Lnet/minecraft/class_1937;"+
                        "Lnet/minecraft/class_2338;"+
                        "Lnet/minecraft/class_1657;"+
                        "Lnet/minecraft/class_1268;"+
                        "Lnet/minecraft/class_3965;)"+
                        "Lnet/minecraft/class_1269;");
    }
}
