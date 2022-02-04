package org.vivecraft.render;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class VRPlayerModel<T extends LivingEntity> extends PlayerModel<T>
{
    private final boolean slim;
    public ModelPart vrHMD;
    ResourceLocation DIAMOND_HMD = new ResourceLocation("vivecraft:textures/diamond_hmd.png");
    ResourceLocation GOLD_HMD = new ResourceLocation("vivecraft:textures/gold_hmd.png");
    ResourceLocation BLACK_HMD = new ResourceLocation("vivecraft:textures/black_hmd.png");
    PlayerModelController.RotInfo rotInfo;
    private boolean laying;
    private final List<ModelPart> parts;
    
    public VRPlayerModel(ModelPart p_170821_, boolean p_170822_)
    {
        super(p_170821_, p_170822_);
        this.slim = p_170822_;
        this.vrHMD = p_170821_.getChild("vrHMD");
//        this.vrHMD.setTextureSize(16, 16);
//        this.vrHMD.setTextureLocation(BLACK_HMD);       
        this.parts = p_170821_.getAllParts().filter((p_170824_) ->
        {
            return !p_170824_.isEmpty();
        }).collect(ImmutableList.toImmutableList());
    }
    
    public static MeshDefinition createMesh(CubeDeformation p_170826_, boolean p_170827_)
    {
    	MeshDefinition meshdefinition = PlayerModel.createMesh(p_170826_, false);
    	PartDefinition partdefinition = meshdefinition.getRoot();
    	partdefinition.addOrReplaceChild("vrHMD", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -6.0F, -7.5F, 7.0F, 4.0F, 5.0F, p_170826_), PartPose.ZERO);
    	return meshdefinition;
    }
    

    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
    	super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
    	this.rotInfo = PlayerModelController.getInstance().getRotationsForPlayer(((Player)pEntity).getUUID());
    	PlayerModelController.RotInfo rotinfo = PlayerModelController.getInstance().getRotationsForPlayer(((Player)pEntity).getUUID());

    	if (rotinfo == null) return; //how

    	double d0 = (double)(-1.501F * rotinfo.heightScale);
    	float f = (float)Math.toRadians((double)pEntity.getYRot());
        float f1 = (float)Math.atan2(-rotinfo.headRot.x, -rotinfo.headRot.z);
        float f2 = (float)Math.asin(rotinfo.headRot.y / rotinfo.headRot.length());
    	double d1 = rotinfo.getBodyYawRadians();
    	this.head.xRot = -f2;
    	this.head.yRot = (float)(Math.PI - (double)f1 - d1);
    	this.laying = this.swimAmount > 0.0F || pEntity.isFallFlying() && !pEntity.isAutoSpinAttack();

    	if (this.laying)
    	{
    		this.head.z = 0.0F;
    		this.head.x = 0.0F;
    		this.head.y = -4.0F;
    		this.head.xRot = (float)((double)this.head.xRot - (Math.PI / 2D));
    	}
    	else
    	{
    		this.head.z = 0.0F;
    		this.head.x = 0.0F;
    		this.head.y = 0.0F;
    	}

    	this.vrHMD.visible = true;

//    	switch (rotinfo.hmd)
//    	{
//    	case 0:
//    		this.vrHMD.visible = false;
//    		break;
//
//    	case 1:
//    		this.vrHMD.setTextureLocation(this.BLACK_HMD);
//    		break;
//
//    	case 2:
//    		this.vrHMD.setTextureLocation(this.GOLD_HMD);
//    		break;
//
//    	case 3:
//    		this.vrHMD.setTextureLocation(this.DIAMOND_HMD);
//    		break;
//
//    	case 4:
//    		this.vrHMD.setTextureLocation(this.DIAMOND_HMD);
//    	}

    	this.vrHMD.copyFrom(this.head);
        this.hat.copyFrom(this.head);
    }
}
