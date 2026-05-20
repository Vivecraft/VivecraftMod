package org.vivecraft.client_vr.render.renderstates;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CameraWidgetRenderState {
    public final ItemStackRenderState cameraModelState = new ItemStackRenderState();
    public final ItemStackRenderState displayModelState = new ItemStackRenderState();

    public final Matrix4f modelMatrix = new Matrix4f();
    public Vec3 pos = Vec3.ZERO;

    public boolean visible;
    public int combinedLight;
}
