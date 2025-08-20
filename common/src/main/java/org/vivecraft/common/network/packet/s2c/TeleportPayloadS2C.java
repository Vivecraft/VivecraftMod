package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * indicates that the server supports direct teleports
 *
 * @param allowed              indicates if teleports are allowed
 * @param targetNetworkVersion network version of the target player, to not send additional data, if they don't support it
 */
public record TeleportPayloadS2C(boolean allowed, int targetNetworkVersion) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.TELEPORT;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        // old clients don't expect additional data
        if (this.targetNetworkVersion >= CommonNetworkHelper.NETWORK_VERSION_OPTION_TOGGLE) {
            buffer.writeBoolean(this.allowed);
        }
    }

    public static TeleportPayloadS2C read(FriendlyByteBuf buffer) {
        if (buffer.readableBytes() > 0) {
            return new TeleportPayloadS2C(buffer.readBoolean(), CommonNetworkHelper.NETWORK_VERSION_OPTION_TOGGLE);
        } else {
            // old servers always allowed it when they sent this packet
            return new TeleportPayloadS2C(true, CommonNetworkHelper.NETWORK_VERSION_LEGACY);
        }
    }
}
