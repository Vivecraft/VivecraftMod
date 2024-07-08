package org.vivecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.material.FluidState;

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

    enum ModLoader{
        FABRIC("fabric"),
        FORGE("forge"),
        NEOFORGE("neoforge");

        public final String name;

        ModLoader(String name) {
            this.name = name;
        }
    }

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
    static ModLoader getModloader() {
        throw new AssertionError();
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

    @ExpectPlatform
    static Path getJarPath() {
        return null;
    }

    @ExpectPlatform
    static String getUseMethodName() {
        return "";
    }

    @ExpectPlatform
    static TextureAtlasSprite[] getFluidTextures(BlockAndTintGetter level, BlockPos pos, FluidState fluidStateIn) {
        return new TextureAtlasSprite[]{};
    }

    @ExpectPlatform
    static Biome.ClimateSettings getBiomeClimateSettings(Biome biome) {
        return null;
    }

    @ExpectPlatform
    static BiomeSpecialEffects getBiomeEffects(Biome biome) {
        return null;
    }

    @ExpectPlatform
    static boolean serverAcceptsPacket(ClientPacketListener connection, ResourceLocation id) {
        return true;
    }
}
