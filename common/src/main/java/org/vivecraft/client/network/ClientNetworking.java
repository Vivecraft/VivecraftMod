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
import org.vivecraft.common.network.BufferSerializable;
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
        return new ServerboundCustomPayloadPacket(new VivecraftDataPacket(command, payload));
    }

    public static ServerboundCustomPayloadPacket createVRActivePacket(boolean vrActive) {
        return new ServerboundCustomPayloadPacket(new VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators.IS_VR_ACTIVE, new byte[]{(byte) (vrActive ? 1 : 0)}));
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

        // clear VR player data
        VRPlayersClient.clear();
        // clear teleport
        VRServerPerms.INSTANCE.setTeleportSupported(false);
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrPlayer.setTeleportOverride(false);
        }
        // clear server overrides
        ClientDataHolderVR.getInstance().vrSettings.overrides.resetAll();
    }

    public static void sendVersionInfo() {
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
            ByteBuf payload = Unpooled.buffer();
            payload.writeFloat(worldScale);
            byte[] out = new byte[payload.readableBytes()];
            payload.readBytes(out);
            ServerboundCustomPayloadPacket packet = getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.WORLDSCALE, out);
            connection.send(packet);

            worldScallast = worldScale;
        }

        float userHeight = AutoCalibration.getPlayerHeight();

        if (userHeight != heightlast) {
            ByteBuf payload = Unpooled.buffer();
            payload.writeFloat(userHeight / AutoCalibration.defaultHeight);
            byte[] out = new byte[payload.readableBytes()];
            payload.readBytes(out);
            ServerboundCustomPayloadPacket packet = getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.HEIGHT, out);
            connection.send(packet);

            heightlast = userHeight;
        }

        var vrPlayerState = VrPlayerState.create(vrPlayer);

        if (usedNetworkVersion >= 0) {
            connection.send(createVrPlayerStatePacket(vrPlayerState));
        } else {
            sendLegacyPackets(connection, vrPlayerState);
        }
        VRPlayersClient.getInstance().update(Minecraft.getInstance().player.getGameProfile().getId(), vrPlayerState, worldScale, userHeight / AutoCalibration.defaultHeight, true);
    }

    private static byte[] serializeToArray(BufferSerializable object, byte[] additionalData) {
        FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
        if (additionalData != null) {
            tempBuffer.writeBytes(additionalData);
        }
        object.serialize(tempBuffer);
        byte[] buffer = new byte[tempBuffer.readableBytes()];
        tempBuffer.readBytes(buffer);
        tempBuffer.release();
        return buffer;
    }

    public static ServerboundCustomPayloadPacket createVrPlayerStatePacket(VrPlayerState vrPlayerState) {
        return new ServerboundCustomPayloadPacket(new VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators.VR_PLAYER_STATE, serializeToArray(vrPlayerState, null)));
    }

    public static void sendLegacyPackets(ClientPacketListener connection, VrPlayerState vrPlayerState) {
        // main controller packet
        connection.send(new ServerboundCustomPayloadPacket(new VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators.CONTROLLER0DATA, serializeToArray(vrPlayerState.controller0(), new byte[]{(byte) (ClientDataHolderVR.getInstance().vrSettings.reverseHands ? 1 : 0)}))));

        // offhand controller packet
        connection.send(new ServerboundCustomPayloadPacket(new VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators.CONTROLLER1DATA, serializeToArray(vrPlayerState.controller1(), new byte[]{(byte) (ClientDataHolderVR.getInstance().vrSettings.reverseHands ? 1 : 0)}))));

        // hmd packet
        connection.send(new ServerboundCustomPayloadPacket(new VivecraftDataPacket(CommonNetworkHelper.PacketDiscriminators.HEADDATA, serializeToArray(vrPlayerState.hmd(), new byte[]{(byte) (ClientDataHolderVR.getInstance().vrSettings.seated ? 1 : 0)}))));
    }

    // ServerSetting override checks

    public static boolean isThirdPersonItems() {
        return ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.THIRDPERSON_ITEMTRANSFORMS).getBoolean();
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
            ServerboundCustomPayloadPacket packet = getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.ACTIVEHAND, new byte[]{c});

            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(packet);
            }
        }
    }

    public static void overridePose(LocalPlayer player) {
        if (ClientDataHolderVR.getInstance().crawlTracker.crawling) {
            player.setPose(Pose.SWIMMING);
        }
    }

    public static void overrideLook(Player player, Vec3 view) {
        if (serverWantsData) return; // shouldn't be needed, don't tease the anti-cheat.

        capturedPitch = player.getXRot();
        capturedYaw = player.getYRot();
        float pitch = (float) Math.toDegrees(Math.asin(-view.y / view.length()));
        float yaw = (float) Math.toDegrees(Math.atan2(-view.x, view.z));
        ((LocalPlayer) player).connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround()));
        overrideActive = true;
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
                String version = buffer.readUtf(1024);
                VRServerPerms.INSTANCE.setTeleportSupported(true);
                if (VRState.vrInitialized) {
                    dataholder.vrPlayer.teleportWarning = false;
                    dataholder.vrPlayer.vrSwitchWarning = true;
                }
                if (!ClientNetworking.displayedChatMessage &&
                    (dataholder.vrSettings.showServerPluginMessage == VRSettings.ChatServerPluginMessage.ALWAYS ||
                        (dataholder.vrSettings.showServerPluginMessage ==
                            VRSettings.ChatServerPluginMessage.SERVER_ONLY && !Minecraft.getInstance().isLocalServer()
                        )
                    ))
                {
                    ClientNetworking.displayedChatMessage = true;
                    mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.serverplugin", version));
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
                    dataholder.climbTracker.serverBlockmode = buffer.readByte();
                    dataholder.climbTracker.blocklist.clear();

                    while (buffer.readableBytes() > 0) {
                        String name = buffer.readUtf(16384);
                        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(name));

                        // if the block is not there AIR is returned
                        if (block != Blocks.AIR) {
                            dataholder.climbTracker.blocklist.add(block);
                        }
                    }
                }
            }
            case TELEPORT -> ClientNetworking.serverSupportsDirectTeleport = true;
            case UBERPACKET -> {
                UUID uuid = buffer.readUUID();
                var vrPlayerState = VrPlayerState.deserialize(buffer);
                float worldScale = buffer.readFloat();
                float heightScale = buffer.readFloat();
                VRPlayersClient.getInstance().update(uuid, vrPlayerState, worldScale, heightScale);
            }
            case SETTING_OVERRIDE -> {
                while (buffer.readableBytes() > 0) {
                    String settingName = buffer.readUtf(16384);
                    String value = buffer.readUtf(16384);
                    String[] split = settingName.split("\\.", 2);

                    if (dataholder.vrSettings.overrides.hasSetting(split[0])) {
                        VRSettings.ServerOverrides.Setting setting = dataholder.vrSettings.overrides.getSetting(split[0]);

                        try {
                            if (split.length > 1) {
                                String s15 = split[1];
                                switch (s15) {
                                    case "min":
                                        setting.setValueMin(Float.parseFloat(value));
                                        break;

                                    case "max":
                                        setting.setValueMax(Float.parseFloat(value));
                                }
                            } else {
                                Object origValue = setting.getOriginalValue();

                                if (origValue instanceof Boolean) {
                                    setting.setValue(value.equals("true"));
                                } else if (origValue instanceof Integer || origValue instanceof Byte || origValue instanceof Short) {
                                    setting.setValue(Integer.parseInt(value));
                                } else if (origValue instanceof Float || origValue instanceof Double) {
                                    setting.setValue(Float.parseFloat(value));
                                } else {
                                    setting.setValue(value);
                                }
                            }

                            VRSettings.logger.info("Server setting override: {}={}", settingName, value);
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
