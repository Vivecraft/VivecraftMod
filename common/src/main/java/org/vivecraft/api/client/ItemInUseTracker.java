package org.vivecraft.api.client;

import net.minecraft.client.player.LocalPlayer;

/**
 * An interface that should be implemented by {@link Tracker}s if they want to take advantage of
 * {@link #itemInUse(LocalPlayer)}.
 */
public interface ItemInUseTracker {

    /**
     * Called for the client player, to check if this tracker is currently causing the item to be used to not release
     * the use key. In other words, if you want the item currently being held to act as the use key being held, one
     * should call the use item function, then return true from this method while the item should still remain used.
     *
     * @param player The local player which is running this tracker.
     * @return Whether the item should remain in use.
     */
    boolean itemInUse(LocalPlayer player);
}
