package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.Pose;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * legacy packet, holds the seated flag and the head pose
 *
 * @param seated  if the player is in seated mode
 * @param hmdPose pose of the players headset
 */
public record LegacyHeadDataPayloadC2S(boolean seated, Pose hmdPose) implements VivecraftPayloadC2S {
    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.HEADDATA;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.seated);
        this.hmdPose.serialize(buffer);
    }

    public static LegacyHeadDataPayloadC2S read(FriendlyByteBuf buffer) {
        return new LegacyHeadDataPayloadC2S(buffer.readBoolean(), Pose.deserialize(buffer));
    }
}
