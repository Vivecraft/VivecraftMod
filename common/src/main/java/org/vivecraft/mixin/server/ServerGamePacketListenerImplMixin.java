package org.vivecraft.mixin.server;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.server.AimFixHandler;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.config.ServerConfig;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ServerGamePacketListener {

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

    @Inject(at = @At("TAIL"), method = "<init>")
    public void vivecraft$init(MinecraftServer p_9770_, Connection p_9771_, ServerPlayer p_9772_, CallbackInfo info) {
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

    /**
     * handle server bound vivecraft packets
     */
    @Inject(at = @At("HEAD"), method = "handleCustomPayload", cancellable = true)
    public void vivecraft$handleVivecraftPackets(ServerboundCustomPayloadPacket payloadPacket, CallbackInfo ci) {
        if (CommonNetworkHelper.CHANNEL.equals(payloadPacket.getIdentifier())) {
            PacketUtils.ensureRunningOnSameThread(payloadPacket, this, this.player.serverLevel());

            CommonNetworkHelper.PacketDiscriminators packetId = CommonNetworkHelper.PacketDiscriminators.values()[payloadPacket.getData().readByte()];

            ServerNetworking.handlePacket(packetId, payloadPacket.getData(), this.player, ((ServerGamePacketListenerImpl) (Object) this)::send);

            if (packetId == CommonNetworkHelper.PacketDiscriminators.CLIMBING) {
                this.aboveGroundTickCount = 0;
            }
            ci.cancel();
        }
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
