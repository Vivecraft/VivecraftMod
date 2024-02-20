package org.vivecraft.neoforge.event;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packets.VivecraftDataPacket;
import org.vivecraft.neoforge.Vivecraft;
import org.vivecraft.server.ServerNetworking;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Vivecraft.MODID)
public class CommonModEvents {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar("vivecraft")
            .optional();
        registrar.play(CommonNetworkHelper.CHANNEL,
            VivecraftDataPacket::new,
            (packet, contex) -> {
                if (contex.flow().isClientbound()) {
                    handleClientVivePacket(packet, contex);
                } else {
                    handleServerVivePacket(packet, contex);
                }
            });
    }

    public static void handleClientVivePacket(VivecraftDataPacket packet, IPayloadContext context) {
        context.workHandler().execute(() -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(packet.buffer());
            ClientNetworking.handlePacket(packet.packetid(), buffer);
            buffer.release();
        });
    }

    public static void handleServerVivePacket(VivecraftDataPacket packet, IPayloadContext context) {
        context.workHandler().execute(() -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(packet.buffer());
            ServerNetworking.handlePacket(packet.packetid(), buffer, (ServerPlayer) context.player().get(), p -> context.replyHandler().send(p.payload()));
            buffer.release();
        });
    }
}
