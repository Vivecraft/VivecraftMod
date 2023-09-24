package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

import org.joml.Vector3d;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.Pose;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class CrawlTracker extends Tracker
{
    private boolean wasCrawling;
    public boolean crawling;
    public boolean crawlsteresis;

    public boolean isActive()
    {
        if (dh.vrSettings.seated)
        {
            return false;
        }
        else if (!dh.vrSettings.allowCrawling)
        {
            return false;
        }
        else if (!ClientNetworking.serverAllowsCrawling)
        {
            return false;
        }
        else if (!mc.player.isAlive())
        {
            return false;
        }
        else if (mc.player.isSpectator())
        {
            return false;
        }
        else if (mc.player.isSleeping())
        {
            return false;
        }
        else
        {
            return !mc.player.isPassenger();
        }
    }

    public void reset()
    {
        this.crawling = false;
        this.crawlsteresis = false;
        this.updateState();
    }

    public void doProcess()
    {
        double scaleMultiplier = 1.0;
        if (Xplat.isModLoaded("pehkui")) {
            scaleMultiplier /= PehkuiHelper.getPlayerScale(mc.player, mc.getFrameTime());
        }
        this.crawling = dh.vr.hmdPivotHistory.averagePosition(0.2D, new Vector3d()).y * dh.vrPlayer.worldScale * scaleMultiplier + 0.1D < (double)dh.vrSettings.crawlThreshold;
        this.updateState();
    }

    private void updateState()
    {
        if (this.crawling != this.wasCrawling)
        {
            if (this.crawling)
            {
                mc.player.setPose(Pose.SWIMMING);
                this.crawlsteresis = true;
            }

            if (ClientNetworking.serverAllowsCrawling)
            {
                ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.CRAWL, new byte[] {(byte)(this.crawling ? 1 : 0)});

                if (mc.getConnection() != null)
                {
                    mc.getConnection().send(serverboundcustompayloadpacket);
                }
            }

            this.wasCrawling = this.crawling;
        }

        if (!this.crawling && mc.player.getPose() != Pose.SWIMMING)
        {
            this.crawlsteresis = false;
        }
    }
}
