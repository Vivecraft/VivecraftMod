package org.vivecraft.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.examplemod.DataHolder;
import com.google.common.base.Charsets;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class NetworkHelper
{
    public static Map<UUID, ServerVivePlayer> vivePlayers = new HashMap<>();
    public static final ResourceLocation channel = new ResourceLocation("vivecraft:data");
    public static boolean displayedChatMessage = false;
    public static boolean serverWantsData = false;
    public static boolean serverAllowsClimbey = false;
    public static boolean serverSupportsDirectTeleport = false;
    public static boolean serverAllowsCrawling = false;
    private static float worldScallast = 0.0F;
    private static float heightlast = 0.0F;
    private static float capturedYaw;
    private static float capturedPitch;
    private static boolean overrideActive;

    public static ServerboundCustomPayloadPacket getVivecraftClientPacket(NetworkHelper.PacketDiscriminators command, byte[] payload)
    {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ServerboundCustomPayloadPacket(channel, friendlybytebuf);
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(NetworkHelper.PacketDiscriminators command, byte[] payload)
    {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ClientboundCustomPayloadPacket(channel, friendlybytebuf);
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(NetworkHelper.PacketDiscriminators command, String payload)
    {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeUtf(payload);
        return new ClientboundCustomPayloadPacket(channel, friendlybytebuf);
    }

    public static void resetServerSettings()
    {
        worldScallast = 0.0F;
        heightlast = 0.0F;
        serverAllowsClimbey = false;
        serverWantsData = false;
        serverSupportsDirectTeleport = false;
        serverAllowsCrawling = false;
    }

    public static void sendVersionInfo()
    {
        byte[] abyte = DataHolder.getInstance().minecriftVerString.getBytes(Charsets.UTF_8);
        String s = channel.toString();
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeBytes(s.getBytes());
        Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new ResourceLocation("minecraft:register"), friendlybytebuf));
        Minecraft.getInstance().getConnection().send(getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.VERSION, abyte));
    }


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
                                ClientboundCustomPayloadPacket clientboundcustompayloadpacket = getVivecraftServerPacket(NetworkHelper.PacketDiscriminators.UBERPACKET, serverviveplayer.getUberPacket());
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


    public static void sendActiveHand(byte c)
    {
        if (serverWantsData)
        {
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket = getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.ACTIVEHAND, new byte[] {c});

            if (Minecraft.getInstance().getConnection() != null)
            {
                Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket);
            }
        }
    }

    public static void overridePose(Player player)
    {
        if (player instanceof ServerPlayer)
        {
            ServerVivePlayer serverviveplayer = vivePlayers.get(player.getGameProfile().getId());

            if (serverviveplayer != null && serverviveplayer.isVR() && serverviveplayer.crawling)
            {
                player.setPose(Pose.SWIMMING);
            }
        }
    }
    public static void overrideLook(Player player, Vec3 view)
    {
        if (!serverWantsData)
        {
            capturedPitch = player.getXRot();
            capturedYaw = player.getYRot();
            float f = (float)Math.toDegrees(Math.asin(-view.y / view.length()));
            float f1 = (float)Math.toDegrees(Math.atan2(-view.x, view.z));
            ((LocalPlayer)player).connection.send(new ServerboundMovePlayerPacket.Rot(f1, f, player.isOnGround()));
            overrideActive = true;
        }
    }

    public static void restoreLook(Player player)
    {
        if (!serverWantsData)
        {
            if (overrideActive)
            {
                ((LocalPlayer)player).connection.send(new ServerboundMovePlayerPacket.Rot(capturedYaw, capturedPitch, player.isOnGround()));
                overrideActive = false;
            }
        }
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
