package org.vivecraft.api_beta.client;

import net.minecraft.client.player.LocalPlayer;

public interface Tracker {

    /**
     * Whether the tracker is active for the local player.
     * @param player Player being checked if they are active for this tracker instances.
     * @return true if the tracker is active for the specified player.
     */
    boolean isActive(LocalPlayer player);

    /**
     * Called for the client player if this tracker is active, which is when {@link #isActive(LocalPlayer)} returns true.
     * @param player Player to run this tracker for, which is the local player.
     */
    void doProcess(LocalPlayer player);

    /**
     * The ticking type for this tracker.
     * If this is PER_FRAME, the tracker is called once with the local player per frame.
     * If this is PER_TICK, the tracker is called once with the local player per game tick.
     * @return The ticking type this tracker should use.
     */
    TrackerTickType tickType();

    /**
     * Called to reset data for the local player. This is called whenever {@link #isActive(LocalPlayer)} returns false.
     * @param player The local player, which will have their data reset.
     */
    default void reset(LocalPlayer player) {

    }

    /**
     * Called for all players, whether the tracker is active or not for them. This runs before
     * {@link #isActive(LocalPlayer)} or {@link #reset(LocalPlayer)}.
     * @param player Player to do an idle tick for, which is the local player.
     */
    default void idleTick(LocalPlayer player) {

    }
}
