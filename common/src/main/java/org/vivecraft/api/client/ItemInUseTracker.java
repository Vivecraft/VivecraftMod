package org.vivecraft.api.client;

import net.minecraft.client.player.LocalPlayer;

/**
 * A type of {@link Tracker} which can prevent the vanilla use key from being released, with {@link #itemInUse(LocalPlayer)}.
 *
 * @since 1.3.0
 */
public interface ItemInUseTracker extends Tracker {

    /**
     * Called for the client player, to check if this tracker is currently causing the item to be used to not release
     * the use key. In other words, if you want the item currently being held to act as the use key being held, one
     * should call the use item function, then return true from this method while the item should still remain used.
     *
     * @param player The local player which is running this tracker.
     * @return Whether the item should remain in use.
     * @since 1.3.0
     */
    boolean itemInUse(LocalPlayer player);
}
