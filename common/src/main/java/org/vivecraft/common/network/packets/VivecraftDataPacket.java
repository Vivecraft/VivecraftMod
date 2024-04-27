package org.vivecraft.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.vivecraft.common.network.CommonNetworkHelper;

public record VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators packetid,
                                  byte[] buffer) implements CustomPacketPayload {

    public static final StreamCodec<FriendlyByteBuf, VivecraftDataPacket> STREAM_CODEC = CustomPacketPayload.codec(VivecraftDataPacket::write, VivecraftDataPacket::new);
    public static final Type<VivecraftDataPacket> TYPE = new Type<>(CommonNetworkHelper.CHANNEL);

    public VivecraftDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this(CommonNetworkHelper.PacketDiscriminators.values()[friendlyByteBuf.readByte()], readBuffer(friendlyByteBuf));
    }

    private static byte[] readBuffer(FriendlyByteBuf friendlyByteBuf) {
        byte[] buffer = new byte[friendlyByteBuf.readableBytes()];
        friendlyByteBuf.readBytes(buffer);
        return buffer;
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(packetid.ordinal());
        friendlyByteBuf.writeBytes(buffer);
    }

    @Override
    public Type<VivecraftDataPacket> type() {
        return TYPE;
    }
}
