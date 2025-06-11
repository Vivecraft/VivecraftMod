package org.vivecraft.neoforge.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;
import org.vivecraft.common.network.packet.VivecraftPayload;

/**
 * this is a packet that just holds the raw data, is only here to have one parent packet for client and server flow
 *
 * @param rawData raw data of the packet
 */
public record RawVivecraftPayload(byte[] rawData) implements VivecraftPayload {

    public static Type<RawVivecraftPayload> TYPE = new Type<>(CommonNetworkHelper.CHANNEL);

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBytes(this.rawData);
    }

    /**
     * returns the PacketIdentifier associated with this packet
     */
    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.values()[this.rawData[0]];
    }

    /**
     * @return this packets content as a FriendlyByteBuf, needs to be manually freed
     */
    public FriendlyByteBuf asByteBuf() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        this.write(buffer);
        return buffer;
    }

    public static RawVivecraftPayload read(FriendlyByteBuf buffer) {
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        return new RawVivecraftPayload(data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
