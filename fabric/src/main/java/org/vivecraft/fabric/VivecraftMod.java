package org.vivecraft.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import org.vivecraft.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.fabric.client.ClientEvents;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerUtil;
import org.vivecraft.server.config.ServerConfig;

public class VivecraftMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // init server config
        ServerConfig.init(null);

        // add server config commands
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> ServerUtil.registerCommands(dispatcher, registryAccess));

        // register packets
        // use channel registers to be compatible with other mod loaders
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(CommonNetworkHelper.CHANNEL,
                (client, handler, buffer, responseSender) -> {
                    VivecraftPayloadS2C packet = VivecraftPayloadS2C.readPacket(buffer);
                    client.execute(() -> ClientNetworking.handlePacket(packet));
                });
            ClientEvents.registerClientCommands();
        }

        ServerPlayNetworking.registerGlobalReceiver(CommonNetworkHelper.CHANNEL,
            (server, player, handler, buffer, responseSender) -> {
                VivecraftPayloadC2S packet = VivecraftPayloadC2S.readPacket(buffer);
                server.execute(() -> ServerNetworking.handlePacket(packet, player,
                    p -> responseSender.sendPacket(Xplat.getS2CPacket(p))));
            });
    }
}
