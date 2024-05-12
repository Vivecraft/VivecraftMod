package org.vivecraft.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.config.ServerConfig;

@Mod(Vivecraft.MODID)
public class Vivecraft {
    public static final String MODID = "vivecraft";

    public static final EventNetworkChannel VIVECRAFT_NETWORK_CHANNEL =
        ChannelBuilder.named(CommonNetworkHelper.CHANNEL)
            .acceptedVersions((status, version) -> true)
            .optional()
            .networkProtocolVersion(0)
            .eventNetworkChannel();

    public Vivecraft() {
        // init server config
        ServerConfig.init(null);

        VIVECRAFT_NETWORK_CHANNEL.addListener(event  -> {
            if (event.getSource().isServerSide()) {
                handleServerVivePacket(event.getPayload(), event.getSource());
            } else {
                handleClientVivePacket(event.getPayload(), event.getSource());
            }
        });
    }

    private static void handleClientVivePacket(FriendlyByteBuf buffer, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ClientNetworking.handlePacket(CommonNetworkHelper.PacketDiscriminators.values()[buffer.readByte()], buffer);
        });
    }

    private static void handleServerVivePacket(FriendlyByteBuf buffer, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerNetworking.handlePacket(CommonNetworkHelper.PacketDiscriminators.values()[buffer.readByte()], buffer, context.getSender(), p -> context.getConnection().send(p));
            buffer.release();
        });
    }
}
