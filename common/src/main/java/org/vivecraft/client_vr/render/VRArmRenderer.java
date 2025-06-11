package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.vivecraft.client_vr.provider.ControllerType;

public class VRArmRenderer extends PlayerRenderer {

    public float armAlpha = 1F;

    public VRArmRenderer(EntityRendererProvider.Context context, boolean useSlimModel) {
        super(context, useSlimModel);
    }

    @Override
    public void renderRightHand(
        PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation resourceLocation,
        boolean sleeve)
    {
        this.renderHand(ControllerType.RIGHT, poseStack, buffer, combinedLight, resourceLocation, this.model.rightArm,
            sleeve);
    }

    @Override
    public void renderLeftHand(
        PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation resourceLocation,
        boolean sleeve)
    {
        this.renderHand(ControllerType.LEFT, poseStack, buffer, combinedLight, resourceLocation, this.model.leftArm,
            sleeve);
    }

    /**
     * renders the player hand<br>
     * copy of {@link PlayerRenderer#renderHand}
     *
     * @param side             controller this hand belongs to
     * @param poseStack        PoseStack top use for rendering
     * @param buffer           MultiBufferSource to use
     * @param combinedLight    brightness of the hand
     * @param resourceLocation skin of the player the arm is from
     * @param rendererArm      Arm to render
     * @param sleeve           if the sleeve should be rendered
     */
    private void renderHand(
        ControllerType side, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
        ResourceLocation resourceLocation, ModelPart rendererArm, boolean sleeve)
    {
        PlayerModel playermodel = this.getModel();

        // blending, since we render the arm translucent
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // in case some mod messes with it
        rendererArm.resetPose();
        rendererArm.visible = true;

        // make sure they have the same state
        playermodel.leftSleeve.visible = sleeve;
        playermodel.rightSleeve.visible = sleeve;

        // render hand
        rendererArm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(resourceLocation)), combinedLight,
            OverlayTexture.NO_OVERLAY, ARGB.white(this.armAlpha));

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
