package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds the clients current active hand, this is usually the hand that caused the next action
 * @param hand ordinal of the active hand, 0 main hand, 1 offhand
 */
public record ActiveHandPayloadC2S(byte hand) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ACTIVEHAND;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.hand);
    }

    public static ActiveHandPayloadC2S read(FriendlyByteBuf buffer) {
        return new ActiveHandPayloadC2S(buffer.readByte());
    }
}
