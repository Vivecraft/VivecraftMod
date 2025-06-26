package org.vivecraft.api.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.VRAPI;

import javax.annotation.Nullable;

/**
 * A tracker is an object that is run for the local player during the game tick or before rendering a frame only if
 * they are in VR. Using trackers is one of the cleanest ways to interact with Vivecraft's data, it's how Vivecraft
 * itself does. Trackers should generally use {@link VRClientAPI#getPreTickWorldPose()}, as this provides
 * the most up-to-date data, and other methods such as {@link VRClientAPI#getPostTickWorldPose()} or
 * {@link VRAPI#getVRPose(Player)} may not have data available when the tracker is run.
 *
 * @since 1.3.0
 */
public interface Tracker {

    /**
     * Whether the tracker is active for the local player.
     *
     * @param player Player being checked if they are active for this tracker instance. Will be {@code null} when not in a world.
     * @return true if the tracker is active for the specified player.
     * @since 1.3.0
     */
    boolean isActive(@Nullable LocalPlayer player);

    /**
     * Called for the client player if this tracker is active, which is when {@link #isActive(LocalPlayer)} returns true.
     *
     * @param player Player to run this tracker for, which is the local player. Will be {@code null} when not in a world. Only {@code null} if {@link #isActive(LocalPlayer)} also got {@code null}.
     * @since 1.3.0
     */
    void doProcess(@Nullable LocalPlayer player);

    /**
     * The ticking type for this tracker.
     * <br>
     * If this is {@link TrackerTickType#PER_FRAME}, the tracker is called once with the local player per frame before the frame is rendered.
     * <br>
     * If this is {@link TrackerTickType#PER_TICK}, the tracker is called once with the local player per game tick during the tick.
     *
     * @return The ticking type this tracker should use.
     * @since 1.3.0
     */
    TrackerTickType tickType();

    /**
     * Called to reset this tracker's state. This is called whenever {@link #isActive(LocalPlayer)} returns false.
     *
     * @param player The local player. Will be {@code null} when not in a world. Only {@code null} if {@link #isActive(LocalPlayer)} also got {@code null}.
     * @since 1.3.0
     */
    default void reset(@Nullable LocalPlayer player) {}

    /**
     * Called for the local player, whether the tracker is active or not for them. This runs before
     * {@link #isActive(LocalPlayer)} or {@link #reset(LocalPlayer)}.
     *
     * @param player Player to do an idle tick for. Will be {@code null} when not in a world.
     * @since 1.3.0
     */
    default void idleTick(@Nullable LocalPlayer player) {}

    /**
     * The timing type used for ticking trackers.
     *
     * @since 1.3.0
     */
    enum TrackerTickType {
        PER_FRAME, PER_TICK
    }
}
