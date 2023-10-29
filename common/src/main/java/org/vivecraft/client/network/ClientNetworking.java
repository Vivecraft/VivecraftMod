package org.vivecraft.client.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.network.packets.VivecraftDataPacket;

import java.util.UUID;

public class ClientNetworking {

    public static boolean displayedChatMessage = false;
    public static boolean displayedChatWarning = false;
    public static boolean serverWantsData = false;
    public static boolean serverAllowsClimbey = false;
    public static boolean serverSupportsDirectTeleport = false;
    public static boolean serverAllowsTeleport = true;
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

    public static ServerboundCustomPayloadPacket getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators command, byte[] payload) {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ServerboundCustomPayloadPacket(new VivecraftDataPacket(friendlybytebuf));
    }

    public static ServerboundCustomPayloadPacket createVRActivePacket(boolean vrActive) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.IS_VR_ACTIVE.ordinal());
        buffer.writeBoolean(vrActive);
        return new ServerboundCustomPayloadPacket(new VivecraftDataPacket(buffer));
    }

    public static void resetServerSettings() {
        worldScallast = 0.0F;
        heightlast = 0.0F;
        serverAllowsClimbey = false;
        serverWantsData = false;
        serverSupportsDirectTeleport = false;
        serverAllowsTeleport = true;
        serverAllowsCrawling = false;
        serverAllowsVrSwitching = false;
        usedNetworkVersion = -1;
        //DataHolder.getInstance().vrSettings.overrides.resetAll(); move to mixin
    }

    public static void sendVersionInfo() {
        //Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new ChannelRegisterPacket(CommonNetworkHelper.CHANNEL.toString())));
        Xplat.addNetworkChannel(Minecraft.getInstance().getConnection(), CommonNetworkHelper.CHANNEL);
        // send version string, with currently running
        Minecraft.getInstance().getConnection().send(getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.VERSION,
            (CommonDataHolder.getInstance().versionIdentifier + (VRState.vrRunning ? " VR" : " NONVR")
                + "\n" + CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION
                + "\n" + CommonNetworkHelper.MIN_SUPPORTED_NETWORK_VERSION
            ).getBytes(Charsets.UTF_8)));
    }

    public static void sendVRPlayerPositions(VRPlayer vrPlayer) {
        var connection = Minecraft.getInstance().getConnection();
        if (!serverWantsData || connection == null) {
            return;
        }

        float worldScale = ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_post.worldScale;

        if (worldScale != worldScallast) {
            ByteBuf bytebuf = Unpooled.buffer();
            bytebuf.writeFloat(worldScale);
            byte[] abyte = new byte[bytebuf.readableBytes()];
            bytebuf.readBytes(abyte);
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket = getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.WORLDSCALE, abyte);
            Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket);
            worldScallast = worldScale;
        }

        float f1 = AutoCalibration.getPlayerHeight();

        if (f1 != heightlast) {
            ByteBuf bytebuf2 = Unpooled.buffer();
            bytebuf2.writeFloat(f1 / 1.52F);
            byte[] abyte3 = new byte[bytebuf2.readableBytes()];
            bytebuf2.readBytes(abyte3);
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket1 = getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.HEIGHT, abyte3);
            Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket1);
            heightlast = f1;
        }

        var vrPlayerState = VrPlayerState.create(vrPlayer);

        if (usedNetworkVersion >= 0) {
            connection.send(createVrPlayerStatePacket(vrPlayerState));
        } else {
            sendLegacyPackets(connection, vrPlayerState);
        }
        VRPlayersClient.getInstance().Update(Minecraft.getInstance().player.getGameProfile().getId(), vrPlayerState, worldScale, f1 / 1.52F, true);
    }

    public static ServerboundCustomPayloadPacket createVrPlayerStatePacket(VrPlayerState vrPlayerState) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.VR_PLAYER_STATE.ordinal());
        vrPlayerState.serialize(buffer);
        return new ServerboundCustomPayloadPacket(new VivecraftDataPacket(buffer));
    }

    public static void sendLegacyPackets(ClientPacketListener connection, VrPlayerState vrPlayerState) {
        // left controller packet
        FriendlyByteBuf controller0Buffer = new FriendlyByteBuf(Unpooled.buffer());
        controller0Buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.CONTROLLER0DATA.ordinal());
        controller0Buffer.writeBoolean(ClientDataHolderVR.getInstance().vrSettings.reverseHands);
        vrPlayerState.controller0().serialize(controller0Buffer);
        connection.send(new ServerboundCustomPayloadPacket(new VivecraftDataPacket(controller0Buffer)));

        // right controller packet
        FriendlyByteBuf controller1Buffer = new FriendlyByteBuf(Unpooled.buffer());
        controller1Buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.CONTROLLER1DATA.ordinal());
        controller1Buffer.writeBoolean(ClientDataHolderVR.getInstance().vrSettings.reverseHands);
        vrPlayerState.controller1().serialize(controller1Buffer);
        connection.send(new ServerboundCustomPayloadPacket(new VivecraftDataPacket(controller1Buffer)));

        // hmd packet
        FriendlyByteBuf headBuffer = new FriendlyByteBuf(Unpooled.buffer());
        headBuffer.writeByte(CommonNetworkHelper.PacketDiscriminators.HEADDATA.ordinal());
        headBuffer.writeBoolean(ClientDataHolderVR.getInstance().vrSettings.seated);
        vrPlayerState.hmd().serialize(headBuffer);
        connection.send(new ServerboundCustomPayloadPacket(new VivecraftDataPacket(headBuffer)));
    }

    public static boolean isLimitedSurvivalTeleport() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.LIMIT_TELEPORT).getBoolean();
    }

    public static int getTeleportUpLimit() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_UP_LIMIT).getInt();
    }

    public static int getTeleportDownLimit() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_DOWN_LIMIT).getInt();
    }

    public static int getTeleportHorizLimit() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_HORIZ_LIMIT).getInt();
    }

    public static void sendActiveHand(byte c) {
        if (serverWantsData) {
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket = getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.ACTIVEHAND, new byte[]{c});

            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(serverboundcustompayloadpacket);
            }
        }
    }

    public static void overridePose(LocalPlayer player) {
        if (ClientDataHolderVR.getInstance().crawlTracker.crawling) {
            player.setPose(Pose.SWIMMING);
        }
    }

    public static void overrideLook(Player player, Vec3 view) {
        if (!serverWantsData) {
            capturedPitch = player.getXRot();
            capturedYaw = player.getYRot();
            float f = (float) Math.toDegrees(Math.asin(-view.y / view.length()));
            float f1 = (float) Math.toDegrees(Math.atan2(-view.x, view.z));
            ((LocalPlayer) player).connection.send(new ServerboundMovePlayerPacket.Rot(f1, f, player.onGround()));
            overrideActive = true;
        }
    }

    public static void restoreLook(Player player) {
        if (!serverWantsData) {
            if (overrideActive) {
                ((LocalPlayer) player).connection.send(new ServerboundMovePlayerPacket.Rot(capturedYaw, capturedPitch, player.onGround()));
                overrideActive = false;
            }
        }
    }

    public static void handlePacket(CommonNetworkHelper.PacketDiscriminators packetID, FriendlyByteBuf buffer) {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();
        switch (packetID) {
            case VERSION -> {
                String s11 = buffer.readUtf(1024);
                VRServerPerms.INSTANCE.setTeleportSupported(true);
                if (VRState.vrInitialized) {
                    dataholder.vrPlayer.teleportWarning = false;
                    dataholder.vrPlayer.vrSwitchWarning = true;
                }
                if (!ClientNetworking.displayedChatMessage
                    && (dataholder.vrSettings.showServerPluginMessage == VRSettings.ChatServerPluginMessage.ALWAYS
                    || (dataholder.vrSettings.showServerPluginMessage == VRSettings.ChatServerPluginMessage.SERVER_ONLY && !Minecraft.getInstance().isLocalServer()))) {
                    ClientNetworking.displayedChatMessage = true;
                    mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.serverplugin", s11));
                }
                if (VRState.vrEnabled && dataholder.vrSettings.manualCalibration == -1.0F && !dataholder.vrSettings.seated) {
                    mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.calibrateheight"));
                }
            }
            case IS_VR_ACTIVE -> {
                if (!buffer.readBoolean()) {
                    VRPlayersClient.getInstance().disableVR(buffer.readUUID());
                }
            }
            case REQUESTDATA -> ClientNetworking.serverWantsData = true;
            case CLIMBING -> {
                ClientNetworking.serverAllowsClimbey = buffer.readBoolean();
                if (buffer.readableBytes() > 0) {
                    dataholder.climbTracker.serverblockmode = buffer.readByte();
                    dataholder.climbTracker.blocklist.clear();

                    while (buffer.readableBytes() > 0) {
                        String s12 = buffer.readUtf(16384);
                        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(s12));

                        // if the block is not there AIR is returned
                        if (block != Blocks.AIR) {
                            dataholder.climbTracker.blocklist.add(block);
                        }
                    }
                }
            }
            case TELEPORT -> {
                ClientNetworking.serverSupportsDirectTeleport = true;
                if (buffer.readableBytes() > 0) {
                    ClientNetworking.serverAllowsTeleport = buffer.readBoolean();
                    if (!ClientNetworking.serverAllowsTeleport) {
                        dataholder.vrPlayer.setTeleportOverride(false);
                    }
                } else {
                    ClientNetworking.serverAllowsTeleport = true;
                }
            }
            case UBERPACKET -> {
                UUID uuid = buffer.readUUID();
                var vrPlayerState = VrPlayerState.deserialize(buffer);
                float worldScale = buffer.readFloat();
                float heightScale = buffer.readFloat();
                VRPlayersClient.getInstance().Update(uuid, vrPlayerState, worldScale, heightScale);
            }
            case SETTING_OVERRIDE -> {
                while (buffer.readableBytes() > 0) {
                    String s13 = buffer.readUtf(16384);
                    String s14 = buffer.readUtf(16384);
                    String[] astring = s13.split("\\.", 2);

                    if (dataholder.vrSettings.overrides.hasSetting(astring[0])) {
                        VRSettings.ServerOverrides.Setting vrsettings$serveroverrides$setting = dataholder.vrSettings.overrides.getSetting(astring[0]);

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
                                    vrsettings$serveroverrides$setting.setValue(s14.equals("true"));
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

                            System.out.println("Server setting override: " + s13 + " = " + s14);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
            case CRAWL -> ClientNetworking.serverAllowsCrawling = true;
            case NETWORK_VERSION -> // cast to unsigned byte
                ClientNetworking.usedNetworkVersion = buffer.readByte() & 0xFF;
            case VR_SWITCHING -> {
                ClientNetworking.serverAllowsVrSwitching = buffer.readBoolean();
                if (VRState.vrInitialized) {
                    if (!ClientNetworking.serverAllowsVrSwitching) {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("vivecraft.messages.novrhotswitching"));
                    }
                    dataholder.vrPlayer.vrSwitchWarning = false;
                }
            }
        }
    }
}
