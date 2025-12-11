package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.vivecraft.client_vr.provider.ControllerType;

public class VRArmRenderer extends AvatarRenderer<AbstractClientPlayer> {

    public float armAlpha = 1F;

    public VRArmRenderer(EntityRendererProvider.Context context, boolean useSlimModel) {
        super(context, useSlimModel);
    }

    @Override
    public void renderRightHand(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, Identifier identifier,
        boolean sleeve)
    {
        this.renderHand(ControllerType.RIGHT, poseStack, collector, combinedLight, identifier,
            this.model.rightArm,
            sleeve);
    }

    @Override
    public void renderLeftHand(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, Identifier identifier,
        boolean sleeve)
    {
        this.renderHand(ControllerType.LEFT, poseStack, collector, combinedLight, identifier, this.model.leftArm,
            sleeve);
    }

    /**
     * renders the player hand<br>
     * copy of {@link AvatarRenderer#renderHand}
     *
     * @param side          controller this hand belongs to
     * @param poseStack     PoseStack top use for rendering
     * @param collector     Collector to submit render calls to
     * @param combinedLight brightness of the hand
     * @param identifier    skin of the player the arm is from
     * @param rendererArm   Arm to render
     * @param sleeve        if the sleeve should be rendered
     */
    private void renderHand(
        ControllerType side, PoseStack poseStack, SubmitNodeCollector collector, int combinedLight,
        Identifier identifier, ModelPart rendererArm, boolean sleeve)
    {
        PlayerModel playermodel = this.getModel();

        // in case some mod messes with it
        rendererArm.resetPose();
        rendererArm.visible = true;

        // make sure they have the same state
        playermodel.leftSleeve.visible = sleeve;
        playermodel.rightSleeve.visible = sleeve;

        // render hand
        collector.submitModelPart(rendererArm, poseStack, RenderTypes.entityTranslucent(identifier), combinedLight,
            OverlayTexture.NO_OVERLAY, null, ARGB.white(this.armAlpha), null);
    }
}
