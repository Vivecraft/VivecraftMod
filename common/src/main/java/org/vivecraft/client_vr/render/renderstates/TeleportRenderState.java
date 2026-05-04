package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

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

    public List<Segment> segments= new ArrayList<>();

    public record Segment(Vec3 start, Vec3 end, float vOffset) {};
}
