package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client_vr.settings.AutoCalibration;

import org.joml.Vector3d;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class SneakTracker extends Tracker
{
    public boolean sneakOverride = false;
    public int sneakCounter = 0;

    @Override
    public boolean isActive()
    {
        if (dh.vrSettings.seated)
        {
            return false;
        }
        else if (!dh.vrPlayer.getFreeMove() && !dh.vrSettings.simulateFalling)
        {
            return false;
        }
        else if (!dh.vrSettings.realisticSneakEnabled)
        {
            return false;
        }
        else if (mc.gameMode == null)
        {
            return false;
        }
        else if (mc.player != null && mc.player.isAlive() && mc.player.onGround())
        {
            return !mc.player.isPassenger();
        }
        else
        {
            return false;
        }
    }

    @Override
    public void reset()
    {
        this.sneakOverride = false;
    }

    @Override
    public void doProcess()
    {
        if (!mc.isPaused() && dh.sneakTracker.sneakCounter > 0)
        {
            --dh.sneakTracker.sneakCounter;
        }

        this.sneakOverride = (double)AutoCalibration.getPlayerHeight() - dh.vr.hmdPivotHistory.latest(new Vector3d()).y > (double)dh.vrSettings.sneakThreshold;
    }
}
