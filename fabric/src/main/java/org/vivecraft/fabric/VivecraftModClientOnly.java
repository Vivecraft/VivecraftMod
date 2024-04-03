package org.vivecraft.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.packets.VivecraftDataPacket;

public class VivecraftModClientOnly implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // init client packets
        PayloadTypeRegistry.playS2C().register(VivecraftDataPacket.TYPE, VivecraftDataPacket.STREAM_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(VivecraftDataPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(payload.buffer());
                ClientNetworking.handlePacket(payload.packetid(), buffer);
                buffer.release();
            });
        });

    }
}
