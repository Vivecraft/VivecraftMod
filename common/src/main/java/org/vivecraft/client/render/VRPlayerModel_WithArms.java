package org.vivecraft.client.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Polygon;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.Xplat;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;

public class VRPlayerModel_WithArms<T extends LivingEntity> extends VRPlayerModel<T> {
    public ModelPart leftShoulder;
    public ModelPart rightShoulder;
    public ModelPart leftShoulder_sleeve;
    public ModelPart rightShoulder_sleeve;
    public ModelPart leftHand;
    public ModelPart rightHand;

    public VRPlayerModel_WithArms(ModelPart modelPart, boolean isSlim) {
        super(modelPart, isSlim);
        // use left/right arm as shoulders
        this.leftShoulder = modelPart.getChild("left_arm");
        this.rightShoulder = modelPart.getChild("right_arm");
        this.leftShoulder_sleeve = modelPart.getChild("leftShoulder_sleeve");
        this.rightShoulder_sleeve = modelPart.getChild("rightShoulder_sleeve");
        this.rightHand = modelPart.getChild("rightHand");
        this.leftHand = modelPart.getChild("leftHand");


        //finger hax
        // some mods remove the base parts
        if (!this.leftShoulder.cubes.isEmpty()) {
            copyUV(this.leftShoulder.cubes.get(0).polygons[1], this.leftHand.cubes.get(0).polygons[1]);
            copyUV(this.leftShoulder.cubes.get(0).polygons[1], this.leftHand.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(this.leftShoulder, this.leftHand, 3, 3);
                SodiumHelper.copyModelCuboidUV(this.leftShoulder, this.leftHand, 3, 2);
            }
        }
        if (!this.rightShoulder.cubes.isEmpty()) {
            copyUV(this.rightShoulder.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[1]);
            copyUV(this.rightShoulder.cubes.get(0).polygons[1], this.rightHand.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(this.rightShoulder, this.rightHand, 3, 3);
                SodiumHelper.copyModelCuboidUV(this.rightShoulder, this.rightHand, 3, 2);
            }
        }

        if (!this.rightSleeve.cubes.isEmpty()) {
            copyUV(this.rightShoulder_sleeve.cubes.get(0).polygons[1], this.rightSleeve.cubes.get(0).polygons[1]);
            copyUV(this.rightShoulder_sleeve.cubes.get(0).polygons[1], this.rightSleeve.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(this.rightShoulder_sleeve, this.rightSleeve, 3, 3);
                SodiumHelper.copyModelCuboidUV(this.rightShoulder_sleeve, this.rightSleeve, 3, 2);
            }
        }
        if (!this.leftSleeve.cubes.isEmpty()) {
            copyUV(this.leftShoulder_sleeve.cubes.get(0).polygons[1], this.leftSleeve.cubes.get(0).polygons[1]);
            copyUV(this.leftShoulder_sleeve.cubes.get(0).polygons[1], this.leftSleeve.cubes.get(0).polygons[0]);
            if (SodiumHelper.isLoaded()) {
                SodiumHelper.copyModelCuboidUV(this.leftShoulder_sleeve, this.leftSleeve, 3, 3);
                SodiumHelper.copyModelCuboidUV(this.leftShoulder_sleeve, this.leftSleeve, 3, 2);
            }
        }
    }

    private void copyUV(Polygon source, Polygon dest) {
        for (int i = 0; i < source.vertices.length; i++) {
            Vertex newVertex = new Vertex(dest.vertices[i].pos, source.vertices[i].u, source.vertices[i].v);
            if (OptifineHelper.isOptifineLoaded()) {
                OptifineHelper.copyRenderPositions(dest.vertices[i], newVertex);
            }
            dest.vertices[i] = newVertex;
        }
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition meshDefinition = VRPlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition partDefinition = meshDefinition.getRoot();

        if (slim) {
            partDefinition.addOrReplaceChild("leftHand",
                CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 55)
                    .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("rightHand",
                CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 39)
                    .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("leftShoulder_sleeve", CubeListBuilder.create().texOffs(48, 48)
                    .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("rightShoulder_sleeve", CubeListBuilder.create().texOffs(40, 32)
                    .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
        } else {
            partDefinition.addOrReplaceChild("leftHand",
                CubeListBuilder.create().texOffs(32, 55).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 55)
                    .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("rightHand",
                CubeListBuilder.create().texOffs(40, 23).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 39)
                    .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("leftShoulder_sleeve", CubeListBuilder.create().texOffs(48, 48)
                    .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            partDefinition.addOrReplaceChild("rightShoulder_sleeve", CubeListBuilder.create().texOffs(40, 32)
                    .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
        }
        return meshDefinition;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.jacket, this.hat,
            this.leftHand, this.rightHand, this.leftSleeve, this.rightSleeve,
            this.leftShoulder, this.rightShoulder, this.leftShoulder_sleeve, this.rightShoulder_sleeve,
            this.leftLeg, this.rightLeg, this.leftPants, this.rightPants);
    }

    @Override
    public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (this.rotInfo == null) {
            return;
        }

        double handsYOffset = -1.501F * this.rotInfo.heightScale;

        float leftControllerYaw = (float) Math.atan2(-this.rotInfo.leftArmRot.x, -this.rotInfo.leftArmRot.z);
        float leftControllerPitch = (float) Math.asin(this.rotInfo.leftArmRot.y / this.rotInfo.leftArmRot.length());
        float rightControllerYaw = (float) Math.atan2(-this.rotInfo.rightArmRot.x, -this.rotInfo.rightArmRot.z);
        float rightControllerPitch = (float) Math.asin(this.rotInfo.rightArmRot.y / this.rotInfo.rightArmRot.length());
        double bodyYaw = this.rotInfo.getBodyYawRadians();

        this.laying = this.swimAmount > 0.0F || player.isFallFlying() && !player.isAutoSpinAttack();

        if (!this.rotInfo.reverse) {
            this.rightShoulder.setPos(-Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, Mth.sin(this.body.yRot) * 5.0F);
            this.leftShoulder.setPos(Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, -Mth.sin(this.body.yRot) * 5.0F);
        } else {
            this.leftShoulder.setPos(-Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, Mth.sin(this.body.yRot) * 5.0F);
            this.rightShoulder.setPos(Mth.cos(this.body.yRot) * 5.0F, this.slim ? 2.5F : 2.0F, -Mth.sin(this.body.yRot) * 5.0F);
        }

        if (this.crouching) {
            this.rightShoulder.y += 3.2F;
            this.leftShoulder.y += 3.2F;
        }

        Vec3 leftArmPos = this.rotInfo.leftArmPos;
        Vec3 rightArmPos = this.rotInfo.rightArmPos;
        if (Xplat.isModLoaded("pehkui")) {
            // remove pehkui scale from that, since the whole entity is scaled
            leftArmPos = leftArmPos.scale(1.0F / PehkuiHelper.getPlayerScale(player, Minecraft.getInstance().getFrameTime()));
            rightArmPos = rightArmPos.scale(1.0F / PehkuiHelper.getPlayerScale(player, Minecraft.getInstance().getFrameTime()));
        }

        // Left Arm
        leftArmPos = leftArmPos.add(0.0D, handsYOffset, 0.0D);
        leftArmPos = leftArmPos.yRot((float) (-Math.PI + bodyYaw));
        leftArmPos = leftArmPos.scale(16.0F / this.rotInfo.heightScale);
        this.leftHand.setPos((float) -leftArmPos.x, (float) -leftArmPos.y, (float) leftArmPos.z);
        this.leftHand.xRot = -leftControllerPitch + (float) Math.PI * 1.5F;
        this.leftHand.yRot = (float) (Math.PI - (double) leftControllerYaw - bodyYaw);
        this.leftHand.zRot = 0.0F;
        if (this.leftArmPose == ArmPose.THROW_SPEAR) {
            this.leftHand.xRot =  this.leftHand.xRot - (float) Math.PI * 0.5F;
        }

        // left shoulder
        Vec3 leftShoulderPos = new Vec3(
            this.leftShoulder.x + leftArmPos.x,
            this.leftShoulder.y + leftArmPos.y,
            this.leftShoulder.z - leftArmPos.z);

        float leftShoulderYaw = (float) Math.atan2(leftShoulderPos.x, leftShoulderPos.z);
        float leftShoulderPitch = (float) ((Math.PI * 1.5D) - Math.asin(leftShoulderPos.y / leftShoulderPos.length()));
        this.leftShoulder.zRot = 0.0F;
        this.leftShoulder.xRot = leftShoulderPitch;
        this.leftShoulder.yRot = leftShoulderYaw;
        if (this.leftShoulder.yRot > 0.0F) {
            this.leftShoulder.yRot = 0.0F;
        }

        // Right arm
        rightArmPos = rightArmPos.add(0.0D, handsYOffset, 0.0D);
        rightArmPos = rightArmPos.yRot((float) (-Math.PI + bodyYaw));
        rightArmPos = rightArmPos.scale(16.0F / this.rotInfo.heightScale);
        this.rightHand.setPos((float) -rightArmPos.x, (float) -rightArmPos.y, (float) rightArmPos.z);
        this.rightHand.xRot = -rightControllerPitch + (float) Math.PI * 1.5F;
        this.rightHand.yRot = (float) (Math.PI - (double) rightControllerYaw - bodyYaw);
        this.rightHand.zRot = 0.0F;
        if (this.rightArmPose == ArmPose.THROW_SPEAR) {
            this.rightHand.xRot = this.rightHand.xRot - (float) Math.PI * 0.5F;
        }

        // Right shoulder
        Vec3 rightShoulderPos = new Vec3(
            this.rightShoulder.x + rightArmPos.x,
            this.rightShoulder.y + rightArmPos.y,
            this.rightShoulder.z - rightArmPos.z);

        float rightShoulderYaw = (float) Math.atan2(rightShoulderPos.x, rightShoulderPos.z);
        float rightShoulderPitch = (float) ((Math.PI * 1.5D) - Math.asin(rightShoulderPos.y / rightShoulderPos.length()));
        this.rightShoulder.zRot = 0.0F;
        this.rightShoulder.xRot = rightShoulderPitch;
        this.rightShoulder.yRot = rightShoulderYaw;
        if (this.rightShoulder.yRot < 0.0F) {
            this.rightShoulder.yRot = 0.0F;
        }

        if (this.laying) {
            this.rightShoulder.xRot = this.rightShoulder.xRot - (float) Math.PI * 0.5F;
            this.leftShoulder.xRot = this.leftShoulder.xRot - (float) Math.PI * 0.5F;
        }

        this.leftSleeve.copyFrom(this.leftHand);
        this.rightSleeve.copyFrom(this.rightHand);
        this.leftShoulder_sleeve.copyFrom(this.leftShoulder);
        this.rightShoulder_sleeve.copyFrom(this.rightShoulder);
        this.leftShoulder_sleeve.visible = this.leftSleeve.visible;
        this.rightShoulder_sleeve.visible = this.rightSleeve.visible;
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);

        this.rightShoulder.visible = visible;
        this.leftShoulder.visible = visible;
        this.rightShoulder_sleeve.visible = visible;
        this.leftShoulder_sleeve.visible = visible;
        this.rightHand.visible = visible;
        this.leftHand.visible = visible;
    }

    @Override
    protected ModelPart getArm(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.leftHand : this.rightHand;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        ModelPart modelpart = this.getArm(side);

        if (this.laying) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }

        modelpart.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.XP.rotation((float) Math.sin((double) this.attackTime * Math.PI)));
        poseStack.translate(0.0D, -0.5D, 0.0D);
    }
}
