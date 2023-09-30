package org.vivecraft.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;

import java.util.Map;
import java.util.UUID;

public class ServerVRPlayers {
    public static ServerVivePlayer getVivePlayer(ServerPlayer player) {
        return getPlayersWithVivecraft(player.server).get(player.getUUID());
    }

    public static boolean isVRPlayer(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        ServerVivePlayer serverviveplayer = getVivePlayer(player);
        if (serverviveplayer == null) {
            return false;
        }
        return serverviveplayer.isVR();
    }

    public static void overridePose(ServerPlayer player) {
        ServerVivePlayer serverviveplayer = getVivePlayer(player);

        if (serverviveplayer != null && serverviveplayer.isVR() && serverviveplayer.crawling) {
            player.setPose(Pose.SWIMMING);
        }
    }

    public static Map<UUID, ServerVivePlayer> getPlayersWithVivecraft(MinecraftServer server) {
        return ((MinecraftServerExt) server).vivecraft$getPlayersWithVivecraft();
    }
}
