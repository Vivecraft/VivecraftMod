package org.vivecraft.neoforge.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.neoforge.Vivecraft;
import org.vivecraft.neoforge.packet.VivecraftPayloadBiDir;
import org.vivecraft.server.ServerNetworking;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Vivecraft.MODID)
public class CommonModEvents {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("vivecraft").optional();

        registrar.playBidirectional(VivecraftPayloadBiDir.TYPE,
            VivecraftPayloadBiDir.CODEC,
            (packet, context) -> {
                if (context.flow().isClientbound()) {
                    handleClientVivePacket(packet.getS2CPayload(), context);
                } else {
                    handleServerVivePacket(packet.getC2SPayload(), context);
                }
            });
    }

    public static void handleClientVivePacket(VivecraftPayloadS2C packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworking.handlePacket(packet));
    }

    public static void handleServerVivePacket(VivecraftPayloadC2S packet, IPayloadContext context) {
        context.enqueueWork(
            () -> ServerNetworking.handlePacket(packet, (ServerPlayer) context.player(),
                p -> context.reply(new VivecraftPayloadBiDir(p))));
    }
}
