package org.vivecraft.neoforge.event;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.packets.VivecraftDataPacket;
import org.vivecraft.neoforge.Vivecraft;
import org.vivecraft.server.ServerNetworking;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Vivecraft.MODID)
public class CommonModEvents {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("vivecraft")
            .optional();
        registrar.playBidirectional(VivecraftDataPacket.TYPE, VivecraftDataPacket.STREAM_CODEC,
            (packet, context) -> {
                if (context.flow().isClientbound()) {
                    handleClientVivePacket(packet, context);
                } else {
                    handleServerVivePacket(packet, context);
                }
            });
    }

    public static void handleClientVivePacket(VivecraftDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(packet.buffer());
            ClientNetworking.handlePacket(packet.packetid(), buffer);
            buffer.release();
        });
    }

    public static void handleServerVivePacket(VivecraftDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(packet.buffer());
            ServerNetworking.handlePacket(packet.packetid(), buffer, (ServerPlayer) context.player(), p -> context.reply(p.payload()));
            buffer.release();
        });
    }
}
