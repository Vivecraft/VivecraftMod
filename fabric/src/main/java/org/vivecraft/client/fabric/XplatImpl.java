package org.vivecraft.client.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.vivecraft.client.Xplat;
import org.vivecraft.fabric.mixin.world.level.biome.BiomeAccessor;

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

    public static Xplat.ModLoader getModloader() {
        return Xplat.ModLoader.FABRIC;
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
            "net.minecraft.class_4970", "method_55766",
            "(Lnet/minecraft/class_2680;" +
                "Lnet/minecraft/class_1937;" +
                "Lnet/minecraft/class_2338;" +
                "Lnet/minecraft/class_1657;" +
                "Lnet/minecraft/class_3965;)" +
                "Lnet/minecraft/class_1269;");
    }

    public static TextureAtlasSprite[] getFluidTextures(BlockAndTintGetter level, BlockPos pos, FluidState fluidStateIn) {
        if (isModLoaded("fabric-rendering-fluids-v1")) {
            return FluidRenderHandlerRegistry.INSTANCE.get(fluidStateIn.getType()).getFluidSprites(level, pos, fluidStateIn);
        } else {
            // return vanilla textures
            if (fluidStateIn.is(FluidTags.LAVA)) {
                return new TextureAtlasSprite[]{
                    Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon(),
                    ModelBakery.LAVA_FLOW.sprite()
                };
            } else {
                return new TextureAtlasSprite[]{
                    Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon(),
                    ModelBakery.WATER_FLOW.sprite()
                };
            }
        }
    }

    public static Biome.ClimateSettings getBiomeClimateSettings(Biome biome) {
        return ((BiomeAccessor) (Object) biome).getClimateSettings();
    }

    public static BiomeSpecialEffects getBiomeEffects(Biome biome) {
        return biome.getSpecialEffects();
    }

    public static boolean serverAcceptsPacket(ClientPacketListener connection, ResourceLocation id) {
        return true;
    }
}
