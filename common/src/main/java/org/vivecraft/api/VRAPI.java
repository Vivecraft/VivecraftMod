package org.vivecraft.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;
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
     * <br>
     * Note that due to the inherent latency of networking, historical VR data for the first two options above
     * (the server requesting the history for any player or the client requesting history for a player other than the
     * local player) may be unideal.
     * {@link org.vivecraft.api.client.VRClientAPI#getHistoricalVRPoses()} is preferred.
     *
     * @return The history of VR poses for the player. Will be null if the player isn't in VR or if
     * {@link #requestTicksOfHistory(int)} has yet to be called.
     */
    @Nullable
    VRPoseHistory getHistoricalVRPoses(Player player);

    /**
     * Sends a haptic pulse (vibration/rumble) for the specified VRBodyPart, if possible, to the given player.
     * To directly trigger a haptic pulse for the local player, use {@link VRClientAPI#triggerHapticPulse}
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param player    Player to send the haptic pulse to.
     * @param bodyPart  The VRBodyPart to trigger a haptic pulse on.
     * @param duration  The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                  underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                  very short pulses.
     * @param frequency The frequency of the haptic pulse in Hz. (might be ignored if the targeted device doesn't support it)
     *                  <br>
     *                  160 Hz is a safe bet for this number, with Vivecraft's codebase
     *                  using anywhere from 160 Hz for actions such as a bite on a fishing line, to 1000 Hz for things such
     *                  as a chat notification.
     * @param amplitude The amplitude of the haptic pulse. This should be kept between 0 and 1.
     * @param delay     An amount of time to delay until creating the haptic pulse. The majority of the time, one should use 0 here. This starts counting when the client receives the packet.
     */
    void triggerHapticPulse(
        ServerPlayer player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay);

    /**
     * Sends a haptic pulse (vibration/rumble) at full strength with 160 Hz  for the specified VRBodyPart, if possible, to the given player.
     * <br>
     * If one wants more control over the used parameters one should use {@link #triggerHapticPulse(ServerPlayer, VRBodyPart, float, float, float, float)} instead.
     * <br>
     * To directly trigger a haptic pulse for the local player, use {@link VRClientAPI#triggerHapticPulse}
     * This function silently fails if called for players not in VR or players who are in seated mode.
     *
     * @param player   Player to send the haptic pulse to.
     * @param bodyPart The VRBodyPart to trigger a haptic pulse on.
     * @param duration The duration of the haptic pulse in seconds. Note that this number is passed to the
     *                 underlying VR API used by Vivecraft, and may act with a shorter length than expected beyond
     *                 very short pulses.
     */
    default void triggerHapticPulse(ServerPlayer player, VRBodyPart bodyPart, float duration) {
        triggerHapticPulse(player, bodyPart, duration, 160F, 1F, 0F);
    }
}
