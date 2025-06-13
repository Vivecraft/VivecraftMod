package org.vivecraft.api;

import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.data.VRPoseHistory;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.common.api_impl.VRAPIImpl;

import javax.annotation.Nullable;

/**
 * The main interface for interacting with Vivecraft from common code.
 */
public interface VRAPI {

    /**
     * @return The Vivecraft API instance for interacting with Vivecraft's common API.
     */
    static VRAPI instance() {
        return VRAPIImpl.INSTANCE;
    }

    /**
     * Check whether a given player is currently in VR.
     *
     * @param player The player to check the VR status of.
     * @return true if the player is in VR.
     */
    boolean isVRPlayer(Player player);

    /**
     * Returns the VR pose for the given player. Will return null if the player isn't in VR,
     * or if being called from the client and the client has yet to receive any data for the player.
     *
     * @param player Player to get the VR pose of.
     * @return The VR pose for a player, or null if the player isn't in VR or no data has been received for said player.
     */
    @Nullable
    VRPose getVRPose(Player player);

    /**
     * Requests the number of ticks of history wanted for {@link #getHistoricalVRPoses(Player)}. Any value larger than 200
     * will be capped at 200.
     *
     * @param maxTicksBack The maximum number of ticks of history wanted.
     * @throws IllegalArgumentException If a non-positive number is supplied.
     */
    void requestTicksOfHistory(int maxTicksBack) throws IllegalArgumentException;

    /**
     * Returns the history of VR poses for the player. One should make one call to {@link #requestTicksOfHistory(int)}
     * before calling this method to inform Vivecraft of the amount of history to keep.
     * <br>
     * This method acts differently depending on the side and player requested:
     * <ul>
     *     <li>On the server, this will return the history for the provided player for the ticks requested by
     *     {@link #requestTicksOfHistory(int)}</li>
     *     <li>On the client, if requested a player other than the local player, this will return the history for the
     *     provided player for the ticks requested by {@link #requestTicksOfHistory(int)}</li>
     *     <li>On the client, if requested by a player that IS the local player, this will return the history for the
     *     local player for the ticks requested by
     *     {@link org.vivecraft.api.client.VRClientAPI#requestTicksOfHistory(int)}. One can use
     *     {@link org.vivecraft.api.client.VRClientAPI#getHistoricalVRPoses()} to retrieve that same data.</li>
     * </ul>
     *
     * @return The history of VR poses for the player. Will be null if the player isn't in VR or if
     * {@link #requestTicksOfHistory(int)} has yet to be called.
     */
    @Nullable
    VRPoseHistory getHistoricalVRPoses(Player player);
}
