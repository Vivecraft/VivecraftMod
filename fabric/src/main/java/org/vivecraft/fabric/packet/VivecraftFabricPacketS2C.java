package org.vivecraft.fabric.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;

/**
 * Fabric network packet
 */
public record VivecraftFabricPacketS2C(VivecraftPayloadS2C data) implements FabricPacket
{
    /**
     * Identifier for this packet
     */
    public static final PacketType<VivecraftFabricPacketS2C> TYPE =
        PacketType.create(CommonNetworkHelper.CHANNEL, VivecraftFabricPacketS2C::new);

    /**
     * reads and creates a VivecraftDataPacket from {@code buffer}
     * @param buffer buffer to read from
     */
    public VivecraftFabricPacketS2C(FriendlyByteBuf buffer) {
        this(VivecraftPayloadS2C.readPacket(buffer));
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
