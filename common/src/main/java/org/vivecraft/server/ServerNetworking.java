package org.vivecraft.server;

import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.mixin.server.ChunkMapAccessor;
import org.vivecraft.server.config.ServerConfig;

import java.util.*;

import static org.vivecraft.server.ServerVRPlayers.getVivePlayer;

public class ServerNetworking {

    // temporarily stores the packets from legacy clients to assemble a complete VrPlayerState
    private final static Map<UUID, Map<CommonNetworkHelper.PacketDiscriminators, FriendlyByteBuf>> legacyDataMap = new HashMap<>();

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void handlePacket(CommonNetworkHelper.PacketDiscriminators packetID, FriendlyByteBuf buffer, ServerGamePacketListenerImpl listener) {
        var playerEntity = listener.player;
        ServerVivePlayer vivePlayer = getVivePlayer(playerEntity);

        if (vivePlayer == null && packetID != CommonNetworkHelper.PacketDiscriminators.VERSION) {
            return;
        }

        switch (packetID) {
            case VERSION:
                // Vivecraft client connected, send server settings

                vivePlayer = new ServerVivePlayer(playerEntity);

                // read initial VR State
                byte[] stringBytes = new byte[buffer.readableBytes()];
                buffer.readBytes(stringBytes);
                String[] parts = new String(stringBytes).split("\\n");
                String clientVivecraftVersion = parts[0];

                if (ServerConfig.debug.get()) {
                    LOGGER.info("Vivecraft: player '{}' joined with {}", listener.player.getName().getString(), clientVivecraftVersion);
                }

                if (parts.length >= 3) {
                    // has versions
                    int clientMaxVersion = Integer.parseInt(parts[1]);
                    int clientMinVersion = Integer.parseInt(parts[2]);
                    // check if client supports a supported version
                    if (CommonNetworkHelper.MIN_SUPPORTED_NETWORK_VERSION <= clientMaxVersion
                        && clientMinVersion <= CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION) {
                        vivePlayer.networkVersion = Math.min(clientMaxVersion, CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION);
                        if (ServerConfig.debug.get()) {
                            LOGGER.info("{} networking supported, using version vivePlayer.networkVersion", listener.player.getName().getString());
                        }
                    } else {
                        // unsupported version, send notification, and disregard
                        listener.player.sendSystemMessage(Component.literal("Unsupported vivecraft version, VR features will not work"));
                        if (ServerConfig.debug.get()) {
                            LOGGER.info("{} networking not supported. client range [{},{}], server range [{},{}]",
                                listener.player.getName().getString(),
                                clientMinVersion,
                                clientMaxVersion,
                                CommonNetworkHelper.MIN_SUPPORTED_NETWORK_VERSION,
                                CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION);
                        }
                        return;
                    }
                } else {
                    // client didn't send a version, so it's a legacy client
                    vivePlayer.networkVersion = -1;
                    if (ServerConfig.debug.get()) {
                        LOGGER.info("{} using legacy networking", listener.player.getName().getString());
                    }
                }

                vivePlayer.setVR(!clientVivecraftVersion.contains("NONVR"));

                ServerVRPlayers.getPlayersWithVivecraft(listener.player.server).put(playerEntity.getUUID(), vivePlayer);

                listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.VERSION, CommonDataHolder.getInstance().versionIdentifier));
                listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.REQUESTDATA, new byte[0]));

                if (ServerConfig.climbeyEnabled.get()) {
                    listener.send(getClimbeyServerPacket());
                }

                if (ServerConfig.teleportEnabled.get()) {
                    listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.TELEPORT, new byte[0]));
                }
                if (ServerConfig.teleportLimitedSurvival.get()) {
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeUtf("limitedTeleport");
                    byteBuf.writeUtf(""+true);

                    byteBuf.writeUtf("teleportLimitUp");
                    byteBuf.writeUtf(""+ ServerConfig.teleportUpLimit.get());

                    byteBuf.writeUtf("teleportLimitDown");
                    byteBuf.writeUtf(""+ ServerConfig.teleportDownLimit.get());

                    byteBuf.writeUtf("teleportLimitHoriz");
                    byteBuf.writeUtf(""+ ServerConfig.teleportHorizontalLimit.get());

                    listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.SETTING_OVERRIDE, byteBuf.readByteArray()));
                }

                if (ServerConfig.worldscaleLimited.get()) {
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeUtf("worldScale.min");
                    byteBuf.writeUtf(""+ ServerConfig.worldscaleMin.get());

                    byteBuf.writeUtf("worldScale.max");
                    byteBuf.writeUtf(""+ ServerConfig.worldscaleMax.get());

                    listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.SETTING_OVERRIDE, byteBuf.readByteArray()));
                }

                if (ServerConfig.crawlingEnabled.get()) {
                    listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.CRAWL, new byte[0]));
                }

                // send if hotswitching is allowed
                listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.VR_SWITCHING, new byte[]{(byte)(ServerConfig.vrSwitchingEnabled.get() && !ServerConfig.vr_only.get() ? 1 : 0)}));

                listener.send(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.NETWORK_VERSION, new byte[]{(byte)vivePlayer.networkVersion}));

                break;
            case IS_VR_ACTIVE:
                if (vivePlayer.isVR() == buffer.readBoolean()) {
                    break;
                }
                vivePlayer.setVR(!vivePlayer.isVR());
                if (!vivePlayer.isVR()) {
                    for (var trackingPlayer : ServerNetworking.getTrackingPlayers(playerEntity)) {
                        if (!ServerVRPlayers.getPlayersWithVivecraft(listener.player.server).containsKey(trackingPlayer.getPlayer().getUUID()) || trackingPlayer.getPlayer() == playerEntity) {
                            continue;
                        }
                        trackingPlayer.send(createVRActivePlayerPacket(false, playerEntity.getUUID()));
                    }
                }
                break;

            case DRAW:
                vivePlayer.draw = buffer.readFloat();
                break;

            case VR_PLAYER_STATE:
                vivePlayer.vrPlayerState = VrPlayerState.deserialize(buffer);

            case MOVEMODE:
            case REQUESTDATA:
            default:
                break;

            case WORLDSCALE:
                vivePlayer.worldScale = buffer.readFloat();
                break;
            case HEIGHT:
                vivePlayer.heightScale = buffer.readFloat();
                break;

            case TELEPORT:
                float f = buffer.readFloat();
                float f1 = buffer.readFloat();
                float f2 = buffer.readFloat();
                playerEntity.absMoveTo(f, f1, f2, playerEntity.getYRot(), playerEntity.getXRot());
                break;

            case CLIMBING:
                playerEntity.fallDistance = 0.0F;
            case ACTIVEHAND:
                vivePlayer.activeHand = buffer.readByte();

                if (vivePlayer.isSeated()) {
                    vivePlayer.activeHand = 0;
                }

                break;

            case CRAWL:
                vivePlayer.crawling = buffer.readByte() != 0;

                if (vivePlayer.crawling) {
                    playerEntity.setPose(Pose.SWIMMING);
                }
                break;
            // legacy support
            case CONTROLLER0DATA:
            case CONTROLLER1DATA:
            case HEADDATA:
                Map<CommonNetworkHelper.PacketDiscriminators, FriendlyByteBuf> playerData;
                if ((playerData = legacyDataMap.get(playerEntity.getUUID())) == null) {
                    playerData = new HashMap<>();
                    legacyDataMap.put(playerEntity.getUUID(), playerData);
                }

                playerData.put(packetID, buffer);

                if (playerData.size() == 3) {
                    FriendlyByteBuf controller0Data = playerData.get(CommonNetworkHelper.PacketDiscriminators.CONTROLLER0DATA);
                    controller0Data.resetReaderIndex().readByte();
                    FriendlyByteBuf controller1Data = playerData.get(CommonNetworkHelper.PacketDiscriminators.CONTROLLER1DATA);
                    controller1Data.resetReaderIndex().readByte();
                    FriendlyByteBuf headData = playerData.get(CommonNetworkHelper.PacketDiscriminators.HEADDATA);
                    headData.resetReaderIndex().readByte();

                    vivePlayer.vrPlayerState = new VrPlayerState(
                            headData.readBoolean(), // isSeated
                            org.vivecraft.common.network.Pose.deserialize(headData), // head pose
                            controller0Data.readBoolean(), // reverseHands 0
                            org.vivecraft.common.network.Pose.deserialize(controller0Data), // controller0 pose
                            controller1Data.readBoolean(), // reverseHands 1
                            org.vivecraft.common.network.Pose.deserialize(controller1Data)); // controller1 pose
                    legacyDataMap.remove(playerEntity.getUUID());
                }
                break;
        }
    }

    public static Set<ServerPlayerConnection> getTrackingPlayers(Entity entity) {
        var manager = entity.level.getChunkSource();
        var storage = ((ServerChunkCache) manager).chunkMap;
        var playerTracker = ((ChunkMapAccessor) storage).getTrackedEntities().get(entity.getId());
        return playerTracker != null ? playerTracker.getPlayersTracking() : Collections.emptySet();
    }

    public static ClientboundCustomPayloadPacket createVRActivePlayerPacket(boolean vrActive, UUID playerID) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.IS_VR_ACTIVE.ordinal());
        buffer.writeBoolean(vrActive);
        buffer.writeUUID(playerID);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, buffer);
    }

    public static ClientboundCustomPayloadPacket createUberPacket(Player player, VrPlayerState vrPlayerState, float worldScale, float heightScale) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.UBERPACKET.ordinal());
        buffer.writeUUID(player.getUUID());
        vrPlayerState.serialize(buffer);
        buffer.writeFloat(worldScale);
        buffer.writeFloat(heightScale);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, buffer);
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, byte[] payload) {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, friendlybytebuf);
    }

    public static ClientboundCustomPayloadPacket getClimbeyServerPacket() {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(CommonNetworkHelper.PacketDiscriminators.CLIMBING.ordinal());
        friendlybytebuf.writeBoolean(true);
        if (!"DISABLED".equals(ServerConfig.climbeyBlockmode.get())) {
            if ("WHITELIST".equals(ServerConfig.climbeyBlockmode.get())) {
                friendlybytebuf.writeByte(1);
            } else {
                friendlybytebuf.writeByte(2);
            }
            for (String block : ServerConfig.climbeyBlocklist.get()) {
                friendlybytebuf.writeUtf(block);
            }
        } else {
            // no block list
            friendlybytebuf.writeByte(0);
        }
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, friendlybytebuf);
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, String payload) {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeUtf(payload);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, friendlybytebuf);
    }

    public static void sendVrPlayerStateToClients(ServerPlayer vrPlayerEntity) {
        var playersWithVivecraft = ServerVRPlayers.getPlayersWithVivecraft(vrPlayerEntity.server);
        var vivePlayer = playersWithVivecraft.get(vrPlayerEntity.getUUID());
        if (vivePlayer == null) {
            return;
        }
        if (vivePlayer.player == null || vivePlayer.player.hasDisconnected()) {
            playersWithVivecraft.remove(vrPlayerEntity.getUUID());
        }
        if (!vivePlayer.isVR() || vivePlayer.vrPlayerState == null) {
            return;
        }
        for (var trackingPlayer : getTrackingPlayers(vrPlayerEntity)) {
            if (!playersWithVivecraft.containsKey(trackingPlayer.getPlayer().getUUID()) || trackingPlayer.getPlayer() == vrPlayerEntity) {
                continue;
            }
            trackingPlayer.send(createUberPacket(vivePlayer.player, vivePlayer.vrPlayerState, vivePlayer.worldScale, vivePlayer.heightScale));
        }
    }
}
