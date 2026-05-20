package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CrosshairRenderState {

    public boolean occlude;
    public boolean shouldRender;

    public Vec3 pos;
    public final Matrix4f rotation = new Matrix4f();
    public float scale;
    public float brightness;
    public int light;
}
