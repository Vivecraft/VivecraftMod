package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.vivecraft.client.VRPlayersClient;

public class HMDLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    ResourceLocation DIAMOND_HMD = ResourceLocation.parse("vivecraft:textures/diamond_hmd.png");
    ResourceLocation GOLD_HMD = ResourceLocation.parse("vivecraft:textures/gold_hmd.png");
    ResourceLocation BLACK_HMD = ResourceLocation.parse("vivecraft:textures/black_hmd.png");

    public HMDLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer entity, float f, float g, float h, float j, float k, float l) {
        if (this.getParentModel().head.visible && this.getParentModel() instanceof VRPlayerModel<?> vrPlayerModel) {
            VRPlayersClient.RotInfo rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(entity.getUUID());
            ResourceLocation hmd;
            switch (rotinfo.hmd) {
                default -> hmd = null;
                case 1 -> hmd = this.BLACK_HMD;
                case 2 -> hmd = this.GOLD_HMD;
                case 3, 4 -> hmd = this.DIAMOND_HMD;
            }

            if (hmd == null) {
                return;
            }
            poseStack.pushPose();
            VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(hmd));
            vrPlayerModel.renderHMDR(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
