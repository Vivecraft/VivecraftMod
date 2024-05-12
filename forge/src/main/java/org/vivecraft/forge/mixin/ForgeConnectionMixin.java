package org.vivecraft.forge.mixin;

import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.common.network.packets.VivecraftDataPacket;
import org.vivecraft.forge.Vivecraft;

@Mixin(Connection.class)
public class ForgeConnectionMixin {
    @ModifyVariable(at = @At("HEAD"), method = "send(Lnet/minecraft/network/protocol/Packet;)V", argsOnly = true)
    private Packet<?> vivecraft$convertPacket(Packet<?> packet) {
        // stupid forge doesn't register packets, so these wouldn't actually be sent correctly on the client
        // need to convert them to forge packets
        if (packet instanceof ClientboundCustomPayloadPacket clientPacket && clientPacket.payload() instanceof VivecraftDataPacket vivecraftDataPacket) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            vivecraftDataPacket.write(buffer);
            packet = NetworkDirection.PLAY_TO_CLIENT.buildPacket(Vivecraft.VIVECRAFT_NETWORK_CHANNEL, buffer).getThis();
        } else if (packet instanceof ServerboundCustomPayloadPacket serverPacket && serverPacket.payload() instanceof VivecraftDataPacket vivecraftDataPacket) {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            vivecraftDataPacket.write(buffer);
            packet = NetworkDirection.PLAY_TO_SERVER.buildPacket(Vivecraft.VIVECRAFT_NETWORK_CHANNEL, buffer).getThis();
        }
        return packet;
    }
}
