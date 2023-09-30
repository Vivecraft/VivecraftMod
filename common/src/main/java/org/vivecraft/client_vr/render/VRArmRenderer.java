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
import net.minecraft.world.item.ItemStack;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.provider.ControllerType;

public class VRArmRenderer extends PlayerRenderer {
    public VRArmRenderer(EntityRendererProvider.Context p_117733_, boolean p_117734_) {
        super(p_117733_, p_117734_);
    }

    public void renderRightHand(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, AbstractClientPlayer pPlayer) {
        this.renderItem(ControllerType.RIGHT, pMatrixStack, pBuffer, pCombinedLight, pPlayer, (this.model).rightArm, (this.model).rightSleeve);
    }

    public void renderLeftHand(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, AbstractClientPlayer pPlayer) {
        this.renderItem(ControllerType.LEFT, pMatrixStack, pBuffer, pCombinedLight, pPlayer, (this.model).leftArm, (this.model).leftSleeve);
    }

    private void renderItem(ControllerType side, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
        PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
        this.setModelProperties(playerIn);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;
        rendererArmIn.xRot = 0.0F;
        playermodel.leftSleeve.copyFrom(playermodel.leftArm);
        playermodel.rightSleeve.copyFrom(playermodel.rightArm);
        float f = SwingTracker.getItemFade((LocalPlayer) playerIn, ItemStack.EMPTY);
        rendererArmIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.entityTranslucent(playerIn.getSkin().texture())), combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, f);
        rendererArmwearIn.xRot = 0.0F;
        rendererArmwearIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.entityTranslucent(playerIn.getSkin().texture())), combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, f);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
