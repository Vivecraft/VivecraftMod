package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.Pose;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * legacy packet, holds the reversed hand flag and the main hand controller pose
 * @param reverseHands if the player has reversed hands set
 * @param controller1Pose pose of the players offhand controller
 */
public record LegacyController0DataPayloadC2S(boolean reverseHands, Pose controller1Pose) implements VivecraftPayloadC2S {
    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CONTROLLER0DATA;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.reverseHands);
        this.controller1Pose.serialize(buffer);
    }

    public static LegacyController0DataPayloadC2S read(FriendlyByteBuf buffer) {
        return new LegacyController0DataPayloadC2S(buffer.readBoolean(), Pose.deserialize(buffer));
    }
}
