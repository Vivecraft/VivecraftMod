package org.vivecraft.common.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;
import org.vivecraft.common.CommonDataHolder;
import org.vivecraft.mixin.server.ChunkMapAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.vivecraft.api.CommonNetworkHelper.playersWithVivecraft;

public class ServerNetworking {

    // temporarily stores the packets from legacy clients to assemble a complete VrPlayerState
    private final static Map<UUID, Map<CommonNetworkHelper.PacketDiscriminators,FriendlyByteBuf>> legacyDataMap= new HashMap<>();

    public static void handlePacket(CommonNetworkHelper.PacketDiscriminators packetID, FriendlyByteBuf buffer, ServerGamePacketListenerImpl listener) {
        var playerEntity = listener.player;
        ServerVivePlayer vivePlayer = playersWithVivecraft.get(playerEntity.getUUID());

        if (vivePlayer == null && packetID != CommonNetworkHelper.PacketDiscriminators.VERSION) {
            return;
        }

        switch (packetID) {
            case VERSION:
                listener.send(CommonNetworkHelper.getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.VERSION, CommonDataHolder.getInstance().versionIdentifier));
                listener.send(CommonNetworkHelper.getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.REQUESTDATA, new byte[0]));
                listener.send(CommonNetworkHelper.getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.CLIMBING, new byte[]{1, 0}));
                listener.send(CommonNetworkHelper.getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.TELEPORT, new byte[0]));
                listener.send(CommonNetworkHelper.getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.CRAWL, new byte[0]));
                listener.send(CommonNetworkHelper.getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.VR_SWITCHING, new byte[0]));
                listener.send(CommonNetworkHelper.getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators.NEW_NETWORKING, new byte[0]));

                vivePlayer = new ServerVivePlayer(playerEntity);
                playersWithVivecraft.put(playerEntity.getUUID(), vivePlayer);

                break;
            case IS_VR_ACTIVE:
                if (vivePlayer.isVR() == buffer.readBoolean()) {
                    break;
                }
                vivePlayer.setVR(!vivePlayer.isVR());
                if (!vivePlayer.isVR()) {
                    for (var trackingPlayer : ServerNetworking.getTrackingPlayers(playerEntity)) {
                        if (!playersWithVivecraft.containsKey(trackingPlayer.getPlayer().getUUID()) || trackingPlayer.getPlayer() == playerEntity) {
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
                Map<CommonNetworkHelper.PacketDiscriminators,FriendlyByteBuf> playerData;
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
        return playerTracker.getPlayersTracking();
    }

    public static ClientboundCustomPayloadPacket createVRActivePlayerPacket(boolean vrActive, UUID playerID) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.IS_VR_ACTIVE.ordinal());
        buffer.writeBoolean(vrActive);
        buffer.writeUUID(playerID);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.channel, buffer);
    }

    public static ClientboundCustomPayloadPacket createUberPacket(Player player, VrPlayerState vrPlayerState, float worldScale, float heightScale) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(CommonNetworkHelper.PacketDiscriminators.UBERPACKET.ordinal());
        buffer.writeUUID(player.getUUID());
        vrPlayerState.serialize(buffer);
        buffer.writeFloat(worldScale);
        buffer.writeFloat(heightScale);
        return new ClientboundCustomPayloadPacket(CommonNetworkHelper.channel, buffer);
    }
}
