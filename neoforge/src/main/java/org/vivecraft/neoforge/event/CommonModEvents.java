package org.vivecraft.neoforge.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.neoforge.Vivecraft;
import org.vivecraft.neoforge.packet.VivecraftPayloadBiDir;
import org.vivecraft.server.ServerNetworking;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Vivecraft.MODID)
public class CommonModEvents {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar("vivecraft")
            .optional();
        registrar.play(CommonNetworkHelper.CHANNEL,
            VivecraftPayloadBiDir::new,
            (packet, context) -> {
                if (context.flow().isClientbound()) {
                    handleClientVivePacket(packet.getS2CPayload(), context);
                } else {
                    handleServerVivePacket(packet.getC2SPayload(), context);
                }
            });
    }

    public static void handleClientVivePacket(VivecraftPayloadS2C packet, IPayloadContext context) {
        context.workHandler().execute(() -> ClientNetworking.handlePacket(packet));
    }

    public static void handleServerVivePacket(VivecraftPayloadC2S packet, IPayloadContext context) {
        context.workHandler().execute(() -> ServerNetworking.handlePacket(packet, (ServerPlayer) context.player().get(),
            p -> context.replyHandler().send(new VivecraftPayloadBiDir(p))));
    }
}
