package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds weather the next attack is a roomscale attack
 *
 * @param isRoomscaleAttack if the next attack is roomscale
 * @param hitsMade          number of ticks worth of hits made during this roomscale attack, only valide if {@code isRoomscaleAttack} is false
 */
public record RoomscaleAttackPayloadC2S(boolean isRoomscaleAttack, int hitsMade) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ROOMSCALE_ATTACK;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.isRoomscaleAttack);
        buffer.writeByte(this.hitsMade);
    }

    public static RoomscaleAttackPayloadC2S read(FriendlyByteBuf buffer) {
        return new RoomscaleAttackPayloadC2S(buffer.readBoolean(), buffer.readByte());
    }
}
