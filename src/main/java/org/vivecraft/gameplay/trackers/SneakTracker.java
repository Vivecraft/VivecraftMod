package org.vivecraft.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.settings.AutoCalibration;

public class SneakTracker extends Tracker
{
    public boolean sneakOverride = false;
    public int sneakCounter = 0;

    public SneakTracker(Minecraft mc)
    {
        super(mc);
    }

    public boolean isActive(LocalPlayer p)
    {
        if (Minecraft.getInstance().vrSettings.seated)
        {
            return false;
        }
        else if (!Minecraft.getInstance().vrPlayer.getFreeMove() && !Minecraft.getInstance().vrSettings.simulateFalling)
        {
            return false;
        }
        else if (!Minecraft.getInstance().vrSettings.realisticSneakEnabled)
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
        if (!this.mc.isPaused() && this.mc.sneakTracker.sneakCounter > 0)
        {
            --this.mc.sneakTracker.sneakCounter;
        }

        if ((double)AutoCalibration.getPlayerHeight() - this.mc.vr.hmdPivotHistory.latest().y > (double)this.mc.vrSettings.sneakThreshold)
        {
            this.sneakOverride = true;
        }
        else
        {
            this.sneakOverride = false;
        }
    }
}
