package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.render.armor.VRArmorLayer;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class VRPlayerRenderer extends AvatarRenderer<AbstractClientPlayer> {
    // Vanilla model
    private static final LayerDefinition VR_LAYER_DEF = LayerDefinition.create(
        VRPlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    private static final LayerDefinition VR_LAYER_DEF_SLIM = LayerDefinition.create(
        VRPlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64);

    // split arms model
    private static LayerDefinition VR_LAYER_DEF_ARMS;
    private static LayerDefinition VR_LAYER_DEF_ARMS_SLIM;

    // split arms/legs model
    private static LayerDefinition VR_LAYER_DEF_ARMS_LEGS;
    private static LayerDefinition VR_LAYER_DEF_ARMS_LEGS_SLIM;

    static {
        // need to make these not final, because they change depending on settings
        createLayers();
    }

    public static void createLayers() {
        // split arms model
        VR_LAYER_DEF_ARMS = LayerDefinition.create(
            VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, false), 64, 64);
        VR_LAYER_DEF_ARMS_SLIM = LayerDefinition.create(
            VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, true), 64, 64);

        // split arms/legs model
        VR_LAYER_DEF_ARMS_LEGS = LayerDefinition.create(
            VRPlayerModel_WithArmsLegs.createMesh(CubeDeformation.NONE, false), 64, 64);
        VR_LAYER_DEF_ARMS_LEGS_SLIM = LayerDefinition.create(
            VRPlayerModel_WithArmsLegs.createMesh(CubeDeformation.NONE, true), 64, 64);
    }

    public enum ModelType {
        VANILLA,
        SPLIT_ARMS,
        SPLIT_ARMS_LEGS
    }

    public VRPlayerRenderer(EntityRendererProvider.Context context, boolean slim, ModelType type) {
        super(context, slim);
        this.model = switch (type) {
            case VANILLA -> new VRPlayerModel(slim ? VR_LAYER_DEF_SLIM.bakeRoot() : VR_LAYER_DEF.bakeRoot(), slim);
            case SPLIT_ARMS ->
                new VRPlayerModel_WithArms(slim ? VR_LAYER_DEF_ARMS_SLIM.bakeRoot() : VR_LAYER_DEF_ARMS.bakeRoot(),
                    slim);
            case SPLIT_ARMS_LEGS -> new VRPlayerModel_WithArmsLegs(
                slim ? VR_LAYER_DEF_ARMS_LEGS_SLIM.bakeRoot() : VR_LAYER_DEF_ARMS_LEGS.bakeRoot(), slim);
        };

        this.addLayer(new HMDLayer(this));

        VRArmorLayer.createLayers();
        if (type != ModelType.VANILLA) {
            // remove vanilla armor layer
            this.layers.stream()
                .filter(layer -> layer.getClass() == HumanoidArmorLayer.class)
                .findFirst()
                .ifPresent(this.layers::remove);
            // add split armor layer
            if (type == ModelType.SPLIT_ARMS) {
                this.addLayer(new VRArmorLayer<>(this,
                    VRArmorLayer.VR_ARMOR_DEF_ARMS.map(
                        layerDefinition -> new VRPlayerModel_WithArms(layerDefinition.bakeRoot(), slim)),
                    context.getEquipmentRenderer()));
            } else {
                this.addLayer(new VRArmorLayer<>(this,
                    VRArmorLayer.VR_ARMOR_DEF_ARMS_LEGS.map(
                        layerDefinition -> new VRPlayerModel_WithArmsLegs(layerDefinition.bakeRoot(), slim)),
                    context.getEquipmentRenderer()));
            }
        }
    }

    /**
     * @param renderLayer RenderLayer to check
     * @return if a layer of the given class is already registered
     */
    public boolean hasLayerType(RenderLayer<?, ?> renderLayer) {
        return this.layers.stream().anyMatch(layer -> {
            if (renderLayer.getClass() == HumanoidArmorLayer.class) {
                return layer.getClass() == renderLayer.getClass() || layer.getClass() == VRArmorLayer.class;
            }
            return layer.getClass() == renderLayer.getClass();
        });
    }

    @Override
    public Vec3 getRenderOffset(AvatarRenderState renderState) {
        // idk why we do this anymore
        // this changes the offset to only apply when swimming, instead of crouching
        if (((EntityRenderStateExtension) renderState).vivecraft$isFirstPersonPlayer()) {
            return renderState.isVisuallySwimming ?
                new Vec3(0.0F, -0.125F * ClientDataHolderVR.getInstance().vrPlayer.worldScale, 0.0F) : Vec3.ZERO;
        } else {
            return renderState.isVisuallySwimming ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
        }
    }

    @Override
    protected void setupRotations(
        AvatarRenderState renderState, PoseStack poseStack, float rotationYaw, float scale)
    {
        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) renderState).vivecraft$getRotInfo();
        if (ClientDataHolderVR.getInstance().currentPass != RenderPass.GUI && rotInfo != null) {
            if (((EntityRenderStateExtension) renderState).vivecraft$isFirstPersonPlayer()) {
                rotationYaw = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getBodyYaw();
            } else {
                rotationYaw = Mth.RAD_TO_DEG * rotInfo.getBodyYawRad();
            }
        }

        // vanilla below here
        super.setupRotations(renderState, poseStack, rotationYaw, scale);
    }
}
