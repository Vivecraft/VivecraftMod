package org.vivecraft;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;

public interface Xplat {

    Xplat INSTANCE = Services.load(Xplat.class);

    /**
     * asks the mod loader to enable the stencil for the given RenderTarget
     *
     * @param renderTarget RenderTarget to enable the Stencil on
     * @return true if the mod loader enabled the stencil
     */
    boolean enableRenderTargetStencil(RenderTarget renderTarget);

    /**
     * @param biome Biome to get the ClimateSettings from
     * @return ClimateSettings of the given Biome
     */
    Biome.ClimateSettings getBiomeClimateSettings(Biome biome);

    /**
     * @param biome Biome to get the BiomeSpecialEffects from
     * @return BiomeSpecialEffects of the given Biome
     */
    BiomeSpecialEffects getBiomeEffects(Biome biome);

    /**
     * check if packets of the given channel id are allowed to be sent
     *
     * @param connection connection to check for
     * @param id         channel id to check
     * @return if the connection accepts packets of the given id
     */
    boolean serverAcceptsPacket(ClientPacketListener connection, Identifier id);

    /**
     * wraps the given payload into the mod loader specific packet
     *
     * @param payload payload to wrap
     * @return ServerboundCustomPayloadPacket
     */
    Packet<?> getC2SPacket(VivecraftPayloadC2S payload);

    /**
     * wraps the given payload into the mod loader specific packet
     *
     * @param payload payload to wrap
     * @return ClientboundCustomPayloadPacket
     */
    Packet<?> getS2CPacket(VivecraftPayloadS2C payload);

    /**
     * checks if the given KeyMapping uses a key modifier to trigger
     *
     * @param keyMapping KeyMapping to check
     * @return true if a key modifier is used
     */
    boolean hasKeyModifier(KeyMapping keyMapping);

    /**
     * gets the key modifier for the given KeyMapping
     *
     * @param keyMapping KeyMapping to check
     * @return one of the GLFW_MOD_X modifiers, or 0 if there is none
     */
    int getKeyModifier(KeyMapping keyMapping);

    /**
     * gets the key that corresponds to the key modifier for the given KeyMapping
     *
     * @param keyMapping KeyMapping to check
     * @return one of the GLFW_KEY_X keys, or -1 if there is none
     */
    int getKeyModifierKey(KeyMapping keyMapping);

    /**
     * checks if the given player is a fake player, instead of an actual player
     *
     * @param player player to check
     * @return {@code true} when it is a fake player
     */
    boolean isFakePlayer(ServerPlayer player);
}
