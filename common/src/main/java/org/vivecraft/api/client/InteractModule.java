package org.vivecraft.api.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * An InteractModule can influence what happens when the user presses the Interact keybind.
 * The Interact keybind is added by Vivecraft and by default bound to the Trigger and Grip of each controller.
 * InteractModules are hand agnostic and are processed on tick.
 * <br>
 * They are sorted by the priority value provided by {@link #getPriority} and their ID on a priority tie.
 * Modules are processed in that fixed sorted order per hand and the first one that returns {@code true} on
 * {@link #isActive} gets the keybind for that hand.
 * {@link #isActive} on modules after the active one will <strong>not</strong> be called for that hand.
 *
 * @since 1.3.0
 */
public interface InteractModule {

    /**
     * The priority value of a module determines when its {@link #isActive} method is called compared to other modules.
     * Modules are only processed until a modules returns {@code true} on {@link #isActive}, modules after that are not checked.
     * A lower priority value means it is processed earlier.
     * Can be any Integer, negative values are also allowed. Vivecraft itself uses values ranging from 0-1500.
     *
     * @return priority value of this module
     * @since 1.3.0
     */
    default int getPriority() {
        return 1000;
    }

    /**
     * The ID of this module. This is used to sort on a priority tie, should ideally be of the format "modID":"moduleName".
     *
     * @return The ID of this module.
     * @since 1.3.0
     */
    ResourceLocation getId();

    /**
     * Used to reset the module state for the given hand, this is called when the Interact Tracker is not active anymore,
     * and before {@link #isActive} is called
     *
     * @param player the local player, {@code null} if not in a world
     * @param hand   the hand to reset
     * @since 1.3.0
     */
    default void reset(@Nullable LocalPlayer player, InteractionHand hand) {}

    /**
     * This is used to check if the user can use the Interact keybind on the given {@code hand} to interact with the module.
     * <br>
     * If this returns {@code true} and no module was active in the last tick, it will cause haptic feedback on the {@code hand} to indicate that the user can now use the Interact keybind.
     *
     * @param player       the local player
     * @param hand         the hand to check for
     * @param handPosition the world position the {@code hand} is at, supplied for convenience
     * @return true if this module is active and wants to use the Interact keybind
     * @since 1.3.0
     */
    boolean isActive(LocalPlayer player, InteractionHand hand, Vec3 handPosition);

    /**
     * Use this to do an action when the Interact keybind is being pressed.
     * <br>
     * This is only called when {@link #isActive} returned {@code true} and no other module did so before this one.
     *
     * @param player the local player
     * @param hand   the hand that is pressing the Interact keybind
     * @return if the interaction was successful, will cause haptic feedback when {@code true}
     * @since 1.3.0
     */
    boolean onPress(LocalPlayer player, InteractionHand hand);

    /**
     * By default, an interaction causes an armswing, to give the player a visual indicator that the action was successful.
     * This can be overridden to prevent that.
     *
     * @return if the interaction should cause a hand swing after a successful {@link #onPress} call
     * @since 1.3.0
     */
    default boolean swingsArm() {
        return true;
    }
}
