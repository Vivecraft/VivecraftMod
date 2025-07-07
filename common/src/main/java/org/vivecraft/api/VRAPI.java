package org.vivecraft.api;

import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.common.api_impl.VRAPIImpl;

import javax.annotation.Nullable;

/**
 * The main interface for interacting with Vivecraft from common code.
 *
 * @since 1.3.0
 */
public interface VRAPI {

    /**
     * Gets API instance for interacting with Vivecraft's common API
     *
     * @return The Vivecraft API instance for interacting with Vivecraft's common API.
     * @since 1.3.0
     */
    static VRAPI instance() {
        return VRAPIImpl.INSTANCE;
    }

    /**
     * Check whether a given player is currently in VR.
     *
     * @param player The player to check the VR status of.
     * @return true if the player is in VR.
     * @since 1.3.0
     */
    boolean isVRPlayer(Player player);

    /**
     * Returns the VR pose for the given player. Will return {@code null} if the player isn't in VR,
     * or if being called from the client and the client has yet to receive any data for the player.
     *
     * @param player Player to get the VR pose of.
     * @return The VR pose for a player, or {@code null} if the player isn't in VR or no data has been received for said player.
     * @since 1.3.0
     */
    @Nullable
    VRPose getVRPose(Player player);

    /**
     * Returns the history of VR poses for the player. If one only needs the history for the local player, this can be
     * more conveniently called using {@link org.vivecraft.api.client.VRClientAPI#getHistoricalVRPoses()}.
     * <br>
     * Note that due to the inherent latency of networking, historical VR data retrieved either by the server or by
     * the client for a client other than the local player may be unideal.
     *
     * @param player Player to get the VR pose history of.
     * @return The history of VR poses for the player. Will be {@code null} if the player isn't in VR or if VR-specific data
     * hasn't been received.
     * @since 1.3.0
     */
    @Nullable
    VRPoseHistory getHistoricalVRPoses(Player player);
}
