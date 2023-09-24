package org.vivecraft.server;

import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators;
import org.vivecraft.common.network.VRPlayerState;
import org.vivecraft.mixin.server.ChunkMapAccessor;
import org.vivecraft.server.config.ServerConfig;

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

import java.util.*;

import static org.vivecraft.common.utils.Utils.logger;
import static org.vivecraft.server.ServerVRPlayers.getVivePlayer;

import static org.joml.Math.*;

public class ServerNetworking {

    // temporarily stores the packets from legacy clients to assemble a complete VRPlayerState
    private static final Map<UUID, Map<PacketDiscriminators, FriendlyByteBuf>> legacyDataMap = new HashMap<>();

    public static void handlePacket(PacketDiscriminators packetID, FriendlyByteBuf buffer, ServerGamePacketListenerImpl listener) {
        ServerPlayer playerEntity = listener.player;
        ServerVivePlayer vivePlayer = getVivePlayer(playerEntity);

        if (vivePlayer == null && packetID != PacketDiscriminators.VERSION)
        {
            return;
        }

        switch (packetID) {
            case VERSION ->
            {
                // Vivecraft client connected, send server settings

                vivePlayer = new ServerVivePlayer(playerEntity);

                // read initial VR State
                byte[] stringBytes = new byte[buffer.readableBytes()];
                buffer.readBytes(stringBytes);
                String[] parts = new String(stringBytes).split("\\n");
                String clientVivecraftVersion = parts[0];

                if (ServerConfig.debug.get())
                {
                    logger.info("player '{}' joined with {}", listener.player.getName().getString(), clientVivecraftVersion);
                }

                if (parts.length >= 3)
                {
                    // has versions
                    int clientMaxVersion = Integer.parseInt(parts[1]);
                    int clientMinVersion = Integer.parseInt(parts[2]);
                    // check if client supports a supported version
                    if (CommonNetworkHelper.MIN_SUPPORTED_NETWORK_VERSION <= clientMaxVersion
                        && clientMinVersion <= CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION)
                    {
                        vivePlayer.networkVersion = min(clientMaxVersion, CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION);
                        if (ServerConfig.debug.get())
                        {
                            logger.info("{} networking supported, using version {}", listener.player.getName().getString(), vivePlayer.networkVersion);
                        }
                    }
                    else
                    {
                        // unsupported version, send notification, and disregard
                        listener.player.sendSystemMessage(Component.literal("Unsupported vivecraft version, VR features will not work"));
                        if (ServerConfig.debug.get())
                        {
                            logger.info("{} networking not supported. client range [{},{}], server range [{},{}]",
                                    listener.player.getName().getString(),
                                    clientMinVersion,
                                    clientMaxVersion,
                                    CommonNetworkHelper.MIN_SUPPORTED_NETWORK_VERSION,
                                    CommonNetworkHelper.MAX_SUPPORTED_NETWORK_VERSION);
                        }
                        return;
                    }
                }
                    else
                {
                    // client didn't send a version, so it's a legacy client
                    vivePlayer.networkVersion = -1;
                    if (ServerConfig.debug.get())
                    {
                        logger.info("{} using legacy networking", listener.player.getName().getString());
                    }
                }

                vivePlayer.setVR(!clientVivecraftVersion.contains("NONVR"));

                ServerVRPlayers.getPlayersWithVivecraft(listener.player.server).put(playerEntity.getUUID(), vivePlayer);

                listener.send(getVivecraftServerPacket(PacketDiscriminators.VERSION, CommonDataHolder.getInstance().versionIdentifier));
                listener.send(getVivecraftServerPacket(PacketDiscriminators.REQUESTDATA, new byte[0]));

                if (ServerConfig.climbeyEnabled.get())
                {
                    listener.send(getClimbeyServerPacket());
                }

                if (ServerConfig.teleportEnabled.get())
                {
                    listener.send(getVivecraftServerPacket(PacketDiscriminators.TELEPORT, new byte[0]));
                }
                if (ServerConfig.teleportLimitedSurvival.get())
                {
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeUtf("limitedTeleport");
                    byteBuf.writeUtf(String.valueOf(true));

                    byteBuf.writeUtf("teleportLimitUp");
                    byteBuf.writeUtf(String.valueOf(ServerConfig.teleportUpLimit.get()));

                    byteBuf.writeUtf("teleportLimitDown");
                    byteBuf.writeUtf(String.valueOf(ServerConfig.teleportDownLimit.get()));

                    byteBuf.writeUtf("teleportLimitHoriz");
                    byteBuf.writeUtf(String.valueOf(ServerConfig.teleportHorizontalLimit.get()));

                    listener.send(getVivecraftServerPacket(PacketDiscriminators.SETTING_OVERRIDE, byteBuf.readByteArray()));
                }

                if (ServerConfig.worldscaleLimited.get())
                {
                    FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
                    byteBuf.writeUtf("worldScale.min");
                    byteBuf.writeUtf(String.valueOf(ServerConfig.worldscaleMin.get()));

                    byteBuf.writeUtf("worldScale.max");
                    byteBuf.writeUtf(String.valueOf(ServerConfig.worldscaleMax.get()));

                    listener.send(getVivecraftServerPacket(PacketDiscriminators.SETTING_OVERRIDE, byteBuf.readByteArray()));
                }

                if (ServerConfig.crawlingEnabled.get())
                {
                    listener.send(getVivecraftServerPacket(PacketDiscriminators.CRAWL, new byte[0]));
                }

                // send if hotswitching is allowed
                listener.send(getVivecraftServerPacket(PacketDiscriminators.VR_SWITCHING, new byte[]{(byte)(ServerConfig.vrSwitchingEnabled.get() && !ServerConfig.vr_only.get() ? 1 : 0)}));

                listener.send(getVivecraftServerPacket(PacketDiscriminators.NETWORK_VERSION, new byte[]{(byte)vivePlayer.networkVersion}));

                break;
            }
            case IS_VR_ACTIVE ->
            {
                if (vivePlayer.isVR() != buffer.readBoolean())
                {
                    vivePlayer.setVR(!vivePlayer.isVR());
                    if (!vivePlayer.isVR())
                    {
                        for (ServerPlayerConnection trackingPlayer : getTrackingPlayers(playerEntity))
                        {
                            if (!ServerVRPlayers.getPlayersWithVivecraft(listener.player.server).containsKey(trackingPlayer.getPlayer().getUUID()) || trackingPlayer.getPlayer() == playerEntity)
                            {
                                continue;
                            }
                            trackingPlayer.send(createVRActivePlayerPacket(false, playerEntity.getUUID()));
                        }
                    }
                }
            }

            case DRAW -> vivePlayer.draw = buffer.readFloat();

            case VR_PLAYER_STATE -> vivePlayer.vrPlayerState = VRPlayerState.deserialize(buffer);

            case WORLDSCALE -> vivePlayer.worldScale = buffer.readFloat();
            case HEIGHT -> vivePlayer.heightScale = buffer.readFloat();
            case TELEPORT ->
            {
                float f = buffer.readFloat();
                float f1 = buffer.readFloat();
                float f2 = buffer.readFloat();
                playerEntity.absMoveTo(f, f1, f2, playerEntity.getYRot(), playerEntity.getXRot());
            }
            case CLIMBING -> playerEntity.fallDistance = 0.0F;
            case ACTIVEHAND ->
            {
                vivePlayer.activeHand = buffer.readByte();

                if (vivePlayer.isSeated())
                {
                    vivePlayer.activeHand = 0;
                }
            }

            case CRAWL->
            {
                vivePlayer.crawling = buffer.readByte() != 0;

                if (vivePlayer.crawling)
                {
                    playerEntity.setPose(Pose.SWIMMING);
                }
            }
            // legacy support
            case CONTROLLER0DATA, CONTROLLER1DATA, HEADDATA ->
            {
                Map<PacketDiscriminators, FriendlyByteBuf> playerData;
                if ((playerData = legacyDataMap.get(playerEntity.getUUID())) == null)
                {
                    playerData = new HashMap<>();
                    legacyDataMap.put(playerEntity.getUUID(), playerData);
                }
                // keep the buffer around
                buffer.retain();
                playerData.put(packetID, buffer);

                if (playerData.size() == 3)
                {
                    FriendlyByteBuf controller0Data = playerData.get(PacketDiscriminators.CONTROLLER0DATA);
                    controller0Data.resetReaderIndex().readByte();
                    FriendlyByteBuf controller1Data = playerData.get(PacketDiscriminators.CONTROLLER1DATA);
                    controller1Data.resetReaderIndex().readByte();
                    FriendlyByteBuf headData = playerData.get(PacketDiscriminators.HEADDATA);
                    headData.resetReaderIndex().readByte();

                    vivePlayer.vrPlayerState = new VRPlayerState(
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
                    legacyDataMap.remove(playerEntity.getUUID());
                }
            }
        }
    }

    public static Set<ServerPlayerConnection> getTrackingPlayers(Entity entity) {
        var manager = entity.level().getChunkSource();
        var storage = ((ServerChunkCache) manager).chunkMap;
        var playerTracker = ((ChunkMapAccessor) storage).getTrackedEntities().get(entity.getId());
        return playerTracker != null ? playerTracker.getPlayersTracking() : Collections.emptySet();
    }

    public static ClientboundCustomPayloadPacket createVRActivePlayerPacket(boolean vrActive, UUID playerID) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(PacketDiscriminators.IS_VR_ACTIVE.ordinal());
        buffer.writeBoolean(vrActive);
        buffer.writeUUID(playerID);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, buffer);
    }

    public static ClientboundCustomPayloadPacket createUberPacket(Player player, VRPlayerState vrPlayerState, float worldScale, float heightScale) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(PacketDiscriminators.UBERPACKET.ordinal());
        buffer.writeUUID(player.getUUID());
        vrPlayerState.serialize(buffer);
        buffer.writeFloat(worldScale);
        buffer.writeFloat(heightScale);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, buffer);
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(PacketDiscriminators command, byte[] payload) {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.CHANNEL, friendlybytebuf);
    }

    public static ClientboundCustomPayloadPacket getClimbeyServerPacket() {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(PacketDiscriminators.CLIMBING.ordinal());
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

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(PacketDiscriminators command, String payload) {
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
