package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds the clients height scale
 *
 * @param heightScale players calibrated height scale
 */
public record HeightPayloadC2S(float heightScale) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HEIGHT;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeFloat(this.heightScale);
    }

    public static HeightPayloadC2S read(FriendlyByteBuf buffer) {
        return new HeightPayloadC2S(buffer.readFloat());
    }
}
