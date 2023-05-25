package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client.network.ClientNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.Pose;
import org.vivecraft.common.network.CommonNetworkHelper;

public class CrawlTracker extends Tracker
{
    private boolean wasCrawling;
    public boolean crawling;
    public boolean crawlsteresis;

    public CrawlTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer player)
    {
        if (this.dh.vrSettings.seated)
        {
            return false;
        }
        else if (!this.dh.vrSettings.allowCrawling)
        {
            return false;
        }
        else if (!ClientNetworking.serverAllowsCrawling)
        {
            return false;
        }
        else if (!player.isAlive())
        {
            return false;
        }
        else if (player.isSpectator())
        {
            return false;
        }
        else if (player.isSleeping())
        {
            return false;
        }
        else
        {
            return !player.isPassenger();
        }
    }

    public void reset(LocalPlayer player)
    {
        this.crawling = false;
        this.crawlsteresis = false;
        this.updateState(player);
    }

    public void doProcess(LocalPlayer player)
    {
        this.crawling = this.dh.vr.hmdPivotHistory.averagePosition((double)0.2F).y * (double)this.dh.vrPlayer.worldScale + (double)0.1F < (double)this.dh.vrSettings.crawlThreshold;
        this.updateState(player);
    }

    private void updateState(LocalPlayer player)
    {
        if (this.crawling != this.wasCrawling)
        {
            if (this.crawling)
            {
                player.setPose(Pose.SWIMMING);
                this.crawlsteresis = true;
            }

            if (ClientNetworking.serverAllowsCrawling)
            {
                ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.CRAWL, new byte[] {(byte)(this.crawling ? 1 : 0)});

                if (this.mc.getConnection() != null)
                {
                    this.mc.getConnection().send(serverboundcustompayloadpacket);
                }
            }

            this.wasCrawling = this.crawling;
        }

        if (!this.crawling && player.getPose() != Pose.SWIMMING)
        {
            this.crawlsteresis = false;
        }
    }
}
