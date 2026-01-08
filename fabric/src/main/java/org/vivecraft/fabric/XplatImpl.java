package org.vivecraft.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.vivecraft.Xloader;
import org.vivecraft.Xplat;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.fabric.mixin.world.level.biome.BiomeAccessor;

public class XplatImpl implements Xplat {

    public static boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        return false;
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

    public static TextureAtlasSprite[] getFluidTextures(
        BlockAndTintGetter level, BlockPos pos, FluidState fluidStateIn)
    {
        if (Xloader.isModLoaded("fabric-rendering-fluids-v1")) {
            return FluidRenderHandlerRegistry.INSTANCE.get(fluidStateIn.getType())
                .getFluidSprites(level, pos, fluidStateIn);
        } else {
            // return vanilla textures
            if (fluidStateIn.is(FluidTags.LAVA)) {
                return new TextureAtlasSprite[]{
                    Minecraft.getInstance().getModelManager().getBlockModelShaper()
                        .getBlockModel(Blocks.LAVA.defaultBlockState()).particleIcon(),
                    Minecraft.getInstance().getAtlasManager().get(ModelBakery.LAVA_FLOW)
                };
            } else {
                return new TextureAtlasSprite[]{
                    Minecraft.getInstance().getModelManager().getBlockModelShaper()
                        .getBlockModel(Blocks.WATER.defaultBlockState()).particleIcon(),
                    Minecraft.getInstance().getAtlasManager().get(ModelBakery.WATER_FLOW)
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

    public static Packet<?> getC2SPacket(VivecraftPayloadC2S payload) {
        return ClientPlayNetworking.createC2SPacket(payload);
    }

    public static Packet<?> getS2CPacket(VivecraftPayloadS2C payload) {
        return ServerPlayNetworking.createS2CPacket(payload);
    }

    public static boolean hasKeyModifier(KeyMapping keyMapping) {
        return false;
    }

    public static int getKeyModifier(KeyMapping keyMapping) {
        return 0;
    }

    public static int getKeyModifierKey(KeyMapping keyMapping) {
        return -1;
    }

    public static boolean isFakePlayer(ServerPlayer player) {
        return Xloader.isModLoaded("fabric-events-interaction-v0") && player instanceof FakePlayer;
    }
}
