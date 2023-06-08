package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class RunTracker extends Tracker
{
    private double direction = 0.0D;
    private double speed = 0.0D;
    private Vec3 movedir;

    public RunTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p)
    {
        if (ClientDataHolderVR.getInstance().vrPlayer.getFreeMove() && !ClientDataHolderVR.getInstance().vrSettings.seated)
        {
            if (ClientDataHolderVR.getInstance().vrSettings.vrFreeMoveMode != VRSettings.FreeMove.RUN_IN_PLACE)
            {
                return false;
            }
            else if (p != null && p.isAlive())
            {
                if (this.mc.gameMode == null)
                {
                    return false;
                }
                else if (p.onGround() || !p.isInWater() && !p.isInLava())
                {
                    if (p.onClimbable())
                    {
                        return false;
                    }
                    else
                    {
                        return !ClientDataHolderVR.getInstance().bowTracker.isNotched();
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public double getYaw()
    {
        return this.direction;
    }

    public double getSpeed()
    {
        return this.speed;
    }

    public void reset(LocalPlayer player)
    {
        this.speed = 0.0D;
    }

    public void doProcess(LocalPlayer player)
    {
        Vec3 vec3 = this.dh.vrPlayer.vrdata_world_pre.getController(0).getPosition();
        Vec3 vec31 = this.dh.vrPlayer.vrdata_world_pre.getController(1).getPosition();
        double d0 = this.dh.vr.controllerHistory[0].averageSpeed(0.33D);
        double d1 = this.dh.vr.controllerHistory[1].averageSpeed(0.33D);

        if (this.speed > 0.0D)
        {
            if (d0 < 0.1D && d1 < 0.1D)
            {
                this.speed = 0.0D;
                return;
            }
        }
        else if (d0 < 0.6D && d1 < 0.6D)
        {
            this.speed = 0.0D;
            return;
        }

        if (Math.abs(d0 - d1) > 0.5D)
        {
            this.speed = 0.0D;
        }
        else
        {
            Vec3 vec32 = this.dh.vrPlayer.vrdata_world_pre.getController(0).getDirection().add(this.dh.vrPlayer.vrdata_world_pre.getController(1).getDirection()).scale(0.5D);
            this.direction = (double)((float)Math.toDegrees(Math.atan2(-vec32.x, vec32.z)));
            double d2 = (d0 + d1) / 2.0D;
            this.speed = d2 * 1.0D * 1.3D;

            if (this.speed > 0.1D)
            {
                this.speed = 1.0D;
            }

            if (this.speed > 1.0D)
            {
                this.speed = (double)1.3F;
            }
        }
    }
}
