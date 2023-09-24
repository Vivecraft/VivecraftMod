package org.vivecraft.client.render;

import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.VRPlayersClient.RotInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static org.joml.Math.*;

public class VRPlayerModel<T extends LivingEntity> extends PlayerModel<T>
{
    private final boolean slim;
    public ModelPart vrHMD;
    RotInfo rotInfo;
    private boolean laying;
    
    public VRPlayerModel(ModelPart modelPart, boolean isSlim)
    {
        super(modelPart, isSlim);
        this.slim = isSlim;
        this.vrHMD = modelPart.getChild("vrHMD");
    }
    
    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim)
    {
        MeshDefinition meshdefinition = PlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("vrHMD", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -6.0F, -7.5F, 7.0F, 4.0F, 5.0F, cubeDeformation), PartPose.ZERO);
        return meshdefinition;
    }
    

    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        this.rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(((Player)pEntity).getUUID());
        RotInfo rotinfo = VRPlayersClient.getInstance().getRotationsForPlayer(((Player)pEntity).getUUID());

        if (rotinfo == null) return; //how

        double d0 = -1.501F * rotinfo.heightScale;
        float f = toRadians(pEntity.getYRot());
        float f1 = (float)atan2(-rotinfo.headRot.x, -rotinfo.headRot.z);
        float f2 = (float)asin(rotinfo.headRot.y / rotinfo.headRot.length());
        double d1 = rotinfo.getBodyYawRadians();
        this.head.xRot = -f2;
        this.head.yRot = (float)(PI - (double)f1 - d1);
        this.laying = this.swimAmount > 0.0F || pEntity.isFallFlying() && !pEntity.isAutoSpinAttack();

        if (this.laying)
        {
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = -4.0F;
            this.head.xRot = (float)(this.head.xRot - (PI / 2.0D));
        } else if (this.crouching) {
            // move head down when crouching
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = 4.2F;
        }
        else
        {
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = 0.0F;
        }

        this.vrHMD.visible = true;

        this.vrHMD.copyFrom(this.head);
        this.hat.copyFrom(this.head);
    }

    public void renderHMDR(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int noOverlay) {
        this.vrHMD.render(poseStack, vertexConsumer, i, noOverlay);
    }
}
