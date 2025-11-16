package org.vivecraft.neoforge.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.neoforge.Vivecraft;
import org.vivecraft.neoforge.packet.VivecraftPayloadBiDir;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerUtil;

@EventBusSubscriber(modid = Vivecraft.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("vivecraft").optional();

        registrar.playBidirectional(VivecraftPayloadBiDir.TYPE,
            VivecraftPayloadBiDir.CODEC,
            (packet, context) -> handleServerVivePacket(packet.getC2SPayload(), context),
            (packet, context) -> handleClientVivePacket(packet.getS2CPayload(), context));
    }

    public static void handleClientVivePacket(VivecraftPayloadS2C packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientNetworking.handlePacket(packet));
    }

    public static void handleServerVivePacket(VivecraftPayloadC2S packet, IPayloadContext context) {
        context.enqueueWork(
            () -> ServerNetworking.handlePacket(packet, (ServerPlayer) context.player(),
                p -> context.reply(new VivecraftPayloadBiDir(p))));
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ServerUtil.registerCommands(event.getDispatcher(), event.getBuildContext());
    }
}
