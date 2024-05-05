package org.vivecraft.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.client.network.ClientNetworking;
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

        // register packets
        PayloadTypeRegistry.playS2C().register(VivecraftDataPacket.TYPE, VivecraftDataPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(VivecraftDataPacket.TYPE, VivecraftDataPacket.STREAM_CODEC);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            // register client packet receiver
            ClientPlayNetworking.registerGlobalReceiver(VivecraftDataPacket.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(payload.buffer());
                    ClientNetworking.handlePacket(payload.packetid(), buffer);
                    buffer.release();
                }));
        }

        // register server packet receiver
        ServerPlayNetworking.registerGlobalReceiver(VivecraftDataPacket.TYPE, (payload, context) -> {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(payload.buffer());
            ServerNetworking.handlePacket(payload.packetid(), buffer, context.player(), p -> context.responseSender().sendPacket(p));
            buffer.release();
        });
    }
}
