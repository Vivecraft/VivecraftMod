package org.vivecraft.mixin.server;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.server.AimFixHandler;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.config.ServerConfig;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {

    @Shadow
    public ServerPlayer player;

    public ServerGamePacketListenerImplMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void vivecraft$init(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        // Vivecraft
        if (this.connection.channel != null && this.connection.channel.pipeline().get("packet_handler") != null) { //fake player fix
            this.connection.channel.pipeline().addBefore("packet_handler", "vr_aim_fix",
                new AimFixHandler(this.connection));
        }
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    public void vivecraft$afterTick(CallbackInfo info) {
        ServerNetworking.sendVrPlayerStateToClients(this.player);
    }

    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void vivecraft$doLeaveMessage(Component component, CallbackInfo ci) {
        if (ServerConfig.messagesEnabled.get()) {
            String message = ServerConfig.messagesLeaveMessage.get();
            if (!message.isEmpty()) {
                this.server.getPlayerList().broadcastSystemMessage(Component.literal(message.formatted(this.player.getName().getString())), false);
            }
        }
        // remove player from vivepalyer list, when they leave
        ServerVRPlayers.getPlayersWithVivecraft(this.server).remove(this.player.getUUID());
    }
}
