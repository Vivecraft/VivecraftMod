package org.vivecraft.api;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
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
import org.vivecraft.DataHolder;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.render.PlayerModelController;
import org.vivecraft.render.RenderPass;
import org.vivecraft.settings.AutoCalibration;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.lwjgl.Matrix4f;
import org.vivecraft.utils.math.Quaternion;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        //DataHolder.getInstance().vrSettings.overrides.resetAll(); move to mixin
    }

    public static void sendVersionInfo()
    {
        byte[] abyte = DataHolder.getInstance().minecriftVerString.getBytes(Charsets.UTF_8);
        String s = channel.toString();
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeBytes(s.getBytes());
        Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new ResourceLocation("minecraft:register"), friendlybytebuf));
        Minecraft.getInstance().getConnection().send(getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.VERSION, abyte));
        DataHolder.getInstance().vrPlayer.teleportWarningTimer = 200;
    }

    public static void sendVRPlayerPositions(VRPlayer player)
    {
        if (serverWantsData)
        {
            if (Minecraft.getInstance().getConnection() != null)
            {
                float f = DataHolder.getInstance().vrPlayer.vrdata_world_post.worldScale;

                if (f != worldScallast)
                {
                    ByteBuf bytebuf = Unpooled.buffer();
                    bytebuf.writeFloat(f);
                    byte[] abyte = new byte[bytebuf.readableBytes()];
                    bytebuf.readBytes(abyte);
                    ServerboundCustomPayloadPacket serverboundcustompayloadpacket = getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.WORLDSCALE, abyte);
                    Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket);
                    worldScallast = f;
                }

                float f1 = AutoCalibration.getPlayerHeight();

                if (f1 != heightlast)
                {
                    ByteBuf bytebuf2 = Unpooled.buffer();
                    bytebuf2.writeFloat(f1 / 1.52F);
                    byte[] abyte3 = new byte[bytebuf2.readableBytes()];
                    bytebuf2.readBytes(abyte3);
                    ServerboundCustomPayloadPacket serverboundcustompayloadpacket1 = getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.HEIGHT, abyte3);
                    Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket1);
                    heightlast = f1;
                }

                byte[] abyte2 = null;
                byte[] abyte4 = null;
                byte[] abyte5 = null;
                FloatBuffer floatbuffer = player.vrdata_world_post.hmd.getMatrix().toFloatBuffer();
                ((Buffer)floatbuffer).rewind();
                Matrix4f matrix4f = new Matrix4f();
                matrix4f.load(floatbuffer);
                Vec3 vec3 = player.vrdata_world_post.getEye(RenderPass.CENTER).getPosition().subtract(Minecraft.getInstance().player.position());
                Quaternion quaternion = new Quaternion(matrix4f);
                ByteBuf bytebuf1 = Unpooled.buffer();
                bytebuf1.writeBoolean(DataHolder.getInstance().vrSettings.seated);
                bytebuf1.writeFloat((float)vec3.x);
                bytebuf1.writeFloat((float)vec3.y);
                bytebuf1.writeFloat((float)vec3.z);
                bytebuf1.writeFloat(quaternion.w);
                bytebuf1.writeFloat(quaternion.x);
                bytebuf1.writeFloat(quaternion.y);
                bytebuf1.writeFloat(quaternion.z);
                byte[] abyte1 = new byte[bytebuf1.readableBytes()];
                bytebuf1.readBytes(abyte1);
                ServerboundCustomPayloadPacket serverboundcustompayloadpacket2 = getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.HEADDATA, abyte1);
                Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket2);

                for (int i = 0; i < 2; ++i)
                {
                    Vec3 vec31 = player.vrdata_world_post.getController(i).getPosition().subtract(Minecraft.getInstance().player.position());
                    FloatBuffer floatbuffer1 = player.vrdata_world_post.getController(i).getMatrix().toFloatBuffer();
                    ((Buffer)floatbuffer1).rewind();
                    Matrix4f matrix4f1 = new Matrix4f();
                    matrix4f1.load(floatbuffer1);
                    Quaternion quaternion1 = new Quaternion(matrix4f1);
                    ByteBuf bytebuf3 = Unpooled.buffer();
                    bytebuf3.writeBoolean(DataHolder.getInstance().vrSettings.reverseHands);
                    bytebuf3.writeFloat((float)vec31.x);
                    bytebuf3.writeFloat((float)vec31.y);
                    bytebuf3.writeFloat((float)vec31.z);
                    bytebuf3.writeFloat(quaternion1.w);
                    bytebuf3.writeFloat(quaternion1.x);
                    bytebuf3.writeFloat(quaternion1.y);
                    bytebuf3.writeFloat(quaternion1.z);
                    byte[] abyte6 = new byte[bytebuf3.readableBytes()];

                    if (i == 0)
                    {
                        abyte4 = abyte6;
                    }
                    else
                    {
                        abyte5 = abyte6;
                    }

                    bytebuf3.readBytes(abyte6);
                    ServerboundCustomPayloadPacket serverboundcustompayloadpacket3 = getVivecraftClientPacket(i == 0 ? NetworkHelper.PacketDiscriminators.CONTROLLER0DATA : NetworkHelper.PacketDiscriminators.CONTROLLER1DATA, abyte6);
                    Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket3);
                }

                PlayerModelController.getInstance().Update(Minecraft.getInstance().player.getGameProfile().getId(), abyte1, abyte4, abyte5, f, f1 / 1.52F, true);
            }
        }
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

    public static boolean isLimitedSurvivalTeleport()
    {
        return DataHolder.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.LIMIT_TELEPORT).getBoolean();
    }

    public static int getTeleportUpLimit()
    {
        return DataHolder.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_UP_LIMIT).getInt();
    }

    public static int getTeleportDownLimit()
    {
        return DataHolder.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_DOWN_LIMIT).getInt();
    }

    public static int getTeleportHorizLimit()
    {
        return DataHolder.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_HORIZ_LIMIT).getInt();
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
        else if (player instanceof LocalPlayer && DataHolder.getInstance().crawlTracker.crawling)
        {
            player.setPose(Pose.SWIMMING);
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
