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
    // If updated, should also update Javadocs in VRClientAPI and VRAPI
    public static final int MAX_CONFIGURABLE_HISTORY_TICKS = 200;

    private final Map<UUID, VRPoseHistoryImpl> clientPoseHistories = new HashMap<>();
    private final Map<UUID, VRPoseHistoryImpl> serverPoseHistories = new HashMap<>();
    private int maxOtherPoseHistorySize = 0;

    private VRAPIImpl() {
    }

    public void clearPoseHistory(UUID player, boolean isClientSide) {
        this.getMap(isClientSide).remove(player);
    }

    public void addPoseToHistory(UUID player, VRPose pose, boolean isClientSide) {
        Map<UUID, VRPoseHistoryImpl> poseHistories = this.getMap(isClientSide);
        VRPoseHistoryImpl poseHistory = poseHistories.get(player);
        if (poseHistory == null) {
            poseHistory = new VRPoseHistoryImpl(false);
            poseHistories.put(player, poseHistory);
        }
        poseHistory.addPose(pose);
    }

    public void clearAllPoseHistories() {
        clientPoseHistories.clear();
        serverPoseHistories.clear();
    }

    public int maxOtherPoseHistorySize() {
        return this.maxOtherPoseHistorySize;
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
    public void requestTicksOfHistory(int maxTicksBack) throws IllegalArgumentException {
        if (maxTicksBack <= 0) {
            throw new IllegalArgumentException("Must call requestTicksOfHistory() with a positive number.");
        }
        this.maxOtherPoseHistorySize = Math.max(this.maxOtherPoseHistorySize,
            Math.min(maxTicksBack, VRAPIImpl.MAX_CONFIGURABLE_HISTORY_TICKS));
    }

    @Override
    @Nullable
    public VRPoseHistory getHistoricalVRPoses(Player player) {
        if (player.level().isClientSide && player.isLocalPlayer()) {
            return VRClientAPIImpl.INSTANCE.getHistoricalVRPoses();
        } else {
            return getMap(player.level().isClientSide).get(player.getUUID());
        }
    }

    private Map<UUID, VRPoseHistoryImpl> getMap(boolean isClientSide) {
        return isClientSide ? clientPoseHistories : serverPoseHistories;
    }
}
