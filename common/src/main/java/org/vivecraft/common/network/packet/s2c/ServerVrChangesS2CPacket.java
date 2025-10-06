package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * holds all server vr changes that are at non-default values
 *
 * @param changes map of setting/value pairs of non-default settings
 */
public record ServerVrChangesS2CPacket(Map<String, String> changes) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.SERVER_VR_CHANGES;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        for (Map.Entry<String, String> entry : this.changes.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
    }

    public static ServerVrChangesS2CPacket read(FriendlyByteBuf buffer) {
        Map<String, String> overrides = new HashMap<>();

        while (buffer.readableBytes() > 0) {
            overrides.put(buffer.readUtf(), buffer.readUtf());
        }

        return new ServerVrChangesS2CPacket(overrides);
    }
}
