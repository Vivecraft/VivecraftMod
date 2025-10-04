package org.vivecraft.client.render.armor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import org.vivecraft.client.render.VRPlayerModel_WithArmsLegs;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class VRArmorModel_WithArmsLegs {

    public static ArmorModelSet<MeshDefinition> createArmorMeshSet(CubeDeformation inner, CubeDeformation outer) {
        ArmorModelSet<MeshDefinition> armorSet = VRArmorModel_WithArms.createArmorMeshSet(inner, outer)
            .map(mesh -> {
                PartDefinition root = mesh.getRoot();
                root.addOrReplaceChild("left_foot", CubeListBuilder.create(), PartPose.ZERO);
                root.addOrReplaceChild("right_foot", CubeListBuilder.create(), PartPose.ZERO);
                return mesh;
            });
        addVRParts(armorSet.legs(), inner);
        addVRParts(armorSet.feet(), outer);

        return armorSet.map(mesh -> {
            PartDefinition root = mesh.getRoot();
            root.getChild("left_leg").addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);
            root.getChild("right_leg").addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);
            root.getChild("left_foot").addOrReplaceChild("left_foot_pants", CubeListBuilder.create(), PartPose.ZERO);
            root.getChild("right_foot").addOrReplaceChild("right_foot_pants", CubeListBuilder.create(), PartPose.ZERO);
            return mesh;
        });
    }

    protected static void addVRParts(MeshDefinition meshDefinition, CubeDeformation cubeDeformation) {
        PartDefinition partDefinition = meshDefinition.getRoot();

        boolean connected = ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected;
        int upperExtension = connected ? VRPlayerModel_WithArmsLegs.UPPER_EXTENSION : 0;
        int lowerExtension = connected ? VRPlayerModel_WithArmsLegs.LOWER_EXTENSION : 0;
        float lowerShrinkage = connected ? -0.05F : 0F;

        partDefinition.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(0, 23 - lowerExtension).mirror()
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage - 0.1F)),
            PartPose.offset(1.9F, 24.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(0, 23 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage - 0.1F)),
            PartPose.offset(-1.9F, 24.0F, 0.0F));

        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 16).mirror()
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(-0.1F)),
            PartPose.offset(1.9F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(-0.1F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F));
    }
}
