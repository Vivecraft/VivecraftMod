package org.vivecraft.fabric.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;

/**
 * Fabric network packet
 */
public record VivecraftFabricPacketC2S(VivecraftPayloadC2S data) implements FabricPacket
{
    /**
     * Identifier for this packet
     */
    public static final PacketType<VivecraftFabricPacketC2S> TYPE =
        PacketType.create(CommonNetworkHelper.CHANNEL, VivecraftFabricPacketC2S::new);

    /**
     * reads and creates a VivecraftDataPacket from {@code buffer}
     * @param buffer buffer to read from
     */
    public VivecraftFabricPacketC2S(FriendlyByteBuf buffer) {
        this(VivecraftPayloadC2S.readPacket(buffer));
    }

    /**
     * writes the packet to {@code buffer}
     * @param buffer buffer to write to
     */
    @Override
    public void write(FriendlyByteBuf buffer) {
        this.data.write(buffer);
    }

    /**
     * @return ResourceLocation identifying this packet
     */
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
