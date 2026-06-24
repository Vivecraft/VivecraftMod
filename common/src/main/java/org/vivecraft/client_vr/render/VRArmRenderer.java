package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.provider.ControllerType;

public class VRArmRenderer extends PlayerRenderer {
    public VRArmRenderer(EntityRendererProvider.Context context, boolean useSlimModel) {
        super(context, useSlimModel);
    }

    @Override
    public void renderRightHand(
        PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        this.renderHand(ControllerType.RIGHT, poseStack, buffer, combinedLight, player, this.model.rightArm,
            this.model.rightSleeve);
    }

    @Override
    public void renderLeftHand(
        PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        this.renderHand(ControllerType.LEFT, poseStack, buffer, combinedLight, player, this.model.leftArm,
            this.model.leftSleeve);
    }

    /**
     * renders the player hand<br>
     * copy of {@link PlayerRenderer#renderHand}
     *
     * @param side            controller this hand belongs to
     * @param poseStack       PoseStack top use for rendering
     * @param buffer          MultiBufferSource to use
     * @param combinedLight   brightness of the hand
     * @param player          Player the hand is from
     * @param rendererArm     Arm to render
     * @param rendererArmwear Armor to render
     */
    private void renderHand(
        ControllerType side, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
        AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear)
    {
        PlayerModel<AbstractClientPlayer> playerModel = this.getModel();
        this.setModelProperties(player);

        // blending, since we render the arm translucent
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // in case some mod messes with it
        rendererArm.setPos(side == ControllerType.LEFT ? 5F : -5F, 2F, 0F);
        rendererArm.setRotation(0F, 0F, 0F);
        rendererArm.xScale = rendererArm.yScale = rendererArm.zScale = 1F;

        // make sure they have the same state
        rendererArmwear.copyFrom(rendererArm);

        float alpha = SwingTracker.getItemFade((LocalPlayer) player, ItemStack.EMPTY);
        ResourceLocation playerSkin = this.getTextureLocation(player);

        // render hand
        rendererArm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(playerSkin)),
            combinedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));

        // render armor
        rendererArmwear.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(playerSkin)), combinedLight,
            OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F));

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
