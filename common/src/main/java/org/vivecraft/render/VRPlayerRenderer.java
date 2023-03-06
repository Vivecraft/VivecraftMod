package org.vivecraft.render;

import java.util.UUID;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.vivecraft.ClientDataHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;

public class VRPlayerRenderer extends PlayerRenderer
{
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

    @Override
    public void render(AbstractClientPlayer entityIn, float pEntityYaw, float pPartialTicks, PoseStack matrixStackIn, MultiBufferSource pBuffer, int pPackedLight)
    {

        PlayerModelController.RotInfo playermodelcontroller$rotinfo = PlayerModelController.getInstance().getRotationsForPlayer(entityIn.getUUID());

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

        VRPlayerModel<AbstractClientPlayer> playermodel = (VRPlayerModel<AbstractClientPlayer>) this.getModel();
        playermodel.crouching &= !pClientPlayer.isVisuallySwimming();
    }

    @Override
    protected void setupRotations(AbstractClientPlayer pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
    {
    	UUID uuid = pEntityLiving.getUUID();
    	if (ClientDataHolder.getInstance().currentPass != RenderPass.GUI && PlayerModelController.getInstance().isTracked(uuid))
    	{
    		PlayerModelController.RotInfo playermodelcontroller$rotinfo = PlayerModelController.getInstance().getRotationsForPlayer(uuid);
    		pRotationYaw = (float)Math.toDegrees(playermodelcontroller$rotinfo.getBodyYawRadians());
    	}

        //vanilla below here
        super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
    }
}
