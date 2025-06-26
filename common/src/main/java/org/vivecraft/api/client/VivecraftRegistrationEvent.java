package org.vivecraft.api.client;

public interface VivecraftRegistrationEvent {

    /**
     * Registers the given trackers to the list of all trackers to be run for the local player. See the documentation for
     * {@link Tracker} for more information on what a tracker is.
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
