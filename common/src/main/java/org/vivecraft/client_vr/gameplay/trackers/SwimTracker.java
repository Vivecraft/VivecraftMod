package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class SwimTracker extends Tracker
{
    Vec3 motion = Vec3.ZERO;
    double friction = (double)0.9F;
    double lastDist;
    final double riseSpeed = (double)0.005F;
    double swimspeed = (double)1.3F;

    public boolean isActive()
    {
        if (dh.vrSettings.seated)
        {
            return false;
        }
        else if (!dh.vrSettings.realisticSwimEnabled)
        {
            return false;
        }
        else if (mc.screen != null)
        {
            return false;
        }
        else if (mc.player != null && mc.player.isAlive())
        {
            if (mc.gameMode == null)
            {
                return false;
            }
            else if (!mc.player.isInWater() && !mc.player.isInLava())
            {
                return false;
            }
            else if (mc.player.zza > 0.0F)
            {
                return false;
            }
            else
            {
                return !(mc.player.xxa > 0.0F);
            }
        }
        else
        {
            return false;
        }
    }

    public void doProcess()
    {
        Vec3 vec3 = dh.vrPlayer.vrdata_world_pre.getController(0).getPosition();
        Vec3 vec31 = dh.vrPlayer.vrdata_world_pre.getController(1).getPosition();
        Vec3 vec32 = vec31.subtract(vec3).scale(0.5D).add(vec3);
        Vec3 vec33 = dh.vrPlayer.vrdata_world_pre.getHeadPivot().subtract(0.0D, 0.3D, 0.0D);
        Vec3 vec34 = vec32.subtract(vec33).normalize().add(dh.vrPlayer.vrdata_world_pre.hmd.getDirection()).scale(0.5D);
        Vec3 vec35 = dh.vrPlayer.vrdata_world_pre.getController(0).getCustomVector(new Vec3(0.0D, 0.0D, -1.0D)).add(dh.vrPlayer.vrdata_world_pre.getController(1).getCustomVector(new Vec3(0.0D, 0.0D, -1.0D))).scale(0.5D);
        double d0 = vec35.add(vec34).length() / 2.0D;
        double d1 = vec33.distanceTo(vec32);
        double d2 = this.lastDist - d1;

        if (d2 > 0.0D)
        {
            Vec3 vec36 = vec34.scale(d2 * this.swimspeed * d0);
            this.motion = this.motion.add(vec36.scale(0.15D));
        }

        this.lastDist = d1;
        mc.player.setSwimming(this.motion.length() > (double)0.3F);
        mc.player.setSprinting(this.motion.length() > 1.0D);
        mc.player.push(this.motion.x, this.motion.y, this.motion.z);
        this.motion = this.motion.scale(this.friction);
    }
}
