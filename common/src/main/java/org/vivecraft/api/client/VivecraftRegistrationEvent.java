package org.vivecraft.api.client;

import java.util.function.Consumer;

/**
 * The event that is fired for registering trackers and interact modules with Vivecraft. One can interact with this
 * event by registering a handler using {@link VRClientAPI#addRegistrationHandler(Consumer)}.
 *
 * @since 1.3.0
 */
public interface VivecraftRegistrationEvent {

    /**
     * Registers the given trackers to the list of all trackers to be run for the local player. See the documentation
     * for {@link Tracker} for more information on what a tracker is.
     *
     * @param trackers Trackers to register.
     * @since 1.3.0
     */
    void registerTrackers(Tracker... trackers);

    /**
     * Registers the given interact modules to the list of all interact modules to be run for the local player.
     * See the documentation for {@link InteractModule} for more information on what an interact modules is.
     *
     * @param modules InteractModules to register.
     * @since 1.3.0
     */
    void registerInteractModules(InteractModule... modules);
}
