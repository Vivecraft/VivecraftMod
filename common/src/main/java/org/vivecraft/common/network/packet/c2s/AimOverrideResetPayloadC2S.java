package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * resets the aim override
 *
 * @param ticks in how many ticks the override should be reset, 0 for immediately
 */
public record AimOverrideResetPayloadC2S(int ticks) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.AIM_OVERRIDE_RESET;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.ticks);
    }

    public static AimOverrideResetPayloadC2S read(FriendlyByteBuf buffer) {
        return new AimOverrideResetPayloadC2S(buffer.readByte());
    }
}
