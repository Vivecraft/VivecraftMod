package org.vivecraft.client.forge;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.netty.buffer.Unpooled;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.nio.file.Path;

public class XplatImpl {

    public static boolean isModLoaded(String name) {
        return FMLLoader.getLoadingModList().getModFileById(name) != null;
    }

    public static Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    public static String getModloader() {
        return "forge";
    }

    public static String getModVersion() {
        if (isModLoadedSuccess()) {
            return FMLLoader.getLoadingModList().getModFileById("vivecraft").versionString();
        }
        return "no version";
    }

    public static boolean isModLoadedSuccess() {
        return FMLLoader.getLoadingModList().getModFileById("vivecraft") != null;
    }

    public static boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        renderTarget.enableStencil();
        return true;
    }

    public static Path getJarPath() {
        return FMLLoader.getLoadingModList().getModFileById("vivecraft").getFile().getSecureJar().getPath("/");
    }

    public static String getUseMethodName() {
        return "useWithoutItem";
    }

    public static TextureAtlasSprite[] getFluidTextures(BlockAndTintGetter level, BlockPos pos, FluidState fluidStateIn) {
        return ForgeHooksClient.getFluidSprites(level, pos, fluidStateIn);
    }

    public static Biome.ClimateSettings getBiomeClimateSettings(Biome biome) {
        return biome.getModifiedClimateSettings();
    }

    public static BiomeSpecialEffects getBiomeEffects(Biome biome) {
        return biome.getModifiedSpecialEffects();
    }

    public static void addNetworkChannel(ClientPacketListener listener, ResourceLocation resourceLocation) {
        /*
        // Forge I really don't know why you are insisting on this being a DiscardedPayload
        listener.send(new ServerboundCustomPayloadPacket(new DiscardedPayload(
            new ResourceLocation("minecraft:register"),
            new FriendlyByteBuf(Unpooled.buffer())
                .writeBytes(resourceLocation.toString().getBytes()))));*/
    }
}
