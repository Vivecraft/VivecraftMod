package org.vivecraft.fabric;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.vivecraft.Xloader;
import org.vivecraft.Xplat;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.fabric.mixin.world.level.biome.BiomeAccessor;

public class XplatImpl implements Xplat {

    @Override
    public boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        return false;
    }

    @Override
    public Biome.ClimateSettings getBiomeClimateSettings(Biome biome) {
        return ((BiomeAccessor) (Object) biome).getClimateSettings();
    }

    @Override
    public BiomeSpecialEffects getBiomeEffects(Biome biome) {
        return biome.getSpecialEffects();
    }

    @Override
    public boolean serverAcceptsPacket(ClientPacketListener connection, Identifier id) {
        return true;
    }

    @Override
    public Packet<?> getC2SPacket(VivecraftPayloadC2S payload) {
        return ClientPlayNetworking.createServerboundPacket(payload);
    }

    @Override
    public Packet<?> getS2CPacket(VivecraftPayloadS2C payload) {
        return ServerPlayNetworking.createClientboundPacket(payload);
    }

    @Override
    public boolean hasKeyModifier(KeyMapping keyMapping) {
        return false;
    }

    @Override
    public int getKeyModifier(KeyMapping keyMapping) {
        return 0;
    }

    @Override
    public int getKeyModifierKey(KeyMapping keyMapping) {
        return -1;
    }

    @Override
    public boolean isFakePlayer(ServerPlayer player) {
        return Xloader.INSTANCE.isModLoaded("fabric-events-interaction-v0") && player instanceof FakePlayer;
    }
}
