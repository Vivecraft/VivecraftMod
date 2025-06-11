package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * unknown received server packet
 */
public record UnknownPayloadC2S() implements VivecraftPayloadC2S {
    @Override
    public PayloadIdentifier payloadId() {
        return null;
    }

    public static UnknownPayloadC2S read(FriendlyByteBuf buffer) {
        // discard the data
        buffer.readBytes(new byte[buffer.readableBytes()]);
        return new UnknownPayloadC2S();
    }
}
