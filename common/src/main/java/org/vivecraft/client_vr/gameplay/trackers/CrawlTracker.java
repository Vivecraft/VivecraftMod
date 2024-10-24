package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.common.network.packet.c2s.CrawlPayloadC2S;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

public class CrawlTracker extends Tracker {
    private boolean wasCrawling;
    public boolean crawling;
    public boolean crawlsteresis;

    public CrawlTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (this.dh.vrSettings.seated) {
            return false;
        } else if (!this.dh.vrSettings.allowCrawling) {
            return false;
        } else if (!ClientNetworking.serverAllowsCrawling) {
            return false;
        } else if (!player.isAlive()) {
            return false;
        } else if (player.isSpectator()) {
            return false;
        } else if (player.isSleeping()) {
            return false;
        } else {
            return !player.isPassenger();
        }
    }

    @Override
    public void reset(LocalPlayer player) {
        this.crawling = false;
        this.crawlsteresis = false;
        this.updateState(player);
    }

    @Override
    public void doProcess(LocalPlayer player) {
        double scaleMultiplier = 1.0;
        if (PehkuiHelper.isLoaded()) {
            scaleMultiplier /= PehkuiHelper.getEntityEyeHeightScale(player, this.mc.getFrameTime());
        }
        this.crawling = this.dh.vr.hmdPivotHistory.averagePosition(0.2F).y * this.dh.vrPlayer.worldScale * scaleMultiplier + 0.1F < this.dh.vrSettings.crawlThreshold;
        this.updateState(player);
    }

    private void updateState(LocalPlayer player) {
        if (this.crawling != this.wasCrawling) {
            if (this.crawling) {
                player.setPose(Pose.SWIMMING);
                this.crawlsteresis = true;
            }

            if (ClientNetworking.serverAllowsCrawling) {
                if (this.mc.getConnection() != null) {
                    this.mc.getConnection()
                        .send(ClientNetworking.createServerPacket(new CrawlPayloadC2S(this.crawling)));
                }
            }

            this.wasCrawling = this.crawling;
        }

        if (!this.crawling && player.getPose() != Pose.SWIMMING) {
            this.crawlsteresis = false;
        }
    }
}
