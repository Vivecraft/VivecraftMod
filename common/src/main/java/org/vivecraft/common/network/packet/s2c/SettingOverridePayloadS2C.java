package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * holds a map of settings the server has overridden
 *
 * @param overrides map with the key as the setting, and the value as the override
 */
public record SettingOverridePayloadS2C(Map<String, String> overrides) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.SETTING_OVERRIDE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        for (Map.Entry<String, String> entry : this.overrides.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeUtf(entry.getValue());
        }
    }

    public static SettingOverridePayloadS2C read(FriendlyByteBuf buffer) {
        Map<String, String> overrides = new HashMap<>();

        while (buffer.readableBytes() > 0) {
            overrides.put(buffer.readUtf(), buffer.readUtf());
        }

        return new SettingOverridePayloadS2C(overrides);
    }
}
