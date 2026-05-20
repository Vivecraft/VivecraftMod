package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;

public class HMDLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final HMDModel model;

    private static final Identifier DIAMOND_HMD = Identifier.parse("vivecraft:textures/diamond_hmd.png");
    private static final Identifier GOLD_HMD = Identifier.parse("vivecraft:textures/gold_hmd.png");
    private static final Identifier BLACK_HMD = Identifier.parse("vivecraft:textures/black_hmd.png");

    private static final LayerDefinition HMD_LAYER_DEF = HMDModel.createHMDLayer();

    public HMDLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
        this.model = new HMDModel(HMD_LAYER_DEF.bakeRoot());
    }

    @Override
    public void submit(
        PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AvatarRenderState entityState,
        float yRot, float xRot)
    {
        // check that the model actually is a vrPlayer model, some mods override the model
        if (getParentModel().head.visible) {
            ClientVRPlayers.RotInfo rotinfo = ((EntityRenderStateExtension) entityState).vivecraft$getRotInfo();
            if (rotinfo != null) {
                Identifier hmd = switch (rotinfo.hmd) {
                    case 1 -> BLACK_HMD;
                    case 2 -> GOLD_HMD;
                    case 3, 4 -> DIAMOND_HMD;
                    default -> null;
                };

                if (hmd == null) return;

                this.getParentModel().root().translateAndRotate(poseStack);
                this.getParentModel().translateToHead(poseStack);
                submitNodeCollector.order(0)
                    .submitModelPart(this.model.head, poseStack, RenderTypes.entitySolid(hmd), packedLight,
                        OverlayTexture.NO_OVERLAY, null, 0xFFFFFFFF, null);
            }
        }
    }
}
