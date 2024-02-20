package org.vivecraft.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.common.network.CommonNetworkHelper;

public record VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators packetid,
                                  byte[] buffer) implements CustomPacketPayload {

    public VivecraftDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this(CommonNetworkHelper.PacketDiscriminators.values()[friendlyByteBuf.readByte()], readBuffer(friendlyByteBuf));
    }

    private static byte[] readBuffer(FriendlyByteBuf friendlyByteBuf) {
        byte[] buffer = new byte[friendlyByteBuf.readableBytes()];
        friendlyByteBuf.readBytes(buffer);
        return buffer;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(packetid.ordinal());
        friendlyByteBuf.writeBytes(buffer);
    }

    @Override
    public ResourceLocation id() {
        return CommonNetworkHelper.CHANNEL;
    }
}
