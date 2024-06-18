package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.client_vr.ClientDataHolderVR;

public abstract class Tracker {
    public Minecraft mc;
    public ClientDataHolderVR dh;

    public Tracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    public abstract boolean isActive(LocalPlayer player);

    public abstract void doProcess(LocalPlayer player);

    public void reset(LocalPlayer player) {
    }

    public void idleTick(LocalPlayer player) {
    }

    public EntryPoint getEntryPoint() {
        return EntryPoint.LIVING_UPDATE;
    }

    public enum EntryPoint {
        LIVING_UPDATE,
        SPECIAL_ITEMS
    }
}
