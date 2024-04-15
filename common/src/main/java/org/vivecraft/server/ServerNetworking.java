package org.vivecraft.server;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.network.packets.VivecraftDataPacket;
import org.vivecraft.mixin.server.ChunkMapAccessor;
import org.vivecraft.server.config.ServerConfig;

import java.util.*;
import java.util.function.Consumer;

public class ServerNetworking {

    // temporarily stores the packets from legacy clients to assemble a complete VrPlayerState
    private static final Map<UUID, Map<CommonNetworkHelper.PacketDiscriminators, FriendlyByteBuf>> legacyDataMap = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("VivecraftServer");

    public static void handlePacket(CommonNetworkHelper.PacketDiscriminators packetID, FriendlyByteBuf buffer, ServerPlayer player, Consumer<ClientboundCustomPayloadPacket> packetConsumer) {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(player);

        if (vivePlayer == null && packetID != CommonNetworkHelper.PacketDiscriminators.VERSION) {
            return;
        }

        switch (packetID) {
            case VERSION:
                // Vivecraft client connected, send server settings

                vivePlayer = new ServerVivePlayer(player);

                // read initial VR State
                byte[] stringBytes = new byte[buffer.readableBytes()];
                buffer.readBytes(stringBytes);
                String[] parts = new String(stringBytes).split("\\n");
                String clientVivecraftVersion = parts[0];

                if (ServerConfig.debug.get()) {
                    LOGGER.info("Vivecraft: player '{}' joined with {}", player.getName().getString(), clientVivecraftVersion);
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
                            LOGGER.info("{} networking supported, using version {}", player.getName().getString(), vivePlayer.networkVersion);
                        }
                    } else {
                        // unsupported version, send notification, and disregard
                        player.sendSystemMessage(Component.literal("Unsupported vivecraft version, VR features will not work"));
                        if (ServerConfig.debug.get()) {
                            LOGGER.info("{} networking not supported. client range [{},{}], server range [{},{}]",
                                player.getName().getString(),
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
                        LOGGER.info("{} using legacy networking", player.getName().getString());
                    }
                }

                vivePlayer.setVR(!clientVivecraftVersion.contains("NONVR"));

                ServerVRPlayers.getPlayersWithVivecraft(player.server).put(player.getUUID(), vivePlayer);

                packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.VERSION, CommonDataHolder.getInstance().versionIdentifier));
                packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.REQUESTDATA, new byte[0]));

                if (ServerConfig.climbeyEnabled.get()) {
                    packetConsumer.accept(getClimbeyServerPacket());
                }

                if (ServerConfig.teleportEnabled.get()) {
                    packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.TELEPORT, new byte[0]));
                }
                if (ServerConfig.teleportLimitedSurvival.get()) {
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeUtf("limitedTeleport");
                    byteBuf.writeUtf("" + true);

                    byteBuf.writeUtf("teleportLimitUp");
                    byteBuf.writeUtf("" + ServerConfig.teleportUpLimit.get());

                    byteBuf.writeUtf("teleportLimitDown");
                    byteBuf.writeUtf("" + ServerConfig.teleportDownLimit.get());

                    byteBuf.writeUtf("teleportLimitHoriz");
                    byteBuf.writeUtf("" + ServerConfig.teleportHorizontalLimit.get());

                    byte[] array = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(array);
                    byteBuf.release();
                    packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.SETTING_OVERRIDE, array));
                }

                if (ServerConfig.worldscaleLimited.get()) {
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeUtf("worldScale.min");
                    byteBuf.writeUtf("" + ServerConfig.worldscaleMin.get());

                    byteBuf.writeUtf("worldScale.max");
                    byteBuf.writeUtf("" + ServerConfig.worldscaleMax.get());

                    byte[] array = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(array);
                    byteBuf.release();
                    packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.SETTING_OVERRIDE, array));
                }

                if (ServerConfig.forceThirdPersonItems.get()) {
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeUtf("thirdPersonItems");
                    byteBuf.writeUtf("" + true);

                    byte[] array = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(array);
                    byteBuf.release();
                    packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.SETTING_OVERRIDE, array));
                }

                if (ServerConfig.crawlingEnabled.get()) {
                    packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.CRAWL, new byte[0]));
                }

                // send if hotswitching is allowed
                packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.VR_SWITCHING, new byte[]{(byte) (ServerConfig.vrSwitchingEnabled.get() && !ServerConfig.vr_only.get() ? 1 : 0)}));

                packetConsumer.accept(getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.NETWORK_VERSION, new byte[]{(byte) vivePlayer.networkVersion}));

                break;
            case IS_VR_ACTIVE:
                if (vivePlayer.isVR() == buffer.readBoolean()) {
                    break;
                }
                vivePlayer.setVR(!vivePlayer.isVR());
                if (!vivePlayer.isVR()) {
                    for (var trackingPlayer : ServerNetworking.getTrackingPlayers(player)) {
                        if (!ServerVRPlayers.getPlayersWithVivecraft(player.server).containsKey(trackingPlayer.getPlayer().getUUID()) || trackingPlayer.getPlayer() == player) {
                            continue;
                        }
                        trackingPlayer.send(createVRActivePlayerPacket(false, player.getUUID()));
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
                player.absMoveTo(f, f1, f2, player.getYRot(), player.getXRot());
                break;

            case CLIMBING:
                player.fallDistance = 0.0F;
                break;
            case ACTIVEHAND:
                vivePlayer.activeHand = buffer.readByte();

                if (vivePlayer.isSeated()) {
                    vivePlayer.activeHand = 0;
                }

                break;

            case CRAWL:
                vivePlayer.crawling = buffer.readByte() != 0;

                if (vivePlayer.crawling) {
                    player.setPose(Pose.SWIMMING);
                }
                break;
            // legacy support
            case CONTROLLER0DATA:
            case CONTROLLER1DATA:
            case HEADDATA:
                Map<CommonNetworkHelper.PacketDiscriminators, FriendlyByteBuf> playerData;
                if ((playerData = legacyDataMap.get(player.getUUID())) == null) {
                    playerData = new HashMap<>();
                    legacyDataMap.put(player.getUUID(), playerData);
                }
                // keep the buffer around
                buffer.retain();
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
                    // release buffers
                    headData.release();
                    controller0Data.release();
                    controller1Data.release();
                    legacyDataMap.remove(player.getUUID());
                }
                break;
        }
    }

    public static Set<ServerPlayerConnection> getTrackingPlayers(Entity entity) {
        var manager = entity.level().getChunkSource();
        var storage = ((ServerChunkCache) manager).chunkMap;
        var playerTracker = ((ChunkMapAccessor) storage).getTrackedEntities().get(entity.getId());
        return playerTracker != null ? playerTracker.getPlayersTracking() : Collections.emptySet();
    }

    public static ClientboundCustomPayloadPacket createVRActivePlayerPacket(boolean vrActive, UUID playerID) {
        FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
        tempBuffer.writeByte(CommonNetworkHelper.PacketDiscriminators.IS_VR_ACTIVE.ordinal());
        tempBuffer.writeBoolean(vrActive);
        tempBuffer.writeUUID(playerID);
        ClientboundCustomPayloadPacket p = new ClientboundCustomPayloadPacket(new VivecraftDataPacket(tempBuffer));
        tempBuffer.release();
        return p;
    }

    public static ClientboundCustomPayloadPacket createUberPacket(Player player, VrPlayerState vrPlayerState, float worldScale, float heightScale) {
        FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
        tempBuffer.writeByte(CommonNetworkHelper.PacketDiscriminators.UBERPACKET.ordinal());
        tempBuffer.writeUUID(player.getUUID());
        vrPlayerState.serialize(tempBuffer);
        tempBuffer.writeFloat(worldScale);
        tempBuffer.writeFloat(heightScale);
        ClientboundCustomPayloadPacket p = new ClientboundCustomPayloadPacket(new VivecraftDataPacket(tempBuffer));
        tempBuffer.release();
        return p;
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, byte[] payload) {
        return new ClientboundCustomPayloadPacket(new VivecraftDataPacket(command, payload));
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
        ClientboundCustomPayloadPacket p = new ClientboundCustomPayloadPacket(new VivecraftDataPacket(friendlybytebuf));
        friendlybytebuf.release();
        return p;
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, String payload) {
        FriendlyByteBuf tempBuffer = new FriendlyByteBuf(Unpooled.buffer());
        tempBuffer.writeByte(command.ordinal());
        tempBuffer.writeUtf(payload);
        ClientboundCustomPayloadPacket p = new ClientboundCustomPayloadPacket(new VivecraftDataPacket(tempBuffer));
        tempBuffer.release();
        return p;
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
