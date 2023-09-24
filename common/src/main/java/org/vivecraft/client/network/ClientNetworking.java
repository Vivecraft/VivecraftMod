package org.vivecraft.client.network;

import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRSettings.ChatServerPluginMessage;
import org.vivecraft.client_vr.settings.VRSettings.ServerOverrides.Setting;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;
import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators;
import org.vivecraft.common.network.VRPlayerState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static org.vivecraft.client.utils.Utils.message;
import static org.vivecraft.client_vr.VRState.*;
import static org.vivecraft.common.utils.Utils.logger;

import static org.joml.Math.*;

public class ClientNetworking {

    public static boolean displayedChatMessage = false;
    public static boolean displayedChatWarning = false;
    public static boolean serverWantsData = false;
    public static boolean serverAllowsClimbey = false;
    public static boolean serverSupportsDirectTeleport = false;
    public static boolean serverAllowsCrawling = false;
    public static boolean serverAllowsVrSwitching = false;
    // assume a legacy server by default, to not send invalid packets
    // -1 == legacy server
    public static int usedNetworkVersion = -1;
    private static float worldScallast = 0.0F;
    private static float heightlast = 0.0F;
    private static float capturedYaw;
    private static float capturedPitch;
    private static boolean overrideActive;

    public static boolean needsReset = true;

    public static ServerboundCustomPayloadPacket getVivecraftClientPacket(PacketDiscriminators command, byte[] payload) {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ServerboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, friendlybytebuf);
    }

    public static ServerboundCustomPayloadPacket createVRActivePacket(boolean vrActive) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(PacketDiscriminators.IS_VR_ACTIVE.ordinal());
        buffer.writeBoolean(vrActive);
        return new ServerboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, buffer);
    }

    public static void resetServerSettings() {
        worldScallast = 0.0F;
        heightlast = 0.0F;
        serverAllowsClimbey = false;
        serverWantsData = false;
        serverSupportsDirectTeleport = false;
        serverAllowsCrawling = false;
        serverAllowsVrSwitching = false;
        usedNetworkVersion = -1;
        //dh.vrSettings.overrides.resetAll(); move to mixin
    }

    public static void sendVersionInfo() {
        String s = CommonNetworkHelper.CHANNEL.toString();
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeBytes(s.getBytes());
        mc.getConnection().send(new ServerboundCustomPayloadPacket(new ResourceLocation("minecraft:register"), friendlybytebuf));
        // send version string, with currently running
        mc.getConnection().send(getVivecraftClientPacket(PacketDiscriminators.VERSION,
            (CommonDataHolder.getInstance().versionIdentifier + (vrRunning ? " VR" : " NONVR")
                + "\n" + CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION
                + "\n" + CommonNetworkHelper.MIN_SUPPORTED_NETWORK_VERSION
        ).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    public static void sendVRPlayerPositions(VRPlayer vrPlayer) {
        var connection = mc.getConnection();
        if (!serverWantsData || connection == null) {
            return;
        }

        float worldScale = dh.vrPlayer.vrdata_world_post.worldScale;

        if (worldScale != worldScallast) {
            ByteBuf bytebuf = Unpooled.buffer();
            bytebuf.writeFloat(worldScale);
            byte[] abyte = new byte[bytebuf.readableBytes()];
            bytebuf.readBytes(abyte);
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket = getVivecraftClientPacket(PacketDiscriminators.WORLDSCALE, abyte);
            mc.getConnection().send(serverboundcustompayloadpacket);
            worldScallast = worldScale;
        }

        float f1 = AutoCalibration.getPlayerHeight();

        if (f1 != heightlast) {
            ByteBuf bytebuf2 = Unpooled.buffer();
            bytebuf2.writeFloat(f1 / 1.52F);
            byte[] abyte3 = new byte[bytebuf2.readableBytes()];
            bytebuf2.readBytes(abyte3);
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket1 = getVivecraftClientPacket(PacketDiscriminators.HEIGHT, abyte3);
            mc.getConnection().send(serverboundcustompayloadpacket1);
            heightlast = f1;
        }

        VRPlayerState vrPlayerState = VRPlayerState.create(vrPlayer);

        if (usedNetworkVersion >= 0) {
            connection.send(createVrPlayerStatePacket(vrPlayerState));
        } else {
            sendLegacyPackets(connection, vrPlayerState);
        }
        VRPlayersClient.getInstance().Update(mc.player.getGameProfile().getId(), vrPlayerState, worldScale, f1 / 1.52F, true);
    }

    public static ServerboundCustomPayloadPacket createVrPlayerStatePacket(VRPlayerState vrPlayerState) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(PacketDiscriminators.VR_PLAYER_STATE.ordinal());
        vrPlayerState.serialize(buffer);
        return new ServerboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, buffer);
    }

    public static void sendLegacyPackets(ClientPacketListener connection, VRPlayerState vrPlayerState) {
        // left controller packet
        FriendlyByteBuf controller0Buffer = new FriendlyByteBuf(Unpooled.buffer());
        controller0Buffer.writeByte(PacketDiscriminators.CONTROLLER0DATA.ordinal());
        controller0Buffer.writeBoolean(dh.vrSettings.reverseHands);
        vrPlayerState.controller0().serialize(controller0Buffer);
        connection.send(new ServerboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, controller0Buffer));

        // right controller packet
        FriendlyByteBuf controller1Buffer = new FriendlyByteBuf(Unpooled.buffer());
        controller1Buffer.writeByte(PacketDiscriminators.CONTROLLER1DATA.ordinal());
        controller1Buffer.writeBoolean(dh.vrSettings.reverseHands);
        vrPlayerState.controller1().serialize(controller1Buffer);
        connection.send(new ServerboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, controller1Buffer));

        // hmd packet
        FriendlyByteBuf headBuffer = new FriendlyByteBuf(Unpooled.buffer());
        headBuffer.writeByte(PacketDiscriminators.HEADDATA.ordinal());
        headBuffer.writeBoolean(dh.vrSettings.seated);
        vrPlayerState.hmd().serialize(headBuffer);
        connection.send(new ServerboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, headBuffer));
    }

    public static boolean isLimitedSurvivalTeleport() {
        return dh.vrSettings.overrides.getSetting(VrOptions.LIMIT_TELEPORT).getBoolean();
    }

    public static int getTeleportUpLimit() {
        return dh.vrSettings.overrides.getSetting(VrOptions.TELEPORT_UP_LIMIT).getInt();
    }

    public static int getTeleportDownLimit() {
        return dh.vrSettings.overrides.getSetting(VrOptions.TELEPORT_DOWN_LIMIT).getInt();
    }

    public static int getTeleportHorizLimit() {
        return dh.vrSettings.overrides.getSetting(VrOptions.TELEPORT_HORIZ_LIMIT).getInt();
    }

    public static void sendActiveHand(byte c) {
        if (serverWantsData) {
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket = getVivecraftClientPacket(PacketDiscriminators.ACTIVEHAND, new byte[]{c});

            if (mc.getConnection() != null) {
                mc.getConnection().send(serverboundcustompayloadpacket);
            }
        }
    }

    public static void overridePose(LocalPlayer player) {
        if (dh.crawlTracker.crawling) {
            player.setPose(Pose.SWIMMING);
        }
    }

    public static void overrideLook(Player player, Vec3 view) {
        if (!serverWantsData) {
            capturedPitch = player.getXRot();
            capturedYaw = player.getYRot();
            float f = (float) toDegrees(asin(-view.y / view.length()));
            float f1 = (float) toDegrees(atan2(-view.x, view.z));
            ((LocalPlayer) player).connection.send(new Rot(f1, f, player.onGround()));
            overrideActive = true;
        }
    }

    public static void restoreLook(Player player) {
        if (!serverWantsData) {
            if (overrideActive) {
                ((LocalPlayer) player).connection.send(new Rot(capturedYaw, capturedPitch, player.onGround()));
                overrideActive = false;
            }
        }
    }

    public static void handlePacket(PacketDiscriminators packetID, FriendlyByteBuf buffer) {
        switch (packetID) {
            case VERSION -> {
                String s11 = buffer.readUtf(1024);
                VRServerPerms.setTeleportSupported(true);
                if (vrInitialized) {
                    dh.vrPlayer.teleportWarning = false;
                    dh.vrPlayer.vrSwitchWarning = true;
                }
                if (!displayedChatMessage
                        && (dh.vrSettings.showServerPluginMessage == ChatServerPluginMessage.ALWAYS
                        || (dh.vrSettings.showServerPluginMessage == ChatServerPluginMessage.SERVER_ONLY && !mc.isLocalServer()))) {
                    displayedChatMessage = true;
                    message(Component.translatable("vivecraft.messages.serverplugin", s11));
                }
                if (vrEnabled && dh.vrSettings.manualCalibration == -1.0F && !dh.vrSettings.seated) {
                    message(Component.translatable("vivecraft.messages.calibrateheight"));
                }
            }
            case IS_VR_ACTIVE -> {
                if (!buffer.readBoolean()) {
                    VRPlayersClient.getInstance().disableVR(buffer.readUUID());
                }
            }
            case REQUESTDATA -> serverWantsData = true;
            case CLIMBING -> {
                serverAllowsClimbey = buffer.readBoolean();
                if (buffer.readableBytes() > 0) {
                    dh.climbTracker.serverblockmode = buffer.readByte();
                    dh.climbTracker.blocklist.clear();

                    while (buffer.readableBytes() > 0) {
                        String s12 = buffer.readUtf(16384);
                        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(s12));

                        // if the block is not there AIR is returned
                        if (block != Blocks.AIR) {
                            dh.climbTracker.blocklist.add(block);
                        }
                    }
                }
            }
            case TELEPORT -> serverSupportsDirectTeleport = true;
            case UBERPACKET -> {
                UUID uuid = buffer.readUUID();
                VRPlayerState vrPlayerState = VRPlayerState.deserialize(buffer);
                float worldScale = buffer.readFloat();
                float heightScale = buffer.readFloat();
                VRPlayersClient.getInstance().Update(uuid, vrPlayerState, worldScale, heightScale);
            }
            case SETTING_OVERRIDE -> {
                while (buffer.readableBytes() > 0) {
                    String s13 = buffer.readUtf(16384);
                    String s14 = buffer.readUtf(16384);
                    String[] astring = s13.split("\\.", 2);

                    if (dh.vrSettings.overrides.hasSetting(astring[0])) {
                        Setting vrsettings$serveroverrides$setting = dh.vrSettings.overrides.getSetting(astring[0]);

                        try {
                            if (astring.length > 1) {
                                String s15 = astring[1];
                                switch (s15) {
                                    case "min":
                                        vrsettings$serveroverrides$setting.setValueMin(Float.parseFloat(s14));
                                        break;

                                    case "max":
                                        vrsettings$serveroverrides$setting.setValueMax(Float.parseFloat(s14));
                                }
                            } else {
                                Object object = vrsettings$serveroverrides$setting.getOriginalValue();

                                if (object instanceof Boolean) {
                                    vrsettings$serveroverrides$setting.setValue("true".equals(s14));
                                } else if (!(object instanceof Integer) && !(object instanceof Byte) && !(object instanceof Short)) {
                                    if (!(object instanceof Float) && !(object instanceof Double)) {
                                        vrsettings$serveroverrides$setting.setValue(s14);
                                    } else {
                                        vrsettings$serveroverrides$setting.setValue(Float.parseFloat(s14));
                                    }
                                } else {
                                    vrsettings$serveroverrides$setting.setValue(Integer.parseInt(s14));
                                }
                            }

                            logger.info("Server setting override: {} = {}", s13, s14);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
            case CRAWL -> serverAllowsCrawling = true;
            case NETWORK_VERSION -> // cast to unsigned byte
                usedNetworkVersion = buffer.readByte() & 0xFF;
            case VR_SWITCHING -> {
                serverAllowsVrSwitching = buffer.readBoolean();
                if (vrInitialized) {
                    if (!serverAllowsVrSwitching) {
                        message(Component.translatable("vivecraft.messages.novrhotswitching"));
                    }
                    dh.vrPlayer.vrSwitchWarning = false;
                }
            }
        }
    }
}
