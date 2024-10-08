package org.vivecraft.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
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
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.common.network.packet.c2s.*;
import org.vivecraft.common.network.packet.s2c.*;

import java.util.Map;

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
        // send version string, with currently running
        Minecraft.getInstance().getConnection().send(createServerPacket(
            new VersionPayloadC2S(
                CommonDataHolder.getInstance().versionIdentifier,
                VRState.vrRunning,
                CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION,
                CommonNetworkHelper.MIN_SUPPORTED_NETWORK_VERSION)));
    }

    public static void sendVRPlayerPositions(VRPlayer vrPlayer) {
        var connection = Minecraft.getInstance().getConnection();
        if (!serverWantsData || connection == null) {
            return;
        }

        float worldScale = ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_post.worldScale;

        if (worldScale != worldScallast) {
            connection.send(createServerPacket(new WorldScalePayloadC2S(worldScale)));

            worldScallast = worldScale;
        }

        float userHeight = AutoCalibration.getPlayerHeight();

        if (userHeight != heightlast) {
            connection.send(createServerPacket(new HeightPayloadC2S(userHeight / AutoCalibration.defaultHeight)));

            heightlast = userHeight;
        }

        var vrPlayerState = VrPlayerState.create(vrPlayer);

        if (usedNetworkVersion >= 0) {
            connection.send(createServerPacket(new VRPlayerStatePayloadC2S(vrPlayerState)));
        } else {
            sendLegacyPackets(connection, vrPlayerState);
        }
        VRPlayersClient.getInstance()
            .update(Minecraft.getInstance().player.getGameProfile().getId(), vrPlayerState, worldScale,
                userHeight / AutoCalibration.defaultHeight, true);
    }

    public static Packet<?> createServerPacket(VivecraftPayloadC2S payload) {
        return Xplat.getC2SPacket(payload);
    }

    public static void sendLegacyPackets(ClientPacketListener connection, VrPlayerState vrPlayerState) {
        // main controller packet
        connection.send(createServerPacket(
            new LegacyController0DataPayloadC2S(ClientDataHolderVR.getInstance().vrSettings.reverseHands,
                vrPlayerState.controller0())));

        // offhand controller packet
        connection.send(createServerPacket(
            new LegacyController1DataPayloadC2S(ClientDataHolderVR.getInstance().vrSettings.reverseHands,
                vrPlayerState.controller1())));

        // hmd packet
        connection.send(createServerPacket(
            new LegacyHeadDataPayloadC2S(ClientDataHolderVR.getInstance().vrSettings.seated, vrPlayerState.hmd())));
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
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(createServerPacket(new ActiveHandPayloadC2S(c)));
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

    public static void handlePacket(VivecraftPayloadS2C s2cPayload) {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();
        switch (s2cPayload.payloadId()) {
            case VERSION -> {
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
                    mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.serverplugin",
                        ((VersionPayloadS2C) s2cPayload).version()));
                }
                if (VRState.vrEnabled && dataholder.vrSettings.manualCalibration == -1.0F && !dataholder.vrSettings.seated) {
                    mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.calibrateheight"));
                }
            }
            case IS_VR_ACTIVE -> {
                VRActivePayloadS2C packet = (VRActivePayloadS2C) s2cPayload;
                if (!packet.vr()) {
                    VRPlayersClient.getInstance().disableVR(packet.playerID());
                }
            }
            case REQUESTDATA -> ClientNetworking.serverWantsData = true;
            case CLIMBING -> {
                ClimbingPayloadS2C packet = (ClimbingPayloadS2C) s2cPayload;
                ClientNetworking.serverAllowsClimbey = packet.allowed();
                dataholder.climbTracker.serverBlockmode = packet.blockmode();
                dataholder.climbTracker.blocklist.clear();

                if (packet.blocks() != null) {
                    for (String blockId : packet.blocks()) {
                        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId));

                        // if the block is not there AIR is returned
                        if (block != Blocks.AIR) {
                            dataholder.climbTracker.blocklist.add(block);
                        }
                    }
                }
            }
            case TELEPORT -> ClientNetworking.serverSupportsDirectTeleport = true;
            case UBERPACKET -> {
                UberPacketPayloadS2C packet = (UberPacketPayloadS2C) s2cPayload;
                VRPlayersClient.getInstance().update(packet.playerID(), packet.state(), packet.worldScale(), packet.heightScale());
            }
            case SETTING_OVERRIDE -> {
                for (Map.Entry<String, String> override : ((SettingOverridePayloadS2C) s2cPayload).overrides().entrySet()) {
                    String[] split = override.getKey().split("\\.", 2);

                    if (dataholder.vrSettings.overrides.hasSetting(split[0])) {
                        VRSettings.ServerOverrides.Setting setting = dataholder.vrSettings.overrides.getSetting(split[0]);

                        try {
                            if (split.length > 1) {
                                switch (split[1]) {
                                    case "min" -> setting.setValueMin(Float.parseFloat(override.getValue()));
                                    case "max"-> setting.setValueMax(Float.parseFloat(override.getValue()));
                                }
                            } else {
                                Object origValue = setting.getOriginalValue();

                                if (origValue instanceof Boolean) {
                                    setting.setValue(override.getValue().equals("true"));
                                } else if (origValue instanceof Integer || origValue instanceof Byte || origValue instanceof Short) {
                                    setting.setValue(Integer.parseInt(override.getValue()));
                                } else if (origValue instanceof Float || origValue instanceof Double) {
                                    setting.setValue(Float.parseFloat(override.getValue()));
                                } else {
                                    setting.setValue(override.getValue());
                                }
                            }

                            VRSettings.logger.info("Vivecraft: Server setting override: {}={}", override.getKey(), override.getValue());
                        } catch (Exception exception) {
                            VRSettings.logger.error("Vivecraft: error parsing server setting override: ", exception);
                        }
                    }
                }
            }
            case CRAWL -> ClientNetworking.serverAllowsCrawling = true;
            case NETWORK_VERSION ->
                ClientNetworking.usedNetworkVersion = ((NetworkVersionPayloadS2C) s2cPayload).version();
            case VR_SWITCHING -> {
                ClientNetworking.serverAllowsVrSwitching = ((VRSwitchingPayloadS2C) s2cPayload).allowed();
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
