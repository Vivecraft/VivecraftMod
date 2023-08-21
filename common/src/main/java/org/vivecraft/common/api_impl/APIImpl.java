package org.vivecraft.common.api_impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api_beta.VivecraftAPI;
import org.vivecraft.api_beta.data.VRData;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.server.ServerVRPlayers;

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

    @Nullable
    @Override
    public VRData getVRData(Player player) {
        if (!isVRPlayer(player)) {
            return null;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return ServerVRPlayers.getVivePlayer(serverPlayer).asVRData();
        }

        return VRPlayersClient.getInstance().getRotationsForPlayer(player.getUUID()).asVRData(player.position());
    }
}
