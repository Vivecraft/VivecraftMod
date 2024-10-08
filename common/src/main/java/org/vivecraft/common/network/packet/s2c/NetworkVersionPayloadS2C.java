package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * packet that holds the network protocol version the server will use to talk to the client
 * @param version network protocol version the server will use
 */
public record NetworkVersionPayloadS2C(int version) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.NETWORK_VERSION;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.version);
    }

    public static NetworkVersionPayloadS2C read(FriendlyByteBuf buffer) {
        return new NetworkVersionPayloadS2C(buffer.readByte() & 0xFF);
    }
}
