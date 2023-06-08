package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.AutoCalibration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class SneakTracker extends Tracker
{
    public boolean sneakOverride = false;
    public int sneakCounter = 0;

    public SneakTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p)
    {
        if (ClientDataHolderVR.getInstance().vrSettings.seated)
        {
            return false;
        }
        else if (!ClientDataHolderVR.getInstance().vrPlayer.getFreeMove() && !ClientDataHolderVR.getInstance().vrSettings.simulateFalling)
        {
            return false;
        }
        else if (!ClientDataHolderVR.getInstance().vrSettings.realisticSneakEnabled)
        {
            return false;
        }
        else if (this.mc.gameMode == null)
        {
            return false;
        }
        else if (p != null && p.isAlive() && p.onGround())
        {
            return !p.isPassenger();
        }
        else
        {
            return false;
        }
    }

    public void reset(LocalPlayer player)
    {
        this.sneakOverride = false;
    }

    public void doProcess(LocalPlayer player)
    {
        if (!this.mc.isPaused() && this.dh.sneakTracker.sneakCounter > 0)
        {
            --this.dh.sneakTracker.sneakCounter;
        }

        if ((double) AutoCalibration.getPlayerHeight() - this.dh.vr.hmdPivotHistory.latest().y > (double)this.dh.vrSettings.sneakThreshold)
        {
            this.sneakOverride = true;
        }
        else
        {
            this.sneakOverride = false;
        }
    }
}
