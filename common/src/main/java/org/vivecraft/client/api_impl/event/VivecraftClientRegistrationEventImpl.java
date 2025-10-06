package org.vivecraft.client.api_impl.event;

import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.event.VivecraftClientRegistrationEvent;
import org.vivecraft.client_vr.ClientDataHolderVR;

public final class VivecraftClientRegistrationEventImpl implements VivecraftClientRegistrationEvent {

    public static final VivecraftClientRegistrationEventImpl INSTANCE = new VivecraftClientRegistrationEventImpl();

    private VivecraftClientRegistrationEventImpl() {}

    @Override
    public void registerTrackers(Tracker... trackers) {
        ClientDataHolderVR.getInstance().registerTracker(trackers);
    }

    @Override
    public void registerInteractModules(InteractModule... modules) {
        ClientDataHolderVR.getInstance().interactTracker.registerModules(modules);
    }
}
