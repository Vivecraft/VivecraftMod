package org.vivecraft.common;

import org.vivecraft.api_beta.VivecraftAPI;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.server.ServerVRPlayers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class APIImpl implements VivecraftAPI {

    public static final APIImpl INSTANCE = new APIImpl();

    private APIImpl() {
    }

    @Override
    public boolean isVRPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return ServerVRPlayers.isVRPlayer(serverPlayer);
        }

        return VRPlayersClient.getInstance().isVRPlayer(player);
    }
}
