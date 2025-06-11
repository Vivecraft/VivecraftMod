package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * packet that holds if the server allows switching between VR and NONVR
 *
 * @param allowed if hot switching is allowed
 */
public record VRSwitchingPayloadS2C(boolean allowed) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.VR_SWITCHING;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.allowed);
    }

    public static VRSwitchingPayloadS2C read(FriendlyByteBuf buffer) {
        return new VRSwitchingPayloadS2C(buffer.readBoolean());
    }
}
