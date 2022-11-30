package org.vivecraft.api.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.api.ClientDataHolder;
import org.vivecraft.api.settings.AutoCalibration;

public class SneakTracker extends Tracker
{
    public boolean sneakOverride = false;
    public int sneakCounter = 0;

    public SneakTracker(Minecraft mc, ClientDataHolder dh)
    {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p)
    {
        if (ClientDataHolder.getInstance().vrSettings.seated)
        {
            return false;
        }
        else if (!ClientDataHolder.getInstance().vrPlayer.getFreeMove() && !ClientDataHolder.getInstance().vrSettings.simulateFalling)
        {
            return false;
        }
        else if (!ClientDataHolder.getInstance().vrSettings.realisticSneakEnabled)
        {
            return false;
        }
        else if (this.mc.gameMode == null)
        {
            return false;
        }
        else if (p != null && p.isAlive() && p.isOnGround())
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
