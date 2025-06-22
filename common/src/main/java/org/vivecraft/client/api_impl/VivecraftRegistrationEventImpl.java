package org.vivecraft.client.api_impl;

import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.api.client.VivecraftRegistrationEvent;
import org.vivecraft.client_vr.ClientDataHolderVR;

public final class VivecraftRegistrationEventImpl implements VivecraftRegistrationEvent {

    public static final VivecraftRegistrationEventImpl INSTANCE = new VivecraftRegistrationEventImpl();

    private VivecraftRegistrationEventImpl() {}

    @Override
    public void registerTracker(Tracker... tracker) {
        ClientDataHolderVR.getInstance().registerTracker(tracker);
    }

    @Override
    public void registerInteractModule(InteractModule... module) {
        ClientDataHolderVR.getInstance().interactTracker.registerModules(module);
    }
}
