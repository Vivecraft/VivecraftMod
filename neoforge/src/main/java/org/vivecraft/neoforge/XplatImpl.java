package org.vivecraft.neoforge;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.Xplat;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.neoforge.packet.VivecraftPayloadBiDir;

public class XplatImpl implements Xplat {

    @Override
    public boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        // TODO there is no stencil support yet
        //renderTarget.enableStencil();
        return true;
    }

    @Override
    public Biome.ClimateSettings getBiomeClimateSettings(Biome biome) {
        return biome.getModifiedClimateSettings();
    }

    @Override
    public BiomeSpecialEffects getBiomeEffects(Biome biome) {
        return biome.getModifiedSpecialEffects();
    }

    @Override
    public boolean serverAcceptsPacket(ClientPacketListener connection, Identifier id) {
        return connection.hasChannel(id);
    }

    @Override
    public Packet<?> getC2SPacket(VivecraftPayloadC2S payload) {
        return new ServerboundCustomPayloadPacket(new VivecraftPayloadBiDir(payload));
    }

    @Override
    public Packet<?> getS2CPacket(VivecraftPayloadS2C payload) {
        return new ClientboundCustomPayloadPacket(new VivecraftPayloadBiDir(payload));
    }

    @Override
    public boolean hasKeyModifier(KeyMapping keyMapping) {
        return keyMapping.getKeyModifier() != KeyModifier.NONE;
    }

    @Override
    public int getKeyModifier(KeyMapping keyMapping) {
        return switch (keyMapping.getKeyModifier()) {
            case SHIFT -> GLFW.GLFW_MOD_SHIFT;
            case ALT -> GLFW.GLFW_MOD_ALT;
            case CONTROL -> GLFW.GLFW_MOD_CONTROL;
            default -> 0;
        };
    }

    @Override
    public int getKeyModifierKey(KeyMapping keyMapping) {
        return switch (keyMapping.getKeyModifier()) {
            case SHIFT -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case ALT -> GLFW.GLFW_KEY_RIGHT_ALT;
            case CONTROL -> GLFW.GLFW_KEY_LEFT_CONTROL;
            default -> -1;
        };
    }

    @Override
    public boolean isFakePlayer(ServerPlayer player) {
        return player instanceof FakePlayer;
    }
}
