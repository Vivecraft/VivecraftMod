package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds the clients wanted teleport position
 * @param x x coordinate the player want to teleport to
 * @param y y coordinate the player want to teleport to
 * @param z z coordinate the player want to teleport to
 */
public record TeleportPayloadC2S(float x, float y, float z) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.TELEPORT;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
        buffer.writeFloat(this.z);
    }

    public static TeleportPayloadC2S read(FriendlyByteBuf buffer) {
        return new TeleportPayloadC2S(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }
}
