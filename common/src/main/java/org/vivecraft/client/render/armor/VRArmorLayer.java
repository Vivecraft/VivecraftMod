package org.vivecraft.client.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class VRArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends HumanoidArmorLayer<S, M, A> {

    // split arms model
    public static ArmorModelSet<LayerDefinition> VR_ARMOR_DEF_ARMS;

    // split arms/legs model
    public static ArmorModelSet<LayerDefinition> VR_ARMOR_DEF_ARMS_LEGS;

    static {
        // need to make these not final, because they change depending on settings
        createLayers();
    }

    public static void createLayers() {
        // split arms model
        VR_ARMOR_DEF_ARMS = VRArmorModel_WithArms.createArmorMeshSet(new CubeDeformation(0.5F),
            new CubeDeformation(1.0F)).map(meshDefinition -> LayerDefinition.create(meshDefinition, 64, 32));

        // split arms/legs model
        VR_ARMOR_DEF_ARMS_LEGS = VRArmorModel_WithArmsLegs.createArmorMeshSet(new CubeDeformation(0.5F),
            new CubeDeformation(1.0F)).map(meshDefinition -> LayerDefinition.create(meshDefinition, 64, 32));
    }

    public VRArmorLayer(
        RenderLayerParent<S, M> renderer, ArmorModelSet<A> armorModelSet, EquipmentLayerRenderer equipmentRenderer)
    {
        super(renderer, armorModelSet, equipmentRenderer);
    }
}
