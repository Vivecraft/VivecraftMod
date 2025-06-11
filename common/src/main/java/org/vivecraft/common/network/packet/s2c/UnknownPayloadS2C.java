package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * unknown received client packet
 */
public record UnknownPayloadS2C() implements VivecraftPayloadS2C {
    @Override
    public PayloadIdentifier payloadId() {
        return null;
    }

    public static UnknownPayloadS2C read(FriendlyByteBuf buffer) {
        // discard the data
        buffer.readBytes(new byte[buffer.readableBytes()]);
        return new UnknownPayloadS2C();
    }
}
