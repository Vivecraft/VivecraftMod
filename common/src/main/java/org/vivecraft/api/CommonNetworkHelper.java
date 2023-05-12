package org.vivecraft.api;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.common.network.ServerNetworking;
import org.vivecraft.common.utils.math.Quaternion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommonNetworkHelper {

    public static Map<UUID, ServerVivePlayer> playersWithVivecraft = new HashMap<>();
    public static final ResourceLocation channel = new ResourceLocation("vivecraft:data");

    public static boolean isVRPlayer(ServerPlayer p) {
        if (p == null) {
            return false;
        } else {
            return playersWithVivecraft.containsKey(p.getGameProfile().getId()) && playersWithVivecraft.get(p.getGameProfile().getId()).isVR();
        }
    }

    public static void sendVrPlayerStateToClients(ServerPlayer vrPlayerEntity) {
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
        for (var trackingPlayer : ServerNetworking.getTrackingPlayers(vrPlayerEntity)) {
            if (!playersWithVivecraft.containsKey(trackingPlayer.getPlayer().getUUID()) || trackingPlayer.getPlayer() == vrPlayerEntity) {
                continue;
            }
            trackingPlayer.send(ServerNetworking.createUberPacket(vivePlayer.player, vivePlayer.vrPlayerState, vivePlayer.worldScale, vivePlayer.heightScale));
        }
    }

    public static void overridePose(ServerPlayer player) {
        ServerVivePlayer serverviveplayer = playersWithVivecraft.get(player.getGameProfile().getId());

        if (serverviveplayer != null && serverviveplayer.isVR() && serverviveplayer.crawling) {
            player.setPose(Pose.SWIMMING);
        }
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, byte[] payload) {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeBytes(payload);
        return new ClientboundCustomPayloadPacket(channel, friendlybytebuf);
    }

    public static ClientboundCustomPayloadPacket getVivecraftServerPacket(CommonNetworkHelper.PacketDiscriminators command, String payload) {
        FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlybytebuf.writeByte(command.ordinal());
        friendlybytebuf.writeUtf(payload);
        return new ClientboundCustomPayloadPacket(channel, friendlybytebuf);
    }

    public enum PacketDiscriminators {
        VERSION,
        IS_VR_ACTIVE,
        REQUESTDATA,
        VR_PLAYER_STATE,
        WORLDSCALE,
        DRAW,
        MOVEMODE,
        UBERPACKET,
        TELEPORT,
        CLIMBING,
        SETTING_OVERRIDE,
        HEIGHT,
        ACTIVEHAND,
        CRAWL
    }

    public static void serializeF(FriendlyByteBuf buffer, Vec3 vec3) {
        buffer.writeFloat((float) vec3.x);
        buffer.writeFloat((float) vec3.y);
        buffer.writeFloat((float) vec3.z);
    }

    public static void serialize(FriendlyByteBuf buffer, Quaternion quat) {
        buffer.writeFloat(quat.w);
        buffer.writeFloat(quat.x);
        buffer.writeFloat(quat.y);
        buffer.writeFloat(quat.z);
    }

    public static Vec3 deserializeFVec3(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static Quaternion deserializeVivecraftQuaternion(FriendlyByteBuf buffer) {
        return new Quaternion(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }
}
