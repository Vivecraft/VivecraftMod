package org.vivecraft.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.vivecraft.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.config.ServerConfig;

@Mod(Vivecraft.MODID)
public class Vivecraft {
    public static final String MODID = "vivecraft";

    public static final EventNetworkChannel VIVECRAFT_NETWORK_CHANNEL =
        NetworkRegistry.ChannelBuilder.named(CommonNetworkHelper.CHANNEL)
            .clientAcceptedVersions(status -> true)
            .serverAcceptedVersions(status -> true)
            .networkProtocolVersion(() -> "0")
            .eventNetworkChannel();

    public Vivecraft() {
        // init server config
        // this is too early for the lang files to be loaded, is needed to register the commands though
        // server config is validated again later to have the comments
        ServerConfig.init(null);

        VIVECRAFT_NETWORK_CHANNEL.addListener(event -> {
            if (event.getPayload() != null) {
                if (event.getSource().get().getDirection().getOriginationSide().isClient()) {
                    handleServerVivePacket(event.getPayload(), event.getSource().get());
                } else {
                    handleClientVivePacket(event.getPayload(), event.getSource().get());
                }
            }
            event.getSource().get().setPacketHandled(true);
        });
    }

    private static void handleClientVivePacket(FriendlyByteBuf buffer, NetworkEvent.Context context) {
        VivecraftPayloadS2C packet = VivecraftPayloadS2C.readPacket(buffer);
        context.enqueueWork(() -> ClientNetworking.handlePacket(packet));
    }

    private static void handleServerVivePacket(FriendlyByteBuf buffer, NetworkEvent.Context context) {
        VivecraftPayloadC2S packet = VivecraftPayloadC2S.readPacket(buffer);
        context.enqueueWork(
            () -> ServerNetworking.handlePacket(packet, context.getSender(),
                p -> context.getNetworkManager().send(Xplat.getS2CPacket(p))));
    }
}
