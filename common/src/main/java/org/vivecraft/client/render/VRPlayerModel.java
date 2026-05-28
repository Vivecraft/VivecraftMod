package org.vivecraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.utils.ModelUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.mod_compat_vr.mca.MCAHelper;

public class VRPlayerModel extends PlayerModel {

    // temp vec for most math
    protected final Vector3f tempV = new Vector3f();
    protected final Vector3f tempV2 = new Vector3f();
    // temp mat3 for rotations
    protected final Matrix3f tempM = new Matrix3f();

    public VRPlayerModel(ModelPart root, boolean isSlim) {
        super(root, isSlim);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, slim);

        return meshDefinition;
    }

    @Override
    public void setupAnim(AvatarRenderState renderState) {
        // no crouch hip movement when roomscale crawling
        renderState.isCrouching &= !renderState.isVisuallySwimming;
        super.setupAnim(renderState);
    }

    public static void animateVRModel(
        PlayerModel model, AvatarRenderState renderState, Vector3f tempV, Vector3f tempV2, Matrix3f tempM)
    {
        if (model instanceof VRPlayerModel_WithArms armsModel) {
            armsModel.leftHand.visible = model.leftArm.visible;
            armsModel.rightHand.visible = model.rightArm.visible;
        }

        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) renderState).vivecraft$getRotInfo();
        VRPlayerRenderData data = ((EntityRenderStateExtension) renderState).vivecraft$getVRRenderData();

        if (rotInfo == null || data == null) {
            // not a vr player
            return;
        }

        if (data.isMainPlayer()) {
            if (ClientDataHolderVR.getInstance().currentPass == RenderPass.CAMERA &&
                ClientDataHolderVR.getInstance().cameraTracker.isQuickMode() &&
                ClientDataHolderVR.getInstance().grabScreenShot)
            {
                // player hands block the camera, so disable them for the screenshot
                hideHand(model, HumanoidArm.LEFT, true);
                hideHand(model, HumanoidArm.RIGHT, true);
            }
            if (VREffectsHelper.isFirstPersonEntityPass()) {
                // hide the head or you won't see anything
                model.head.visible = false;
                model.hat.visible = false;

                // hide model arms when not using them
                if (ClientDataHolderVR.getInstance().vrSettings.modelArmsMode !=
                    VRSettings.ModelArmsMode.COMPLETE)
                {
                    // keep the shoulders when in shoulder mode
                    hideHand(model, HumanoidArm.LEFT, ClientDataHolderVR.getInstance().vrSettings.modelArmsMode ==
                        VRSettings.ModelArmsMode.OFF);
                    hideHand(model, HumanoidArm.RIGHT, ClientDataHolderVR.getInstance().vrSettings.modelArmsMode ==
                        VRSettings.ModelArmsMode.OFF);
                } else {
                    boolean leftHanded = ClientDataHolderVR.getInstance().vrSettings.reverseHands;
                    if (ClientDataHolderVR.getInstance().menuHandOff) {
                        hideHand(model, leftHanded ? HumanoidArm.RIGHT : HumanoidArm.LEFT, false);
                    }
                    if (ClientDataHolderVR.getInstance().menuHandMain) {
                        hideHand(model, leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT, false);
                    }
                }
            }
        }

        // scale the offset with the body and arm scale, to keep them attached
        float sideOffset = 4F * data.bodyScale() + data.armScale();

        // head pivot
        if (!data.swimming()) {
            rotInfo.headQuat.transform(0F, -0.2F, 0.1F, tempV2);
            tempV2.mul(rotInfo.heightScale * rotInfo.worldScale);
        } else {
            // no pivot offset when swimming
            tempV2.zero();
        }
        tempV2.add(rotInfo.headPos);

        float progress = ModelUtils.getBendProgress(renderState.isAutoSpinAttack, renderState.isCrouching,
            renderState.isPassenger, rotInfo, tempV2);
        float heightOffset = 22F * progress;

        // rotate head
        tempM.set(rotInfo.headQuat)
            .rotateLocalY(data.bodyYaw() + Mth.PI)
            .rotateLocalX(-data.xRot());
        ModelUtils.setRotation(model.head, tempM, tempV);
        ModelUtils.worldToModel(renderState, tempV2, rotInfo, data.bodyYaw(), true, tempV);

        if (data.swimming()) {
            // move the head in front of the body when swimming
            tempV.z += 3F;
        }

        // move head and body with bend
        model.head.setPos(tempV.x, tempV.y, tempV.z);
        model.body.setPos(model.head.x, model.head.y, model.head.z);

        // rotate body
        if (renderState.isPassenger) {
            // when riding, rotate body to sitting position
            ModelUtils.pointModelAtModelForward(model.body, 0F, 14F, 2F + heightOffset, tempV, tempV2, tempM);
            tempM.rotateLocalX(-data.xRot());
            ModelUtils.setRotation(model.body, tempM, tempV);
        } else if (data.noLowerBodyAnimation()) {
            // with only arms simply rotate the body in place
            model.body.setRotation(
                Mth.PI * Math.max(0F, model.body.y / 22F) * (model instanceof VRPlayerModel_WithArmsLegs ? 0.5F : 1F),
                0F, 0F);
            if (data.laying()) {
                float bodyXRot;
                if (data.swimming()) {
                    bodyXRot = -data.xRot();
                } else {
                    float aboveGround = (heightOffset - 11F) / 11F;
                    bodyXRot = progress * (Mth.PI - Mth.HALF_PI * (1F + 0.3F * (1F - aboveGround)));
                }
                // lerp body rotation when swimming, to keep the model connected
                model.body.xRot = Mth.lerp(data.layAmount(), model.body.xRot, bodyXRot);
                model.head.y -= 2F * data.layAmount();
                model.body.y -= 2F * data.layAmount();
            }
        } else {
            // body/arm position with waist tracker
            // if there is a waist tracker, align the body to that
            ModelUtils.pointModelAtLocal(renderState, model.body, rotInfo.waistPos, rotInfo.waistQuat, rotInfo,
                data.bodyYaw(), true, tempV, tempV2, tempM);

            // offset arms
            tempM.transform(sideOffset, 2F, 0F, tempV2);
            model.leftArm.x = model.body.x + tempV2.x;
            model.leftArm.y = model.body.y + tempV2.y;
            model.leftArm.z = model.body.z - tempV2.z;

            tempM.transform(-sideOffset, 2F, 0F, tempV2);
            model.rightArm.x = model.body.x + tempV2.x;
            model.rightArm.y = model.body.y + tempV2.y;
            model.rightArm.z = model.body.z - tempV2.z;

            tempM.rotateLocalX(-data.xRot());
            ModelUtils.setRotation(model.body, tempM, tempV);
        }

        float cosBodyRot = Mth.cos(model.body.xRot);

        if (renderState.isPassenger || data.noLowerBodyAnimation()) {
            // offset arms with body rotation
            model.leftArm.x = model.body.x + sideOffset;
            model.rightArm.x = model.body.x - sideOffset;
            model.leftArm.y = 2F * cosBodyRot + model.body.y;
            model.leftArm.z = model.body.z;

            model.rightArm.y = model.leftArm.y;
            model.rightArm.z = model.leftArm.z;
        }

        model.leftLeg.x = 1.9F;
        model.rightLeg.x = -1.9F;

        if (renderState.isPassenger) {
            model.leftLeg.z = heightOffset;
            model.rightLeg.z = model.leftLeg.z;
        } else if (data.laying() && data.noLowerBodyAnimation()) {
            // adjust legs
            if (data.swimming()) {
                tempV.set(0, 12, 0);
                tempV.rotateX(-data.xRot());
                model.leftLeg.y = model.body.y + tempV.y;
                model.leftLeg.z = model.body.z + tempV.z;
            } else {
                // move legs with bend
                float cosBodyRot2 = cosBodyRot * cosBodyRot;
                model.leftLeg.y += 10.25F - 2F * cosBodyRot2;
                model.leftLeg.z = model.body.z + 13F - cosBodyRot2 * 8F;
            }
            model.leftLeg.x += model.body.x;
            model.rightLeg.x += model.body.x;

            model.rightLeg.y = model.leftLeg.y;
            model.rightLeg.z = model.leftLeg.z;
        } else if (rotInfo.fbtMode != FBTMode.ARMS_ONLY) {
            // fbt leg position
            ModelUtils.worldToModel(renderState, rotInfo.waistPos, rotInfo, data.bodyYaw(), true, tempV);

            tempV2.set(-1.9F, -2F, 0F);
            rotInfo.waistQuat.transform(tempV2);
            ModelUtils.worldToModelDirection(tempV2, data.bodyYaw(), tempV2);
            model.leftLeg.setPos(
                tempV.x + tempV2.x,
                tempV.y + tempV2.y,
                tempV.z + tempV2.z);

            tempV2.set(1.9F, -2F, 0F);
            rotInfo.waistQuat.transform(tempV2);
            ModelUtils.worldToModelDirection(tempV2, data.bodyYaw(), tempV2);
            model.rightLeg.setPos(
                tempV.x + tempV2.x,
                tempV.y + tempV2.y,
                tempV.z + tempV2.z);
        } else {
            model.leftLeg.x += model.body.x;
            model.rightLeg.x += model.body.x;
        }

        // regular positioning
        if (!renderState.isPassenger && data.layAmount() < 1.0F && rotInfo.fbtMode == FBTMode.ARMS_ONLY) {
            // move legs back with bend
            float newLegY = 12F + Math.min(model.body.y, 0F);
            float newLegZ = model.body.z + 10F * Mth.sin(model.body.xRot);
            if (model instanceof VRPlayerModel_WithArmsLegs) {
                newLegY += 10F * Mth.sin(model.body.xRot);
            }

            model.leftLeg.y = Mth.lerp(data.layAmount(), newLegY, model.leftLeg.y);
            model.leftLeg.z = Mth.lerp(data.layAmount(), newLegZ, model.leftLeg.z);

            model.rightLeg.y = model.leftLeg.y;
            model.rightLeg.z = model.leftLeg.z;
        }

        // arms/legs only when standing
        if (!rotInfo.seated || data.isMainPlayer()) {
            // arms only when not a split arms model
            if (!(model instanceof VRPlayerModel_WithArms) &&
                rotInfo.offHandPos.distanceSquared(rotInfo.mainHandPos) > 0.0F)
            {
                ModelPart offHand = rotInfo.leftHanded ? model.rightArm : model.leftArm;
                ModelPart mainHand = rotInfo.leftHanded ? model.leftArm : model.rightArm;

                if (ClientDataHolderVR.getInstance().vrSettings.playerLimbsConnected) {
                    // rotation offset, since the rotation point isn't in the center.
                    // this rotates the arm 0.5 or 1 pixels at full arm distance, so that the hand matches up with the center
                    float offset =
                        (rotInfo.leftHanded ? -1F : 1f) * (model.slim ? 0.016F : 0.032F) * Mth.PI * data.armScale();
                    // main hand
                    positionConnectedArm(mainHand, rotInfo.mainHandPos, rotInfo.mainHandQuat, renderState, rotInfo,
                        data, offset, data.attackArm() == data.mainArm(), false, tempV, tempV2, tempM);

                    // offhand
                    positionConnectedArm(offHand, rotInfo.offHandPos, rotInfo.offHandQuat, renderState, rotInfo,
                        data, -offset, data.attackArm() != data.mainArm(), true, tempV, tempV2, tempM);
                } else {
                    float xOffset = (model.slim ? 0.5F : 1F) * (rotInfo.leftHanded ? -1F : 1F);
                    // main hand
                    positionFloatingArm(mainHand, rotInfo.mainHandPos, rotInfo.mainHandQuat, renderState, rotInfo,
                        data, -xOffset, data.attackArm() == data.mainArm(), false, tempV, tempV2, tempM);

                    // offhand
                    positionFloatingArm(offHand, rotInfo.offHandPos, rotInfo.offHandQuat, renderState, rotInfo,
                        data, xOffset, data.attackArm() != data.mainArm(), true, tempV, tempV2, tempM);
                    model.leftArm.yScale = model.rightArm.yScale = data.armScale();
                }
            }

            // legs only when not sitting
            if (!renderState.isPassenger && !data.noLowerBodyAnimation() &&
                !(model instanceof VRPlayerModel_WithArmsLegs))
            {
                float limbRotation = 0F;
                if (ClientDataHolderVR.getInstance().vrSettings.playerWalkAnim) {
                    // vanilla walking animation on top
                    limbRotation = Mth.cos(renderState.walkAnimationPos * 0.6662F) * 1.4F *
                        renderState.walkAnimationSpeed;
                }

                ModelUtils.pointModelAtLocal(renderState, model.rightLeg, rotInfo.rightFootPos,
                    rotInfo.rightFootQuat, rotInfo, data.bodyYaw(), true, tempV,
                    tempV2, tempM);
                tempM.rotateLocalX(limbRotation - data.xRot());
                ModelUtils.setRotation(model.rightLeg, tempM, tempV);

                ModelUtils.pointModelAtLocal(renderState, model.leftLeg, rotInfo.leftFootPos,
                    rotInfo.leftFootQuat,
                    rotInfo, data.bodyYaw(), true, tempV, tempV2, tempM);
                tempM.rotateLocalX(-limbRotation - data.xRot());
                ModelUtils.setRotation(model.leftLeg, tempM, tempV);
            }
        }

        if (data.layAmount() > 0F) {
            if (data.noLowerBodyAnimation()) {
                // with a waist tracker the rotation is already done before
                model.body.xRot += data.xRot();
            }

            if (model instanceof VRPlayerModel_WithArmsLegs) {
                ModelUtils.applySwimRotationOffset(renderState, data.xRot(), tempV, tempV2,
                    model.head, model.body);
            } else if (model instanceof VRPlayerModel_WithArms) {
                ModelUtils.applySwimRotationOffset(renderState, data.xRot(), tempV, tempV2,
                    model.head, model.body,
                    model.leftLeg, model.rightLeg);
            } else {
                ModelUtils.applySwimRotationOffset(renderState, data.xRot(), tempV, tempV2,
                    model.head, model.body,
                    model.leftArm, model.rightArm,
                    model.leftLeg, model.rightLeg);
            }
        }

        model.leftArm.xScale = model.leftArm.zScale = model.rightArm.xScale = model.rightArm.zScale = data.armScale();
        model.body.xScale = model.body.zScale = data.bodyScale();
        model.leftLeg.xScale = model.leftLeg.zScale = model.rightLeg.xScale = model.rightLeg.zScale = data.legScale();

        // spin attack moves the model one block up
        if (renderState.isAutoSpinAttack) {
            spinOffset(model.head, model.body);
            if (!(model instanceof VRPlayerModel_WithArms)) {
                spinOffset(model.leftArm, model.rightArm);
            }
            if (!(model instanceof VRPlayerModel_WithArmsLegs)) {
                spinOffset(model.leftLeg, model.rightLeg);
            }
        }
    }


    private static void positionConnectedArm(
        ModelPart arm, Vector3fc armPos, Quaternionfc armRot, AvatarRenderState renderState,
        ClientVRPlayers.RotInfo rotInfo, VRPlayerRenderData data, float zRotOffset, boolean applyAttackAnim,
        boolean setGuiOrientation, Vector3f tempV, Vector3f tempV2, Matrix3f tempM)
    {
        ModelUtils.worldToModel(renderState, armPos, rotInfo, data.bodyYaw(),
            data.isMainPlayer() || ClientDataHolderVR.getInstance().vrSettings.applyPlayerWorldscale, tempV);
        tempV.sub(arm.x, arm.y, arm.z);
        // move shoulders up when having the arms up, since the rotation point is slightly offset
        arm.y -= 2F * Math.max(0F, -tempV.y / tempV.length());

        ModelUtils.pointAtModelWithLocal(armRot, data.bodyYaw(), tempV, tempV2, tempM);

        float controllerDist = tempV.length();

        if (!ClientDataHolderVR.getInstance().vrSettings.playerLimbsLimit && controllerDist > 10F) {
            tempV.normalize().mul(controllerDist - 10F);
            arm.x += tempV.x;
            arm.y += tempV.y;
            arm.z += tempV.z;
            tempM.rotateZ(-zRotOffset);
        } else {
            // reduce correction angle with distance
            tempM.rotateZ(-zRotOffset * Math.min(10F / controllerDist, 1F));
        }

        if (ClientDataHolderVR.getInstance().vrSettings.playerArmAnim && applyAttackAnim) {
            ModelUtils.swingAnimation(data.attackArm(), renderState.attackTime, data.isMainPlayer(), tempM, tempV);
            arm.x -= tempV.x;
            arm.y -= tempV.y;
            arm.z += tempV.z;
        }

        if (setGuiOrientation && data.isMainPlayer()) {
            positionGUI(arm, renderState, rotInfo, data, 0.584F, tempV, tempV2, tempM);
        }

        tempM.rotateLocalX(-data.xRot());
        ModelUtils.setRotation(arm, tempM, tempV);
    }

    private static void positionFloatingArm(
        ModelPart arm, Vector3fc armPos, Quaternionfc armRot, AvatarRenderState renderState,
        ClientVRPlayers.RotInfo rotInfo, VRPlayerRenderData data, float xOffset, boolean applyAttackAnim,
        boolean setGuiOrientation, Vector3f tempV, Vector3f tempV2, Matrix3f tempM)
    {

        // place lower directly at the lower point
        ModelUtils.worldToModel(renderState, armPos, rotInfo, data.bodyYaw(),
            data.isMainPlayer() || ClientDataHolderVR.getInstance().vrSettings.applyPlayerWorldscale, tempV);

        ModelUtils.toModelDir(data.bodyYaw(), armRot, tempM);
        tempM.transform(xOffset * data.armScale(), 10 * data.armScale(), 0, tempV2);
        tempV.sub(tempV2.x, tempV2.y, -tempV2.z);
        arm.setPos(tempV.x, tempV.y, tempV.z);

        if (ClientDataHolderVR.getInstance().vrSettings.playerArmAnim && applyAttackAnim) {
            ModelUtils.swingAnimation(arm, data.attackArm(), 2F * data.armScale(), renderState.attackTime,
                data.isMainPlayer(), tempM, tempV, tempV2);
        }

        if (setGuiOrientation && data.isMainPlayer()) {
            positionGUI(arm, renderState, rotInfo, data, 0.584F * data.armScale(), tempV, tempV2, tempM);
        }

        tempM.rotateLocalX(-data.xRot());
        ModelUtils.setRotation(arm, tempM, tempV);
    }

    private static void positionGUI(
        ModelPart arm, AvatarRenderState renderState, ClientVRPlayers.RotInfo rotInfo, VRPlayerRenderData data,
        float guiOffset, Vector3f tempV, Vector3f tempV2, Matrix3f tempM)
    {
        if (ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
            ClientDataHolderVR.getInstance().vrSettings.modelArmsMode != VRSettings.ModelArmsMode.OFF)
        {
            GuiHandler.GUI_ROTATION_PLAYER_MODEL.set3x3(tempM);
            // ModelParts are rotated 90°
            GuiHandler.GUI_ROTATION_PLAYER_MODEL.rotateX(-Mth.HALF_PI);
            // undo body yaw
            GuiHandler.GUI_ROTATION_PLAYER_MODEL.rotateLocalY(-data.bodyYaw() - Mth.PI);

            // arm vector
            GuiHandler.GUI_ROTATION_PLAYER_MODEL.transformDirection(MathUtils.BACK, tempV)
                .mul(guiOffset * rotInfo.worldScale);

            ModelUtils.modelToWorld(renderState, arm.x, arm.y, arm.z, rotInfo, data.bodyYaw(), true, true, tempV2);
            if (MCAHelper.isLoaded()) {
                // TODO MCA isn't updated yet so no clue how to do this yet
                // MCAHelper.applyPlayerScale(player, tempV);
            }

            tempV2.add(tempV);

            GuiHandler.GUI_POS_PLAYER_MODEL = new Vec3(renderState.x, renderState.y, renderState.z)
                .add(tempV2.x, tempV2.y, tempV2.z);
        }
    }

    private static void hideHand(PlayerModel model, HumanoidArm arm, boolean completeArm) {
        if (model instanceof VRPlayerModel vrModel) {
            if (arm == HumanoidArm.LEFT) {
                vrModel.hideLeftArm(completeArm);
            } else {
                vrModel.hideRightArm(completeArm);
            }
        } else {
            // this is just for the case someone replaces the model
            if (arm == HumanoidArm.LEFT) {
                model.leftArm.visible = false;
            } else {
                model.rightArm.visible = false;
            }
        }
    }

    public void hideLeftArm(boolean completeArm) {
        this.leftArm.visible = false;
    }

    public void hideRightArm(boolean onlyHand) {
        this.rightArm.visible = false;
    }

    protected static void spinOffset(ModelPart... parts) {
        for (ModelPart part : parts) {
            part.y += 24F;
        }
    }

    @Override
    public void translateToHand(AvatarRenderState avatarRenderState, HumanoidArm side, PoseStack poseStack) {
        // can't call super, because, the vanilla slim offset doesn't work with rotations
        this.getArm(side).translateAndRotate(poseStack);

        if (this.slim) {
            poseStack.translate(side == HumanoidArm.RIGHT ? 0.03125F : -0.03125F, 0.0F, 0.0F);
        }

        doAttackAnim(avatarRenderState, side, poseStack);
    }

    protected void doAttackAnim(AvatarRenderState avatarRenderState, HumanoidArm side, PoseStack poseStack) {
        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) avatarRenderState).vivecraft$getRotInfo();

        if (rotInfo != null && avatarRenderState.attackTime > 0F) {
            // we ignore the vanilla main arm setting
            if (side ==
                (rotInfo.leftHanded ? avatarRenderState.attackArm.getOpposite() : avatarRenderState.attackArm))
            {
                poseStack.translate(0.0F, 0.5F, 0.0F);
                poseStack.mulPose(Axis.XP.rotation(Mth.sin(avatarRenderState.attackTime * Mth.PI)));
                poseStack.translate(0.0F, -0.5F, 0.0F);
            }
        }
    }
}
