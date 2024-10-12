package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds weather the client is in VR or NONVR mode
 * @param vr if the client is actively in VR
 */
public record VRActivePayloadC2S(boolean vr) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.IS_VR_ACTIVE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.vr);
    }

    public static VRActivePayloadC2S read(FriendlyByteBuf buffer) {
        return new VRActivePayloadC2S(buffer.readBoolean());
    }
}
