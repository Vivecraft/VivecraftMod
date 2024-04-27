package org.vivecraft.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packets.VivecraftDataPacket;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerUtil;
import org.vivecraft.server.config.ServerConfig;

public class VivecraftMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // init server config
        ServerConfig.init(null);

        // add server config commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            ServerUtil.registerCommands(dispatcher));

        PayloadTypeRegistry.playC2S().register(VivecraftDataPacket.TYPE, VivecraftDataPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(VivecraftDataPacket.TYPE, (payload, context) -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(payload.buffer());
            ServerNetworking.handlePacket(payload.packetid(), buffer, context.player(), p -> context.responseSender().sendPacket(p.payload()));
            buffer.release();
        });
    }
}
