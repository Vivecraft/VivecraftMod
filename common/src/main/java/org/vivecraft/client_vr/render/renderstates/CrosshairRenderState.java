package org.vivecraft.client_vr.render.renderstates;

import com.mojang.blaze3d.vertex.PoseStack;

public class CrosshairRenderState {

    public boolean occlude;
    public boolean shouldRender;

    public final PoseStack poseStack = new PoseStack();
    public float brightness;
    public int light;
}
