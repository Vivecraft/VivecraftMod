package org.vivecraft.api.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;

/**
 * Compared to a regular {@link InteractModule}, a HeldInteractModule blocks other modules from using the
 * Interact keybind until it is released/not used anymore.
 * <br>
 * A HeldInteractModule can be used to drag stuff around. Vivecraft itself uses it to trigger the camera grabbing and
 * bow drawing.
 *
 * @since 1.3.0
 */
public interface HeldInteractModule extends InteractModule {

    /**
     * Called on tick while this module is active and the Interact keybind is still pressed. Useful to process the
     * module state, or cancel early.
     *
     * @param player the local player
     * @param hand   the hand that is holding the Interact keybind
     * @return {@code true} if this module is still active or {@code false} if the Interact keybind should be released early
     * @since 1.3.0
     */
    default boolean onHoldTick(LocalPlayer player, InteractionHand hand) {
        return true;
    }

    /**
     * Counterpart to {@link InteractModule#onPress}
     * <br>
     * This is called when the module was active, and the Interact keybind released or {@link #onHoldTick} returned {@code false}.
     *
     * @param player the local player, {@code null} if not in a world
     * @param hand   the hand that released the Interact keybind
     * @since 1.3.0
     */
    void onRelease(@Nullable LocalPlayer player, InteractionHand hand);
}
