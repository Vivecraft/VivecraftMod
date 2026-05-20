package org.vivecraft.common.network.packet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class WrappedPacket implements Packet<ServerGamePacketListenerImpl> {
    private static final PacketType<WrappedPacket> TYPE = new PacketType<>(
        PacketFlow.SERVERBOUND, Identifier.fromNamespaceAndPath("vivecraft", "wrapped"));

    private final Runnable runnable;

    public WrappedPacket(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public PacketType<? extends Packet<ServerGamePacketListenerImpl>> type() {
        return TYPE;
    }

    @Override
    public void handle(ServerGamePacketListenerImpl handler) throws RunningOnDifferentThreadException {
        PacketUtils.ensureRunningOnSameThread(this, handler, handler.player.level());
        this.runnable.run();
    }
}
