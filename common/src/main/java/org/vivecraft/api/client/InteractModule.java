package org.vivecraft.api.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * An InteractModule can influence what happens when the user presses the Interact keybind.
 * InteractModules are hand agnostic and are processed on tick.<br>
 * They are sorted by the priority provided by {@link #getPriority} and processed in that order.<br>
 * The first InteractModule in that order, that returns {@code true} on {@link #doProcess} will be the active InteractModule for this tick on the hand.
 */
public interface InteractModule extends Comparable<InteractModule> {

    /**
     * The priority of a module indicates when it is processed, this can be used to order modules to be processed in a specific order. A lower priority value means it is processed earlier.
     *
     * @return priority of this module
     */
    default int getPriority() {
        return 1000;
    }

    /**
     * The ID of this module. This is used to sort on a priority tie, should ideally be of the format "modID":"moduleName".
     *
     * @return The ID of this module.
     */
    ResourceLocation getId();

    /**
     * Used to reset the module state for the given hand, this is called when the Interact Tracker is not active anymore,
     * and before {@link #doProcess} is called
     *
     * @param player the local player, {@code null} if not in a world
     * @param hand   the hand to reset
     */
    default void reset(@Nullable LocalPlayer player, InteractionHand hand) {}

    /**
     * This is used to check if the user can use the Interact keybind at the given position to interact with the module.
     *
     * @param player       the local player
     * @param hand         the hand to process for
     * @param handPosition the world position the {@code hand} is at
     * @return true if this module is active and wants to use the Interact keybind
     */
    boolean doProcess(LocalPlayer player, InteractionHand hand, Vec3 handPosition);

    /**
     * Use this to do an action when the Interact keybind is being pressed.<br>
     * This is only called when {@link #doProcess} returned true.<br>
     * If this returns true it will cause a haptic pulse on the provided {@code hand} to indicate success.
     *
     * @param player the local player
     * @param hand   the hand that is pressing the Interact keybind
     * @return if the interaction was successful
     */
    boolean processBindingPress(LocalPlayer player, InteractionHand hand);

    /**
     * By default, an interaction causes an armswing, to give the player a visual indicator that the action was successful.
     * This can be used overridden to prevent that.
     *
     * @return if the interaction should cause a hand swing after a successful {@link #processBindingPress} call
     */
    default boolean swingsArm() {
        return true;
    }

    @Override
    default int compareTo(@NotNull InteractModule o) {
        return this.getPriority() == o.getPriority() ?
            this.getId().compareTo(o.getId()) :
            this.getPriority() - o.getPriority();
    }
}
