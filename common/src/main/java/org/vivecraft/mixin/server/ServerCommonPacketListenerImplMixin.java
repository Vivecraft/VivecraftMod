package org.vivecraft.mixin.server;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packets.VivecraftDataPacket;
import org.vivecraft.server.ServerNetworking;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {

    @Shadow
    @Final
    protected MinecraftServer server;


    /**
     * handle server bound vivecraft packets
     * on neoforge those are handled in {@link org.vivecraft.neoforge.event.ServerEvents#handleVivePacket}
     * if connected to spigot they are still handled here
     */
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void vivecraft$handleVivecraftPackets(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof VivecraftDataPacket dataPacket &&
            (Object) this instanceof ServerGamePacketListenerImpl gamePacketListener)
        {
            PacketUtils.ensureRunningOnSameThread(packet, (ServerCommonPacketListenerImpl) (Object) this, this.server);
            var buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(dataPacket.buffer());
            ServerNetworking.handlePacket(dataPacket.packetId(), buffer, gamePacketListener.player, gamePacketListener::send);
            buffer.release();

            if (dataPacket.packetId() == CommonNetworkHelper.PacketDiscriminators.CLIMBING) {
                gamePacketListener.aboveGroundTickCount = 0;
            }
            ci.cancel();
        }
    }
}
