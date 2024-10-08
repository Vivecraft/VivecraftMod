package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

import java.util.UUID;

/**
 * packet that holds if a player switched to VR or NONVR
 * @param vr if the player is now in VR
 * @param playerID uuid of the player that switched vr state
 */
public record VRActivePayloadS2C(boolean vr, UUID playerID) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.IS_VR_ACTIVE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.vr);
        buffer.writeUUID(this.playerID);
    }

    public static VRActivePayloadS2C read(FriendlyByteBuf buffer) {
        return new VRActivePayloadS2C(buffer.readBoolean(), buffer.readUUID());
    }
}
