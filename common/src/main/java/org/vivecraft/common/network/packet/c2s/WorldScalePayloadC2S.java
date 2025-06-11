package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds the clients world scale
 *
 * @param worldScale world scale set by the player
 */
public record WorldScalePayloadC2S(float worldScale) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.WORLDSCALE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeFloat(this.worldScale);
    }

    public static WorldScalePayloadC2S read(FriendlyByteBuf buffer) {
        return new WorldScalePayloadC2S(buffer.readFloat());
    }
}
