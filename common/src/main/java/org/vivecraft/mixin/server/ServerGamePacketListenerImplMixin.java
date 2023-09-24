package org.vivecraft.mixin.server;

import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators;
import org.vivecraft.server.AimFixHandler;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.config.ServerConfig;

import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;

import static org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators.CLIMBING;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ServerPlayerConnection, ServerGamePacketListener {

    @Shadow
    @Final
    private Connection connection;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    public ServerPlayer player;

    @Shadow
    private int aboveGroundTickCount;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V")
    public void init(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer, CallbackInfo info) {
        // Vivecraft
        if (this.connection.channel != null && this.connection.channel.pipeline().get("packet_handler") != null) { //fake player fix
            this.connection.channel.pipeline().addBefore("packet_handler", "vr_aim_fix",
                    new AimFixHandler(this.connection));
        }
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    public void afterTick(CallbackInfo info) {
        ServerNetworking.sendVrPlayerStateToClients(this.player);
    }

    @Inject(at = @At("TAIL"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/game/ServerboundCustomPayloadPacket;)V")
    public void handleVivecraftPackets(ServerboundCustomPayloadPacket pPacket, CallbackInfo info) {
        FriendlyByteBuf buffer = pPacket.getData();
        ResourceLocation channelID = pPacket.getIdentifier();

        if (channelID.equals(CommonNetworkHelper.CHANNEL)) {
            PacketUtils.ensureRunningOnSameThread(pPacket, this, this.player.serverLevel());
            PacketDiscriminators packetDiscriminator = PacketDiscriminators.values()[buffer.readByte()];
            ServerNetworking.handlePacket(packetDiscriminator, buffer, (ServerGamePacketListenerImpl) (Object)this);
            if (packetDiscriminator == CLIMBING) {
                this.aboveGroundTickCount = 0;
            }
        }
    }
    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void doLeaveMessage(Component component, CallbackInfo ci) {
        if (ServerConfig.messagesEnabled.get()) {
            String message = ServerConfig.messagesLeaveMessage.get();
            if (!message.isEmpty()) {
                this.server.getPlayerList().broadcastSystemMessage(Component.literal(message.formatted(this.player.getName().getString())), false);
            }
        }
        // remove player from viveplayer list, when they leave
        ServerVRPlayers.getPlayersWithVivecraft(this.server).remove(this.player.getUUID());
    }
}
