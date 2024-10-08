package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * packet that holds the Vivecraft server version
 * @param version Version String of the server
 */
public record VersionPayloadS2C(String version) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.VERSION;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeUtf(this.version);
    }

    public static VersionPayloadS2C read(FriendlyByteBuf buffer) {
        return new VersionPayloadS2C(buffer.readUtf());
    }
}
