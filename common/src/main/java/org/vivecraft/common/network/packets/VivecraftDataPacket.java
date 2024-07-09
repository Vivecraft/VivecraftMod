package org.vivecraft.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.common.network.CommonNetworkHelper;

/**
 * Vivecraft network packed fo all kinds of stuff
 * @param packetId identifier what data this packet holds
 * @param buffer data of the packet
 */
public record VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators packetId,
                                  byte[] buffer) implements CustomPacketPayload {

    /**
     * reads and creates a VivecraftDataPacket from {@code buffer}
     * @param buffer buffer to read from
     */
    public VivecraftDataPacket(FriendlyByteBuf buffer) {
        this(CommonNetworkHelper.PacketDiscriminators.values()[buffer.readByte()], readBuffer(buffer));
    }

    /**
     * reads all data from {@code buffer} into a byte array
     * @param buffer buffer to read from
     * @return byte array with the data from {@code buffer}
     */
    private static byte[] readBuffer(FriendlyByteBuf buffer) {
        byte[] byteArr = new byte[buffer.readableBytes()];
        buffer.readBytes(byteArr);
        return byteArr;
    }

    /**
     * writes the packet to {@code buffer}
     * @param buffer buffer to write to
     */
    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(packetId.ordinal());
        buffer.writeBytes(this.buffer);
    }

    /**
     * @return ResourceLocation identifying this packet
     */
    @Override
    public ResourceLocation id() {
        return CommonNetworkHelper.CHANNEL;
    }
}
