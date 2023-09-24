package org.vivecraft.client.render;

import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.VRPlayersClient.RotInfo;
import org.vivecraft.client.Xplat;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

import com.google.common.collect.ImmutableList;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Polygon;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.mc;

import static org.joml.Math.*;

public class VRPlayerModel_WithArms<T extends LivingEntity> extends VRPlayerModel<T>
{
    private final boolean slim;
    public ModelPart leftShoulder;
    public ModelPart rightShoulder;
    public ModelPart leftShoulder_sleeve;
    public ModelPart rightShoulder_sleeve;
    public ModelPart leftHand;
    public ModelPart rightHand;
    RotInfo rotInfo;
    private boolean laying;

    public VRPlayerModel_WithArms(ModelPart modelPart, boolean isSlim)
    {
        super(modelPart, isSlim);
        this.slim = isSlim;
        // use left/right arm as shoulders
        this.leftShoulder = modelPart.getChild("left_arm");
        this.rightShoulder = modelPart.getChild("right_arm");
        this.leftShoulder_sleeve = modelPart.getChild("leftShoulder_sleeve");
        this.rightShoulder_sleeve = modelPart.getChild("rightShoulder_sleeve");
        this.rightHand = modelPart.getChild("rightHand");
        this.leftHand = modelPart.getChild("leftHand");


        //finger hax
        // some mods remove the base parts
        if (!leftShoulder.cubes.isEmpty()) {
            copyUV(leftShoulder.cubes.get(0).polygons[1], leftHand.cubes.get(0).polygons[1]);
            copyUV(leftShoulder.cubes.get(0).polygons[1], leftHand.cubes.get(0).polygons[0]);
        }
        if (!rightShoulder.cubes.isEmpty()) {
            copyUV(rightShoulder.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[1]);
            copyUV(rightShoulder.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[0]);
        }

        if (!rightSleeve.cubes.isEmpty()) {
            copyUV(rightShoulder_sleeve.cubes.get(0).polygons[1], this.rightSleeve.cubes.get(0).polygons[1]);
            copyUV(rightShoulder_sleeve.cubes.get(0).polygons[1], this.rightSleeve.cubes.get(0).polygons[0]);
        }
        if (!leftSleeve.cubes.isEmpty()) {
            copyUV(leftShoulder_sleeve.cubes.get(0).polygons[1], leftSleeve.cubes.get(0).polygons[1]);
            copyUV(leftShoulder_sleeve.cubes.get(0).polygons[1], leftSleeve.cubes.get(0).polygons[0]);
        }
    }

    private void copyUV(Polygon source, Polygon dest) {
        for (int i = 0; i < source.vertices.length; i++) {
            dest.vertices[i] = new Vertex(dest.vertices[i].pos, source.vertices[i].u, source.vertices[i].v);
            if (OptifineHelper.isOptifineLoaded()) {
                OptifineHelper.copyRenderPositions(source.vertices[i], dest.vertices[i]);
            }
        }
    }

    public static MeshDefinition createMesh(CubeDeformation p_170826_, boolean p_170827_)
    {
        MeshDefinition meshdefinition = VRPlayerModel.createMesh(p_170826_, p_170827_);
        PartDefinition partdefinition = meshdefinition.getRoot();

        if(p_170827_) {
            partdefinition.addOrReplaceChild("leftHand", CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 55).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("rightHand", CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 39).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("leftShoulder_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("rightShoulder_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
        }else {
            partdefinition.addOrReplaceChild("leftHand", CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 55).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("rightHand", CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 39).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("leftShoulder_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("rightShoulder_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));

        }
        return meshdefinition;
    }


    protected Iterable<ModelPart> bodyParts()
    {
        return ImmutableList.of(this.body, this.leftHand, this.rightHand,this.leftShoulder, this.rightShoulder,this.leftShoulder_sleeve, this.rightShoulder_sleeve,  this.rightLeg, this.leftLeg, this.hat, this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket);
    }

    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        this.rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(((Player)pEntity).getUUID());
        RotInfo rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(((Player)pEntity).getUUID());

        if (rotinfo == null) return;

        double d0 = -1.501F * rotinfo.heightScale;
        float f = toRadians(pEntity.getYRot());
        float f1 = (float)atan2(-rotinfo.headRot.x, -rotinfo.headRot.z);
        float f2 = (float)asin(rotinfo.headRot.y / rotinfo.headRot.length());
        float f3 = (float)atan2(-rotinfo.leftArmRot.x, -rotinfo.leftArmRot.z);
        float f4 = (float)asin(rotinfo.leftArmRot.y / rotinfo.leftArmRot.length());
        float f5 = (float)atan2(-rotinfo.rightArmRot.x, -rotinfo.rightArmRot.z);
        float f6 = (float)asin(rotinfo.rightArmRot.y / rotinfo.rightArmRot.length());
        double d1 = rotinfo.getBodyYawRadians();

        this.laying = this.swimAmount > 0.0F || pEntity.isFallFlying() && !pEntity.isAutoSpinAttack();

        if (!rotinfo.reverse)
        {
			this.rightShoulder.setPos(-cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, sin(this.body.yRot) * 5.0F);
			this.leftShoulder.setPos(cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, -sin(this.body.yRot) * 5.0F);
        }
        else
        {
			this.leftShoulder.setPos(-cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, sin(this.body.yRot) * 5.0F);
			this.rightShoulder.setPos(cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, -sin(this.body.yRot) * 5.0F);
        }

        if (this.crouching)
        {
            this.rightShoulder.y += 3.2F;
            this.leftShoulder.y += 3.2F;
        }

        Vec3 vec3 = rotinfo.leftArmPos;
        Vec3 vec32 = rotinfo.rightArmPos;
        if (Xplat.isModLoaded("pehkui")){
            // remove pehkui scale from that, since the whole entity is scaled
			vec3 = vec3.scale(1.0F / PehkuiHelper.getPlayerScale(pEntity, mc.getFrameTime()));
			vec32 = vec32.scale(1.0F / PehkuiHelper.getPlayerScale(pEntity, mc.getFrameTime()));
        }
		vec3 = vec3.add(0.0D, d0, 0.0D);
		vec3 = vec3.yRot((float)(-PI + d1));
		vec3 = vec3.scale((double)(16.0F / rotinfo.heightScale));
        this.leftHand.setPos((float)(-vec3.x), (float)(-vec3.y), (float)vec3.z);
        this.leftHand.xRot = (float)((-f4) + (PI * 1.5D));
        this.leftHand.yRot = (float)(PI - f3 - d1);
        this.leftHand.zRot = 0.0F;


		Vec3 vec31 = new Vec3((double)this.leftShoulder.x + vec3.x, (double)this.leftShoulder.y + vec3.y, (double)this.leftShoulder.z - vec3.z);
        float f7 = (float)atan2(vec31.x, vec31.z);
        float f8 = (float)((PI * 1.5D) - asin(vec31.y / vec31.length()));
        this.leftShoulder.zRot = 0.0F;
        this.leftShoulder.xRot = f8;
        this.leftShoulder.yRot = f7;

        if (this.leftShoulder.yRot > 0.0F)
        {
            this.leftShoulder.yRot = 0.0F;
        }

        if (this.leftArmPose == ArmPose.THROW_SPEAR)
        {
            this.leftHand.xRot = (float) (this.leftHand.xRot - (PI / 2.0D));
        }

        vec32 = vec32.add(0.0D, d0, 0.0D);
		vec32 = vec32.yRot((float)(-PI + d1));
		vec32 = vec32.scale((double)(16.0F / rotinfo.heightScale));
        this.rightHand.setPos((float)(-vec32.x), -((float)vec32.y), (float)vec32.z);
        this.rightHand.xRot = (float)((-f6) + (PI * 1.5D));
        this.rightHand.yRot = (float)(PI - f5 - d1);
        this.rightHand.zRot = 0.0F;

		Vec3 vec33 = new Vec3((double)this.rightShoulder.x + vec32.x, (double)this.rightShoulder.y + vec32.y, (double)this.rightShoulder.z - vec32.z);
        float f9 = (float)atan2(vec33.x, vec33.z);
        float f10 = (float)((PI * 1.5D) - asin(vec33.y / vec33.length()));
        this.rightShoulder.zRot = 0.0F;
        this.rightShoulder.xRot = f10;
        this.rightShoulder.yRot = f9;

        if (this.rightShoulder.yRot < 0.0F)
        {
            this.rightShoulder.yRot = 0.0F;
        }

        if (this.rightArmPose == ArmPose.THROW_SPEAR) {
            this.rightHand.xRot = (float) (this.rightHand.xRot - (PI / 2.0D));
        }

        if (this.laying)
        {
            this.rightShoulder.xRot = (float)(this.rightShoulder.xRot - (PI / 2.0D));
            this.leftShoulder.xRot = (float)(this.leftShoulder.xRot - (PI / 2.0D));
        }

        this.leftSleeve.copyFrom(this.leftHand);
        this.rightSleeve.copyFrom(this.rightHand);
        this.leftShoulder_sleeve.copyFrom(this.leftShoulder);
        this.rightShoulder_sleeve.copyFrom(this.rightShoulder);
        this.leftShoulder_sleeve.visible = this.leftSleeve.visible;
        this.rightShoulder_sleeve.visible = this.rightSleeve.visible;
    }

    public void setAllVisible(boolean pVisible)
    {
        super.setAllVisible(pVisible);

        this.rightShoulder.visible = pVisible;
        this.leftShoulder.visible = pVisible;
        this.rightShoulder_sleeve.visible = pVisible;
        this.leftShoulder_sleeve.visible = pVisible;
        this.rightHand.visible = pVisible;
        this.leftHand.visible = pVisible;

    }

    protected ModelPart getArm(HumanoidArm pSide)
    {
        return pSide == HumanoidArm.LEFT ? this.leftHand : this.rightHand;
    }

    public void translateToHand(HumanoidArm pSide, PoseStack pMatrixStack)
    {
        ModelPart modelpart = this.getArm(pSide);

        if (this.laying)
        {
            float ang = toRadians(-90.0F);
            pMatrixStack.last().pose().rotateX(ang);
            pMatrixStack.last().normal().rotateX(ang);
        }

        modelpart.translateAndRotate(pMatrixStack);
        float ang = sin((float) PI * this.attackTime);
        pMatrixStack.last().normal().rotateX(ang);
        pMatrixStack.last().pose().rotateX(ang).translate(0.0F, -0.5F, 0.0F);
    }

//	public void renderToBuffer(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
//	{
//		this.body.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.jacket.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.leftLeg.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.rightLeg.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.leftPants.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.rightPants.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		pMatrixStack.pushPose();
//		this.head.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.hat.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.vrHMD.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//
//		if (this.seated)
//		{
//			this.leftArm.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//			this.rightArm.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		}
//		else
//		{
//			this.leftShoulder.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//			this.rightShoulder.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//
//			if (this.laying)
//			{
//				float ang = toRadians(-90.0F);
//				pMatrixStack.last().pose().rotateX(ang);
// 				pMatrixStack.last().normal().rotateX(ang);
//			}
//
//			this.rightHand.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//			this.leftHand.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		}
//
//		this.leftSleeve.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		this.rightSleeve.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
//		pMatrixStack.popPose();
//	}
}
