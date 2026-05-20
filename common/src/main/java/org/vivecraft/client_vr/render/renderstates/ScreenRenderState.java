package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ScreenRenderState {
    public final Matrix4f worldRotation = new Matrix4f();
    public Vec3 worldPos;
    public float scale;
    public int lightCoords;
}
