package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.api.client.Tracker;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.AutoCalibration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class SneakTracker implements Tracker {
    public boolean sneakOverride = false;
    public int sneakCounter = 0;
    protected Minecraft mc;
    protected ClientDataHolderVR dh;

    public SneakTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    public boolean isActive(LocalPlayer p) {
        if (this.dh.vrSettings.seated) {
            return false;
        } else if (!this.dh.vrPlayer.getFreeMove() && !this.dh.vrSettings.simulateFalling) {
            return false;
        } else if (!this.dh.vrSettings.realisticSneakEnabled) {
            return false;
        } else if (this.mc.gameMode == null) {
            return false;
        } else if (p != null && p.isAlive() && p.onGround()) {
            return !p.isPassenger();
        } else {
            return false;
        }
    }

    public void reset(LocalPlayer player) {
        this.sneakOverride = false;
    }

    public void doProcess(LocalPlayer player) {
        if (!this.mc.isPaused() && this.dh.sneakTracker.sneakCounter > 0) {
            --this.dh.sneakTracker.sneakCounter;
        }

        this.sneakOverride = (double) AutoCalibration.getPlayerHeight() - this.dh.vr.hmdPivotHistory.latest().y > (double) this.dh.vrSettings.sneakThreshold;
    }

    @Override
    public TrackerTickType tickType() {
        return TrackerTickType.PER_TICK;
    }
}
