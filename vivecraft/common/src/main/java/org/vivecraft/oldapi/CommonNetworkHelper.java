package org.vivecraft.oldapi;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommonNetworkHelper {

    public static Map<UUID, ServerVivePlayer> vivePlayers = new HashMap<>();
    public static final ResourceLocation channel = new ResourceLocation("vivecraft:data");

    public static boolean isVive(ServerPlayer p)
    {
        if (p == null)
        {
            return false;
        }
        else
        {
            return vivePlayers.containsKey(p.getGameProfile().getId()) ? vivePlayers.get(p.getGameProfile().getId()).isVR() : false;
        }
    }

    public static void sendPosData(ServerPlayer from)
    {
        ServerVivePlayer serverviveplayer = vivePlayers.get(from.getUUID());

        if (serverviveplayer != null)
        {
            if (serverviveplayer.player != null && !serverviveplayer.player.hasDisconnected())
            {
                if (serverviveplayer.isVR())
                {
                    for (ServerVivePlayer serverviveplayer1 : vivePlayers.values())
                    {
                        if (serverviveplayer1 != null && serverviveplayer1.player != null && !serverviveplayer1.player.hasDisconnected() && serverviveplayer != serverviveplayer1 && serverviveplayer.player.getCommandSenderWorld() == serverviveplayer1.player.getCommandSenderWorld() && serverviveplayer.hmdData != null && serverviveplayer.controller0data != null && serverviveplayer.controller1data != null)
                        {
                            double d0 = serverviveplayer1.player.position().distanceToSqr(serverviveplayer.player.position());

                            if (d0 < 65536.0D)
                            {
                                ClientboundCustomPayloadPacket clientboundcustompayloadpacket = getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.UBERPACKET, serverviveplayer.getUberPacket());
                                serverviveplayer1.player.connection.send(clientboundcustompayloadpacket);
                            }
                        }
                    }
                }
            }
            else
            {
                vivePlayers.remove(from.getUUID());
            }
        }
    }

    public static void overridePose(ServerPlayer player) {
        ServerVivePlayer serverviveplayer = vivePlayers.get(player.getGameProfile().getId());

        if (serverviveplayer != null && serverviveplayer.isVR() && serverviveplayer.crawling) {
                player.setPose(Pose.SWIMMING);
        }
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, byte[] payload)
    {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ClientboundCustomPayloadPacket(channel, friendlybytebuf);
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, String payload)
    {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeUtf(payload);
        return new ClientboundCustomPayloadPacket(channel, friendlybytebuf);
    }

    public static enum PacketDiscriminators
    {
        VERSION,
        REQUESTDATA,
        HEADDATA,
        CONTROLLER0DATA,
        CONTROLLER1DATA,
        WORLDSCALE,
        DRAW,
        MOVEMODE,
        UBERPACKET,
        TELEPORT,
        CLIMBING,
        SETTING_OVERRIDE,
        HEIGHT,
        ACTIVEHAND,
        CRAWL;
    }
}
