package org.vivecraft.api.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.VRAPI;

import javax.annotation.Nullable;

/**
 * A tracker is an object that is run for the local player during the game tick or before rendering a frame only if
 * they are in VR. Using trackers is one of the cleanest ways to interact with Vivecraft's data, it's how Vivecraft
 * itself does. Trackers should generally use {@link VRClientAPI#getPreTickWorldPose()} when using the
 * {@link ProcessType#PER_TICK} process type and {@link VRClientAPI#getWorldRenderPose()} when using the
 * {@link ProcessType#PER_FRAME} process type, as this provides the most up-to-date data and relevant data. Furthermore,
 * other methods such as {@link VRClientAPI#getPostTickWorldPose()} or {@link VRAPI#getVRPose(Player)} may not have
 * data available when the tracker is run.
 *
 * @since 1.3.0
 */
public interface Tracker {

    /**
     * The process type for this tracker. Determines when and how frequently it is Processed. See {@link ProcessType} for
     * possible options.
     *
     * @return The process type this tracker should use.
     * @since 1.3.0
     */
    ProcessType processType();

    /**
     * Whether the tracker is active for the local player.
     *
     * @param player Player being checked if they are active for this tracker instance. Will be {@code null} when not in a world.
     * @return true if the tracker is active for the specified player.
     * @since 1.3.0
     */
    boolean isActive(@Nullable LocalPlayer player);

    /**
     * Called for the local player, whether the tracker is active or not. This is called before
     * {@link #activeProcess(LocalPlayer)}, {@link #inactiveProcess(LocalPlayer)}, and
     * {@link #isActive(LocalPlayer)} are called.
     *
     * @param player Player to do an idle process for. Will be {@code null} when not in a world.
     * @since 1.3.0
     */
    default void idleProcess(@Nullable LocalPlayer player) {}

    /**
     * Called for the local player if this tracker is active, which is when {@link #isActive(LocalPlayer)} returns true.
     *
     * @param player Player to run this tracker for, which is the local player. Will be {@code null} when not in a world. Only {@code null} if {@link #isActive(LocalPlayer)} also got {@code null}.
     * @since 1.3.0
     */
    void activeProcess(@Nullable LocalPlayer player);

    /**
     * Called to reset this tracker's state. This is called whenever {@link #isActive(LocalPlayer)} returns false.
     *
     * @param player The local player. Will be {@code null} when not in a world. Only {@code null} if {@link #isActive(LocalPlayer)} also got {@code null}.
     * @since 1.3.0
     */
    default void inactiveProcess(@Nullable LocalPlayer player) {}

    /**
     * The process type used for processing trackers.
     *
     * @since 1.3.0
     */
    enum ProcessType {
        /**
         * processed every frame, before rendering starts
         */
        PER_FRAME,
        /**
         * processed every tick, during the local player tick
         */
        PER_TICK
    }
}
