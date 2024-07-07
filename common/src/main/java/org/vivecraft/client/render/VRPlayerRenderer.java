package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;

import java.util.UUID;

public class VRPlayerRenderer extends PlayerRenderer {
    static LayerDefinition VRLayerDef = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    static LayerDefinition VRLayerDef_arms = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, false), 64, 64);
    static LayerDefinition VRLayerDef_slim = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64);
    static LayerDefinition VRLayerDef_arms_slim = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, true), 64, 64);

    public VRPlayerRenderer(EntityRendererProvider.Context context, boolean slim, boolean seated) {
        super(context, slim);
        model = !slim ?
                (seated ?
                 new VRPlayerModel<>(VRLayerDef.bakeRoot(), slim) :
                 new VRPlayerModel_WithArms<>(VRLayerDef_arms.bakeRoot(), slim)) :
                (seated ?
                 new VRPlayerModel<>(VRLayerDef_slim.bakeRoot(), slim) :
                 new VRPlayerModel_WithArms<>(VRLayerDef_arms_slim.bakeRoot(), slim));

        this.addLayer(new HMDLayer(this));
    }

    public boolean hasLayerType(RenderLayer<?,?> renderLayer) {
        return this.layers.stream().anyMatch(layer -> layer.getClass() == renderLayer.getClass());
    }

    @Override
    public void render(AbstractClientPlayer entityIn, float pEntityYaw, float pPartialTicks, PoseStack matrixStackIn, MultiBufferSource pBuffer, int pPackedLight) {

        VRPlayersClient.RotInfo playermodelcontroller$rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(entityIn.getUUID());

        if (playermodelcontroller$rotinfo != null) {
            matrixStackIn.scale(playermodelcontroller$rotinfo.heightScale, playermodelcontroller$rotinfo.heightScale, playermodelcontroller$rotinfo.heightScale);
            super.render(entityIn, pEntityYaw, pPartialTicks, matrixStackIn, pBuffer, pPackedLight);
            matrixStackIn.scale(1.0F, 1.0F / playermodelcontroller$rotinfo.heightScale, 1.0F);
        }
    }

    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer pEntity, float pPartialTicks) {
        //idk why we do this anymore
        return pEntity.isVisuallySwimming() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
        // return pEntity.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(pEntity, pPartialTicks);
    }

    @Override
    public void setModelProperties(AbstractClientPlayer pClientPlayer) {
        super.setModelProperties(pClientPlayer);

        this.getModel().crouching &= !pClientPlayer.isVisuallySwimming();

        if (pClientPlayer == Minecraft.getInstance().player && this.getModel() instanceof VRPlayerModel_WithArms<?> armsModel && ClientDataHolderVR.getInstance().currentPass == RenderPass.CAMERA && ClientDataHolderVR.getInstance().cameraTracker.isQuickMode() && ClientDataHolderVR.getInstance().grabScreenShot) {
            // player hands block the camera, so disable them for the screenshot
            armsModel.leftHand.visible = false;
            armsModel.rightHand.visible = false;
            armsModel.leftSleeve.visible = false;
            armsModel.rightSleeve.visible = false;
        }
    }

    @Override
    protected void setupRotations(AbstractClientPlayer pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, float entityScale) {
        UUID uuid = pEntityLiving.getUUID();
        if (ClientDataHolderVR.getInstance().currentPass != RenderPass.GUI && VRPlayersClient.getInstance().isTracked(uuid)) {
            VRPlayersClient.RotInfo playermodelcontroller$rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(uuid);
            pRotationYaw = (float) Math.toDegrees(playermodelcontroller$rotinfo.getBodyYawRadians());
        }

        //vanilla below here
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks, entityScale);
    }
}
