package org.vivecraft.common.api_impl;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.api_impl.VRClientAPIImpl;
import org.vivecraft.common.api_impl.data.VRPoseHistoryImpl;
import org.vivecraft.server.ServerVRPlayers;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VRAPIImpl implements VRAPI {

    public static final VRAPIImpl INSTANCE = new VRAPIImpl();
    public static final int MAX_HISTORY_TICKS = 200;

    private final Map<UUID, VRPoseHistoryImpl> clientPoseHistories = new HashMap<>();
    private final Map<UUID, VRPoseHistoryImpl> serverPoseHistories = new HashMap<>();

    private VRAPIImpl() {
    }

    public void clearPoseHistory(UUID player, boolean isClientSide) {
        this.getMap(isClientSide).remove(player);
    }

    public void addPoseToHistory(UUID player, VRPose pose, boolean isClientSide) {
        Map<UUID, VRPoseHistoryImpl> poseHistories = this.getMap(isClientSide);
        VRPoseHistoryImpl poseHistory = poseHistories.get(player);
        if (poseHistory == null) {
            poseHistory = new VRPoseHistoryImpl();
            poseHistories.put(player, poseHistory);
        }
        poseHistory.addPose(pose);
    }

    public void clearAllPoseHistories() {
        this.clientPoseHistories.clear();
        this.serverPoseHistories.clear();
    }

    @Override
    public boolean isVRPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return ServerVRPlayers.isVRPlayer(serverPlayer);
        } else {
            return ClientVRPlayers.getInstance().isVRPlayer(player);
        }
    }

    @Nullable
    @Override
    public VRPose getVRPose(Player player) {
        if (!isVRPlayer(player)) {
            return null;
        } else if (player instanceof ServerPlayer serverPlayer) {
            return ServerVRPlayers.getVivePlayer(serverPlayer).asVRPose();
        } else {
            return ClientVRPlayers.getInstance().getRotationsForPlayer(player.getUUID()).asVRPose(player.position());
        }
    }

    @Override
    @Nullable
    public VRPoseHistory getHistoricalVRPoses(Player player) {
        if (player.isLocalPlayer()) {
            return VRClientAPIImpl.INSTANCE.getHistoricalVRPoses();
        } else if (isVRPlayer(player)) {
            return getMap(player.level().isClientSide).get(player.getUUID());
        } else {
            return null;
        }
    }

    private Map<UUID, VRPoseHistoryImpl> getMap(boolean isClientSide) {
        return isClientSide ? this.clientPoseHistories : this.serverPoseHistories;
    }
}
