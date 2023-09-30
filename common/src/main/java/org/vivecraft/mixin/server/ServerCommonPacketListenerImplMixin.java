package org.vivecraft.mixin.server;

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

    @Inject(at = @At("TAIL"), method = "handleCustomPayload")
    public void vivecraft$handleVivecraftPackets(ServerboundCustomPayloadPacket payloadPacket, CallbackInfo ci) {
        if (payloadPacket.payload() instanceof VivecraftDataPacket dataPacket
            && (Object) this instanceof ServerGamePacketListenerImpl gamePacketListener) {
            var buffer = dataPacket.buffer();
            PacketUtils.ensureRunningOnSameThread(payloadPacket, (ServerCommonPacketListenerImpl) (Object) this, server);
            CommonNetworkHelper.PacketDiscriminators packetDiscriminator = CommonNetworkHelper.PacketDiscriminators.values()[buffer.readByte()];
            ServerNetworking.handlePacket(packetDiscriminator, buffer, gamePacketListener);

            if (packetDiscriminator == CommonNetworkHelper.PacketDiscriminators.CLIMBING) {
                gamePacketListener.aboveGroundTickCount = 0;
            }
        }
    }
}
