package org.vivecraft.client.api_impl;

import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.VivecraftRegistrationEvent;
import org.vivecraft.client_vr.ClientDataHolderVR;

public final class VivecraftRegistrationEventImpl implements VivecraftRegistrationEvent {

    public static final VivecraftRegistrationEventImpl INSTANCE = new VivecraftRegistrationEventImpl();

    private VivecraftRegistrationEventImpl() {}

    @Override
    public void registerTrackers(Tracker... trackers) {
        ClientDataHolderVR.getInstance().registerTracker(trackers);
    }

    @Override
    public void registerInteractModules(InteractModule... modules) {
        ClientDataHolderVR.getInstance().interactTracker.registerModules(modules);
    }
}
