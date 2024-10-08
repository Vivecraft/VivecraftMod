package org.vivecraft.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.common.network.CommonNetworkHelper;

public interface VivecraftPayload extends CustomPacketPayload {

    /**
     * writes this data packet to the given buffer
     * @param buffer Buffer to write to
     */
    default void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
    }

    /**
     * returns the PacketIdentifier associated with this packet
     */
    PayloadIdentifier payloadId();


    /**
     * @return ResourceLocation identifying this packet
     */
    default ResourceLocation id() {
        return CommonNetworkHelper.CHANNEL;
    }
}
