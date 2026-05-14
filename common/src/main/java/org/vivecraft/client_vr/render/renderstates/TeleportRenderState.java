package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TeleportRenderState {
    public boolean aiming;
    public boolean arcAiming;
    public boolean showBeam;
    public boolean showHitIndicator;
    public boolean tpEnergy;
    public float tpEnergySize;

    public Vec3i color = Vec3i.ZERO;
    public byte alpha;

    public Vec3 dest = Vec3.ZERO;
    public boolean validLocation;
    public float segmentHalfWidth;

    public List<Segment> segments = new ArrayList<>();

    public record Segment(Vec3 start, Vec3 end, float vOffset) {}


    public void extract(@Nullable LocalPlayer player) {
        if (player == null) {
            this.tpEnergy = false;
            this.aiming = false;
            return;
        }
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        Minecraft mc = Minecraft.getInstance();

        this.tpEnergy = ClientNetworking.isLimitedSurvivalTeleport() &&
            !dataHolder.vrPlayer.getFreeMove() &&
            mc.gameMode != null && mc.gameMode.hasMissTime() &&
            dataHolder.teleportTracker.vrMovementStyle.arcAiming &&
            !dataHolder.bowTracker.isActive(player);

        this.aiming = dataHolder.teleportTracker.isAiming();

        if (this.aiming) {
            this.tpEnergySize = 2.0F * (dataHolder.teleportTracker.getTeleportEnergy() -
                4.0F * (float) dataHolder.teleportTracker.movementTeleportDistance
            ) / 100.0F;
        } else {
            this.tpEnergySize = 2.0F * dataHolder.teleportTracker.getTeleportEnergy() / 100.0F;
        }

        this.arcAiming = dataHolder.teleportTracker.vrMovementStyle.arcAiming;

        this.showBeam = this.aiming &&
            dataHolder.teleportTracker.vrMovementStyle.showBeam &&
            dataHolder.teleportTracker.isAiming() &&
            dataHolder.teleportTracker.movementTeleportArcSteps > 1;

        // don't need any of the arc stuff if we are not aiming
        if (!this.showBeam) return;

        this.dest = dataHolder.teleportTracker.getDestination();
        this.validLocation =
            this.dest.x != 0.0D || this.dest.y != 0.0D || this.dest.z != 0.0D;
        this.showHitIndicator = dataHolder.teleportTracker.movementTeleportProgress >= 1.0D;

        double vOffset;
        if (!this.validLocation) {
            // invalid location
            this.color = VRArmHelper.TP_INVALID_COLOR;
            this.alpha = (byte) 128;
            vOffset = dataHolder.teleportTracker.lastTeleportArcDisplayOffset;
        } else {
            this.alpha = (byte) 255;
            if (ClientNetworking.isLimitedSurvivalTeleport() && !player.getAbilities().mayfly) {
                this.color = VRArmHelper.TP_LIMITED_COLOR;
            } else {
                this.color = VRArmHelper.TP_UNLIMITED_COLOR;
            }

            vOffset = Util.getMillis() * 0.001D
                * (double) dataHolder.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
            dataHolder.teleportTracker.lastTeleportArcDisplayOffset = vOffset;
        }

        this.segmentHalfWidth = dataHolder.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
        int segments = dataHolder.teleportTracker.movementTeleportArcSteps - 1;

        if (dataHolder.teleportTracker.vrMovementStyle.beamGrow) {
            segments = (int) (segments * dataHolder.teleportTracker.movementTeleportProgress);
        }
        this.segments.clear();
        if (segments > 0) {
            float segmentProgress = 1.0F / (float) segments;
            for (int i = 0; i < segments; i++) {
                float progress = Mth.frac((float) i / (float) segments + (float) (vOffset * segmentProgress));

                Vec3 start = dataHolder.teleportTracker.getInterpolatedArcPosition(progress - segmentProgress * 0.4F);

                Vec3 end = dataHolder.teleportTracker.getInterpolatedArcPosition(progress);

                this.segments.add(new TeleportRenderState.Segment(start, end, progress * 2.0F));
            }
        }
    }
}
