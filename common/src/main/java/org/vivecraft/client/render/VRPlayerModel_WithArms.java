package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.render.models.HandModel;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client.utils.ModelUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.settings.VRSettings;

public class VRPlayerModel_WithArms extends VRPlayerModel implements HandModel {
    public static final int LOWER_EXTENSION = 2;
    public static final int UPPER_EXTENSION = 3;

    // shoulders use the vanilla arm parts
    public ModelPart leftHand;
    public ModelPart rightHand;
    public ModelPart leftHandSleeve;
    public ModelPart rightHandSleeve;

    private final Vector3f jointOffset = new Vector3f();

    public VRPlayerModel_WithArms(ModelPart root, boolean isSlim) {
        super(root, isSlim);
        this.leftHand = root.getChild("left_hand");
        this.rightHand = root.getChild("right_hand");
        this.leftHandSleeve = this.leftHand.getChild("left_hand_sleeve");
        this.rightHandSleeve = this.rightHand.getChild("right_hand_sleeve");

        // copy textures
        ModelUtils.textureHack(this.leftArm, this.leftHand);
        ModelUtils.textureHack(this.rightArm, this.rightHand);
        ModelUtils.textureHack(this.leftSleeve, this.leftHandSleeve);
        ModelUtils.textureHack(this.rightSleeve, this.rightHandSleeve);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition meshDefinition = VRPlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition partDefinition = meshDefinition.getRoot();
        boolean connected = ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected;
        int upperExtension = connected ? UPPER_EXTENSION : 0;
        int lowerExtension = connected ? LOWER_EXTENSION : 0;
        float lowerShrinkage = connected ? -0.05F : 0F;

        if (slim) {
            PartDefinition leftHand = partDefinition.addOrReplaceChild("left_hand", CubeListBuilder.create()
                    .texOffs(32, 55 - lowerExtension)
                    .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(lowerShrinkage)),
                PartPose.offset(5.5F, 12.0F, 0.0F));
            leftHand.addOrReplaceChild("left_hand_sleeve", CubeListBuilder.create()
                    .texOffs(48, 55 - lowerExtension)
                    .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(0.25f + lowerShrinkage)),
                PartPose.ZERO);
            PartDefinition rightHand = partDefinition.addOrReplaceChild("right_hand", CubeListBuilder.create()
                    .texOffs(40, 23 - lowerExtension)
                    .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(lowerShrinkage)),
                PartPose.offset(-5.5F, 12.0F, 0.0F));
            rightHand.addOrReplaceChild("right_hand_sleeve", CubeListBuilder.create()
                    .texOffs(40, 39 - lowerExtension)
                    .addBox(-1.5F, -5.0F - lowerExtension, -2.0F, 3.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(0.25f + lowerShrinkage)),
                PartPose.ZERO);
            PartDefinition leftArm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                    .texOffs(32, 48)
                    .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                PartPose.offset(5.0F, 2.0F, 0.0F));
            leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                    .texOffs(48, 48)
                    .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.ZERO);
            PartDefinition rightArm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                    .texOffs(40, 16)
                    .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
            rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                    .texOffs(40, 32)
                    .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.ZERO);
        } else {
            PartDefinition leftHand = partDefinition.addOrReplaceChild("left_hand", CubeListBuilder.create()
                    .texOffs(32, 55 - lowerExtension)
                    .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(lowerShrinkage)),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            leftHand.addOrReplaceChild("left_hand_sleeve", CubeListBuilder.create()
                    .texOffs(48, 55 - lowerExtension)
                    .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(0.25f + lowerShrinkage)),
                PartPose.ZERO);
            PartDefinition rightHand = partDefinition.addOrReplaceChild("right_hand", CubeListBuilder.create()
                    .texOffs(40, 23 - lowerExtension)
                    .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(lowerShrinkage)),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            rightHand.addOrReplaceChild("right_hand_sleeve", CubeListBuilder.create()
                    .texOffs(40, 39 - lowerExtension)
                    .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                        cubeDeformation.extend(0.25f + lowerShrinkage)),
                PartPose.ZERO);
            PartDefinition leftArm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                    .texOffs(32, 48)
                    .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                PartPose.offset(5.0F, 2.5F, 0.0F));
            leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                    .texOffs(48, 48)
                    .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.ZERO);
            PartDefinition rightArm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                    .texOffs(40, 16)
                    .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
            rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                    .texOffs(40, 32)
                    .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation.extend(0.25f)),
                PartPose.ZERO);
        }
        return meshDefinition;
    }

    @Override
    public void setupAnim(PlayerRenderState renderState) {
        super.setupAnim(renderState);

        if (this.rotInfo == null) {
            return;
        }

        ModelPart offHand = this.rotInfo.leftHanded ? this.rightHand : this.leftHand;
        ModelPart mainHand = this.rotInfo.leftHanded ? this.leftHand : this.rightHand;
        ModelPart offShoulder = this.rotInfo.leftHanded ? this.rightArm : this.leftArm;
        ModelPart mainShoulder = this.rotInfo.leftHanded ? this.leftArm : this.rightArm;

        if (this.rotInfo.offHandPos.distanceSquared(this.rotInfo.mainHandPos) > 0.0F) {
            float offset = (this.slim ? 0.5F : 1F) * this.armScale * (this.rotInfo.leftHanded ? -1F : 1F);

            // main hand
            if (ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected) {
                positionConnectedLimb(renderState, mainShoulder, mainHand, this.rotInfo.mainHandPos,
                    this.rotInfo.mainHandQuat, -offset, this.rotInfo.rightElbowPos, true, this.mainArm);
            } else {
                positionSplitLimb(renderState, mainShoulder, mainHand, this.rotInfo.mainHandPos,
                    this.rotInfo.mainHandQuat, 0F, -offset, this.rotInfo.rightElbowPos, true, this.mainArm);
            }

            // offhand
            if (ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected) {
                positionConnectedLimb(renderState, offShoulder, offHand, this.rotInfo.offHandPos,
                    this.rotInfo.offHandQuat, offset, this.rotInfo.leftElbowPos, true, this.mainArm.getOpposite());
            } else {
                positionSplitLimb(renderState, offShoulder, offHand, this.rotInfo.offHandPos,
                    this.rotInfo.offHandQuat, 0F, offset, this.rotInfo.leftElbowPos, true, this.mainArm.getOpposite());
            }

            if (this.isMainPlayer && ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
                ClientDataHolderVR.getInstance().vrSettings.modelArmsMode != VRSettings.ModelArmsMode.OFF)
            {
                // undo lay rotation
                this.tempM.rotateLocalX(this.xRot);
                GuiHandler.GUI_ROTATION_PLAYER_MODEL.set3x3(this.tempM);
                // ModelParts are rotated 90°
                GuiHandler.GUI_ROTATION_PLAYER_MODEL.rotateX(-Mth.HALF_PI);
                // undo body yaw
                GuiHandler.GUI_ROTATION_PLAYER_MODEL.rotateLocalY(-this.bodyYaw - Mth.PI);

                ModelUtils.modelToWorld(renderState, offHand.x, offHand.y, offHand.z, this.rotInfo,
                    this.bodyYaw, true, this.isMainPlayer, this.tempV);

                GuiHandler.GUI_POS_PLAYER_MODEL = Minecraft.getInstance().player.getPosition(
                        ClientUtils.getCurrentPartialTick())
                    .add(this.tempV.x, this.tempV.y, this.tempV.z);
            }
        } else {
            // align hands with shoulders, if there is no tracking data
            float offset = this.slim ? this.armScale * 0.5F : this.armScale;
            this.tempV.set(-offset, 10, 0)
                .rotateZ(mainShoulder.zRot)
                .rotateY(mainShoulder.yRot)
                .rotateX(mainShoulder.xRot);
            mainHand.copyFrom(mainShoulder);
            mainHand.x += this.tempV.x;
            mainHand.y += this.tempV.y;
            mainHand.z += this.tempV.z;

            this.tempV.set(offset, 10, 0)
                .rotateZ(offShoulder.zRot)
                .rotateY(offShoulder.yRot)
                .rotateX(offShoulder.xRot);
            offHand.copyFrom(offShoulder);
            offHand.x += this.tempV.x;
            offHand.y += this.tempV.y;
            offHand.z += this.tempV.z;

            if (this.isMainPlayer && ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
                ClientDataHolderVR.getInstance().vrSettings.modelArmsMode != VRSettings.ModelArmsMode.OFF)
            {
                GuiHandler.GUI_POS_PLAYER_MODEL = Vec3.ZERO;
            }
        }

        // first person scale
        this.leftHand.xScale = this.leftHand.zScale = this.rightHand.xScale = this.rightHand.zScale = this.armScale;

        if (this.layAmount > 0F) {
            ModelUtils.applySwimRotationOffset(renderState, this.xRot, this.tempV, this.tempV2,
                this.leftArm, this.rightArm,
                this.leftHand, this.rightHand);
        }

        if (renderState.isAutoSpinAttack) {
            spinOffset(this.leftArm, this.rightArm, this.leftHand, this.rightHand);
        }

        this.leftHandSleeve.visible = renderState.showLeftSleeve;
        this.rightHandSleeve.visible = renderState.showRightSleeve;
    }

    /**
     * positions the hand/foot and applies its rotation. also rotates the shoulder/thigh to point at the elbow/knee
     *
     * @param upper     upper body part (shoulder/thigh)
     * @param lower     lower body part (hand/foot)
     * @param lowerPos  player space position the lower body part should be at
     * @param lowerRot  direction the lower body part should face
     * @param lowerXRot additional rotation around the X axis that should be applied to the {@code lower}
     * @param jointPos  elbow/knee position, if {@code null} a middle point will be estimated
     * @param jointDown if the estimated joint should prefer up/forward or down/back
     * @param arm       arm this is positioning, to check if the swing animation should be applied
     */
    protected void positionSplitLimb(
        PlayerRenderState renderState, ModelPart upper, ModelPart lower, Vector3fc lowerPos,
        Quaternionfc lowerRot, float lowerXRot, float lowerXOffset, Vector3fc jointPos, boolean jointDown,
        HumanoidArm arm)
    {
        // place lower directly at the lower point
        ModelUtils.worldToModel(renderState, lowerPos, this.rotInfo, this.bodyYaw, this.isMainPlayer, this.tempV);
        lower.setPos(this.tempV.x, this.tempV.y, this.tempV.z);

        // joint estimation
        // point the elbow away from the hand direction
        ModelUtils.estimateJointDir(upper, lower, lowerRot, this.bodyYaw, jointDown, jointPos, renderState,
            this.rotInfo, this.isMainPlayer, this.tempV2, this.tempV);

        // get joint
        ModelUtils.estimateJoint(
            upper.x, upper.y, upper.z,
            lower.x, lower.y, lower.z,
            this.tempV2, 12.0F, this.tempV);

        // invert joint dir, use it for up in the point at
        if (jointDown) {
            this.tempV2.mul(-1F);
        }

        this.jointOffset.set(lower.x - upper.x, lower.y - upper.y, lower.z - upper.z);
        this.jointOffset.cross(this.tempV2).normalize().mul(lowerXOffset * 0.5F);
        this.tempV.add(this.jointOffset);

        // upper position and rotation
        ModelUtils.pointModelAtModelWithUp(upper, this.tempV.x, this.tempV.y, this.tempV.z,
            this.tempV2, this.tempV, this.tempM);

        this.tempM.rotateLocalX(-this.xRot);
        ModelUtils.setRotation(upper, this.tempM, this.tempV);

        // lower rotation
        ModelUtils.toModelDir(this.bodyYaw, lowerRot, this.tempM);

        if (ClientDataHolderVR.getInstance().vrSettings.playerArmAnim && arm != null && this.attackArm == arm) {
            ModelUtils.swingAnimation(lower, arm, -3F, this.attackTime, this.isMainPlayer, this.tempM,
                this.tempV, this.tempV2);
        }

        this.tempM.rotateLocalX(-this.xRot + lowerXRot);
        ModelUtils.setRotation(lower, this.tempM, this.tempV);
    }

    /**
     * positions the hand/foot and shoulder/thigh to point at the elbow/knee
     *
     * @param upper     upper body part (shoulder/thigh)
     * @param lower     lower body part (hand/foot)
     * @param lowerPos  player space position the lower body part should be at
     * @param lowerRot  direction the lower body part should face
     * @param jointPos  elbow/knee position, if {@code null} a middle point will be estimated
     * @param jointDown if the estimated joint should prefer up/forward or down/back
     * @param arm       arm this is positioning, to check if the swing animation should be applied
     */
    protected void positionConnectedLimb(
        PlayerRenderState renderState, ModelPart upper, ModelPart lower, Vector3fc lowerPos,
        Quaternionfc lowerRot, float lowerXOffset, Vector3fc jointPos, boolean jointDown, HumanoidArm arm)
    {
        // position lower
        ModelUtils.worldToModel(renderState, lowerPos, this.rotInfo, this.bodyYaw, this.isMainPlayer, this.tempV);
        float armLength = 10F;
        if (arm != null) {
            // increase arm length to the front, feels better, since human shoulders can move forward
            this.tempV.normalize(this.tempV2);
            armLength += 2F * this.tempV2.z * this.tempV2.z;
        }
        // limit length to 12, no limb stretching, for now
        this.tempV.sub(upper.x, upper.y, upper.z, this.tempV2);
        float length = this.tempV2.length();
        // move shoulders up when having the arms up, since the rotation point is slightly offset
        upper.y -= 2F * Math.min(1F, length / armLength) * Math.max(0F, -this.tempV2.y / length);
        if (ClientDataHolderVR.getInstance().vrSettings.playerLimbsLimit && length > armLength) {
            this.tempV.sub(upper.x, upper.y, upper.z);
            this.tempV.normalize().mul(armLength);
            this.tempV.add(upper.x, upper.y, upper.z);
        }
        lower.setPos(this.tempV.x, this.tempV.y, this.tempV.z);

        // point the elbow away from the hand direction
        ModelUtils.estimateJointDir(upper, lower, lowerRot, this.bodyYaw, jointDown, jointPos, renderState,
            this.rotInfo, this.isMainPlayer, this.tempV2, this.tempV);

        // get joint
        ModelUtils.estimateJoint(
            upper.x, upper.y, upper.z,
            lower.x, lower.y, lower.z,
            this.tempV2, armLength, this.tempV);

        // invert joint dir, use it for up in the point at
        if (jointDown) {
            this.tempV2.mul(-1F);
        }

        float jointX = this.tempV.x;
        float jointY = this.tempV.y;
        float jointZ = this.tempV.z;

        this.jointOffset.set(lower.x - upper.x, lower.y - upper.y, lower.z - upper.z);
        this.jointOffset.cross(this.tempV2).normalize().mul(lowerXOffset * 0.5F);

        // upper part rotation
        // dir
        this.tempV.set(jointX - upper.x, jointY - upper.y, jointZ - upper.z);
        this.tempV.add(this.jointOffset);

        ModelUtils.pointAtModel(this.tempV, this.tempV2, this.tempM);
        this.tempM.rotateLocalX(-this.xRot);
        ModelUtils.setRotation(upper, this.tempM, this.tempV);

        // lower part rotation
        // dir
        this.tempV.set(lower.x - jointX, lower.y - jointY, lower.z - jointZ);
        this.tempV.add(this.jointOffset);

        ModelUtils.pointAtModel(this.tempV, this.tempV2, this.tempM);

        if (ClientDataHolderVR.getInstance().vrSettings.playerArmAnim && arm != null && this.attackArm == arm) {
            ModelUtils.swingAnimation(lower, arm, -armLength * 0.5F, this.attackTime, this.isMainPlayer, this.tempM,
                this.tempV, this.tempV2);
        }

        this.tempM.rotateLocalX(-this.xRot);
        ModelUtils.setRotation(lower, this.tempM, this.tempV);
    }

    @Override
    public void copyPropertiesTo(HumanoidModel model) {
        super.copyPropertiesTo(model);
        if (model instanceof HandModel handModel) {
            handModel.getLeftHand().copyFrom(this.leftHand);
            handModel.getRightHand().copyFrom(this.rightHand);
        }
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);

        this.leftHand.visible = visible;
        this.rightHand.visible = visible;
        this.leftHandSleeve.visible = visible;
        this.rightHandSleeve.visible = visible;
    }

    @Override
    public ModelPart getLeftHand() {
        return this.leftHand;
    }

    @Override
    public ModelPart getRightHand() {
        return this.rightHand;
    }

    @Override
    public void hideLeftArm(boolean completeArm) {
        this.leftHand.visible = false;
        this.leftHandSleeve.visible = false;
        if (completeArm) {
            // hide shoulder as well
            super.hideLeftArm(false);
        }
    }

    @Override
    public void hideRightArm(boolean onlyHand) {
        this.rightHand.visible = false;
        this.rightHandSleeve.visible = false;
        if (onlyHand) {
            // hide shoulder as well
            super.hideRightArm(false);
        }
    }

    @Override
    protected ModelPart getArm(HumanoidArm side) {
        return side == HumanoidArm.RIGHT ? this.rightHand : this.leftHand;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        this.getArm(side).translateAndRotate(poseStack);

        poseStack.translate(side == HumanoidArm.LEFT ? -0.0625F : 0.0625F, -0.65F, 0.0F);

        if (side == this.attackArm) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
            poseStack.mulPose(Axis.XP.rotation(Mth.sin(this.attackTime * Mth.PI)));
            poseStack.translate(0.0F, -0.5F, 0.0F);
        }
    }
}
