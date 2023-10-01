package org.vivecraft.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.common.network.CommonNetworkHelper;

public record VivecraftDataPacket(FriendlyByteBuf buffer) implements CustomPacketPayload {
    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBytes(buffer);
    }

    @Override
    public ResourceLocation id() {
        return CommonNetworkHelper.CHANNEL;
    }
}
