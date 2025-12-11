package org.vivecraft.client.render.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import org.vivecraft.client.render.VRPlayerModel_WithArms;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class VRArmorModel_WithArms {

    public static ArmorModelSet<MeshDefinition> createArmorMeshSet(CubeDeformation inner, CubeDeformation outer) {
        ArmorModelSet<MeshDefinition> armorSet = PlayerModel.createArmorMeshSet(inner, outer)
            .map(mesh -> {
                PartDefinition root = mesh.getRoot();
                root.addOrReplaceChild("left_hand", CubeListBuilder.create(), PartPose.ZERO);
                root.addOrReplaceChild("right_hand", CubeListBuilder.create(), PartPose.ZERO);
                return mesh;
            });
        addVRParts(armorSet.chest(), outer);
        return armorSet.map(mesh -> {
            PartDefinition root = mesh.getRoot();
            root.getChild("left_arm").addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
            root.getChild("right_arm").addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
            root.getChild("left_hand").addOrReplaceChild("left_hand_sleeve", CubeListBuilder.create(), PartPose.ZERO);
            root.getChild("right_hand").addOrReplaceChild("right_hand_sleeve", CubeListBuilder.create(), PartPose.ZERO);
            return mesh;
        });
    }

    protected static void addVRParts(MeshDefinition meshDefinition, CubeDeformation cubeDeformation) {
        PartDefinition root = meshDefinition.getRoot();

        boolean connected = ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected;
        int upperExtension = connected ? VRPlayerModel_WithArms.UPPER_EXTENSION : 0;
        int lowerExtension = connected ? VRPlayerModel_WithArms.LOWER_EXTENSION : 0;
        float lowerShrinkage = connected ? -0.05F : 0F;

        root.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(40, 23 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(-5.0F, 2.5F, 0.0F));
        root.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(40, 23 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(-5.0F, 2.5F, 0.0F));


        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).mirror()
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
    }
}
