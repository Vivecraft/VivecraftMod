package org.vivecraft.client.render;

import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.VRPlayersClient.RotInfo;
import org.vivecraft.client_vr.render.RenderPass;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import static org.joml.Math.*;

import static net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class VRPlayerRenderer extends net.minecraft.client.renderer.entity.player.PlayerRenderer
{
    static LayerDefinition VRLayerDef = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    static LayerDefinition VRLayerDef_arms = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, false), 64, 64);
    static LayerDefinition VRLayerDef_slim = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64);
    static LayerDefinition VRLayerDef_arms_slim = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, true), 64, 64);

    public VRPlayerRenderer(Context context, boolean slim, boolean seated) {
        super(context, slim);
        this.model = (seated ?
            new VRPlayerModel<>(slim ? VRLayerDef_slim.bakeRoot() : VRLayerDef.bakeRoot(), slim) :
            new VRPlayerModel_WithArms<>(slim ? VRLayerDef_arms_slim.bakeRoot() : VRLayerDef_arms.bakeRoot(), slim)
        );

        this.addLayer(new HMDLayer(this));
    }

    @Override
    public void render(AbstractClientPlayer entityIn, float pEntityYaw, float pPartialTicks, PoseStack matrixStackIn, MultiBufferSource pBuffer, int pPackedLight)
    {

        RotInfo playermodelcontroller$rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(entityIn.getUUID());

        if (playermodelcontroller$rotinfo != null)
        {
            matrixStackIn.scale(playermodelcontroller$rotinfo.heightScale, playermodelcontroller$rotinfo.heightScale, playermodelcontroller$rotinfo.heightScale);
            super.render(entityIn, pEntityYaw, pPartialTicks, matrixStackIn, pBuffer, pPackedLight);
            matrixStackIn.scale(1.0F, 1.0F / playermodelcontroller$rotinfo.heightScale, 1.0F);
        }
    }

    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer pEntity, float pPartialTicks)
    {
        //idk why we do this anymore
        return pEntity.isVisuallySwimming() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
       // return pEntity.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(pEntity, pPartialTicks);
    }

    @Override
    public void setModelProperties(AbstractClientPlayer pClientPlayer)
    {
        super.setModelProperties(pClientPlayer);

        this.getModel().crouching &= !pClientPlayer.isVisuallySwimming();

        if (pClientPlayer == mc.player && this.getModel() instanceof VRPlayerModel_WithArms<?> armsModel && dh.currentPass == RenderPass.CAMERA && dh.cameraTracker.isQuickMode() && dh.grabScreenShot) {
            // player hands block the camera, so disable them for the screenshot
            armsModel.leftHand.visible = false;
            armsModel.rightHand.visible = false;
            armsModel.leftSleeve.visible = false;
            armsModel.rightSleeve.visible = false;
        }
    }

    @Override
    protected void setupRotations(AbstractClientPlayer pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
    {
        UUID uuid = pEntityLiving.getUUID();
        if (dh.currentPass != RenderPass.GUI && VRPlayersClient.getInstance().isTracked(uuid))
        {
            RotInfo playermodelcontroller$rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(uuid);
            pRotationYaw = (float)toDegrees(playermodelcontroller$rotinfo.getBodyYawRadians());
        }

        //vanilla below here
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
    }
}
