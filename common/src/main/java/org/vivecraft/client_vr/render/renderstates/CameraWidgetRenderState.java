package org.vivecraft.client_vr.render.renderstates;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class CameraWidgetRenderState {
    public final ItemStackRenderState cameraModelState = new ItemStackRenderState();
    public final ItemStackRenderState displayModelState = new ItemStackRenderState();

    public final PoseStack poseStack = new PoseStack();

    public boolean visible;
    public int combinedLight;
}
