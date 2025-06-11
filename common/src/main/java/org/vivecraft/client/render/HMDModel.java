package org.vivecraft.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public class HMDModel extends HumanoidModel<PlayerRenderState> {
    public HMDModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createHMDLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition head = root.clearChild("head");
        head.clearChild("hat");
        root.clearChild("body");
        root.clearChild("left_arm");
        root.clearChild("right_arm");
        root.clearChild("left_leg");
        root.clearChild("right_leg");
        head.addOrReplaceChild("vrHMD", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-3.5F, -6.0F, -7.5F,
                    7.0F, 4.0F, 5.0F),
            PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
