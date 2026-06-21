package org.vivecraft.client.gui.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.gui.pip.state.GuiFBTPlayerState;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;

public class GuiFBTPlayerRenderer extends PictureInPictureRenderer<GuiFBTPlayerState> {

    private static final Vec3i COLOR_INACTIVE = new Vec3i(128, 64, 64);
    private static final Vec3i COLOR_ACTIVE = new Vec3i(64, 128, 64);
    private static final byte ALPHA = (byte) 200;

    public GuiFBTPlayerRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiFBTPlayerState> getRenderStateClass() {
        return GuiFBTPlayerState.class;
    }

    @Override
    protected void renderToTexture(GuiFBTPlayerState fbtState, PoseStack poseStack) {

        poseStack.pushPose();
        poseStack.translate(-0.5, -32, 0);
        poseStack.scale(4, -4, 4);
        poseStack.mulPose(Axis.YP.rotation(Mth.PI + fbtState.yRot()));

        // body overlay
        RenderType renderType = VRRenderTypes.quads(true);
        VertexConsumer consumer = this.bufferSource.getBuffer(renderType);

        Vec3i color = fbtState.leftReady() && fbtState.rightReady() ? COLOR_ACTIVE : COLOR_INACTIVE;

        // legs
        RenderHelper.renderBox(consumer, new Vec3(2, 0, 0), new Vec3(2, 12, 0),
            4, 4, color, ALPHA, poseStack.last());
        RenderHelper.renderBox(consumer, new Vec3(-2, 0, 0), new Vec3(-2, 12, 0),
            4, 4, color, ALPHA, poseStack.last());
        // body
        RenderHelper.renderBox(consumer, new Vec3(0, 12, 0), new Vec3(0, 24, 0),
            8, 4, color, ALPHA, poseStack.last());

        // head
        RenderHelper.renderBox(consumer, new Vec3(0, 24, 0), new Vec3(0, 32, 0),
            8, 8, color, ALPHA, poseStack.last());

        // arms
        RenderHelper.renderBox(consumer,
            new Vec3(6, 22, 0).subtract(fbtState.left().x() * 2F, fbtState.left().y() * 2F, fbtState.left().z() * 2F),
            new Vec3(6, 22, 0).add(fbtState.left().x() * 10F, fbtState.left().y() * 10F, fbtState.left().z() * 10F),
            4, 4, fbtState.leftReady() ? COLOR_ACTIVE : COLOR_INACTIVE, ALPHA, poseStack.last());
        RenderHelper.renderBox(consumer,
            new Vec3(-6, 22, 0).subtract(fbtState.right().x() * 2F, fbtState.right().y() * 2F,
                fbtState.right().z() * 2F),
            new Vec3(-6, 22, 0).add(fbtState.right().x() * 10F, fbtState.right().y() * 10F, fbtState.right().z() * 10F),
            4, 4, fbtState.rightReady() ? COLOR_ACTIVE : COLOR_INACTIVE, ALPHA, poseStack.last());

        this.bufferSource.endBatch(renderType);
        poseStack.popPose();
    }

    @Override
    protected String getTextureLabel() {
        return "fbt player";
    }
}
