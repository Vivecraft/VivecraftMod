package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * indicates that the server allows attacks while blocking
 *
 * @param allowed if attacks while blocking are allowed
 */
public record AttackWhileBlockingPayloadS2C(boolean allowed) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ATTACK_WHILE_BLOCKING;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.allowed);
    }

    public static AttackWhileBlockingPayloadS2C read(FriendlyByteBuf buffer) {
        return new AttackWhileBlockingPayloadS2C(buffer.readBoolean());
    }
}
