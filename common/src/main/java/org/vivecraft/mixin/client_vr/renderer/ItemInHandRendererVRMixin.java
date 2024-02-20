package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;
import org.vivecraft.client_vr.extensions.ItemInHandRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.render.VivecraftItemRendering;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

@Mixin(value = ItemInHandRenderer.class, priority = 999)
public abstract class ItemInHandRendererVRMixin implements ItemInHandRendererExtension {

    @Unique
    private VRFirstPersonArmSwing vivecraft$swingType = VRFirstPersonArmSwing.Attack;

    @Final
    @Shadow
    private Minecraft minecraft;
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;
    @Final
    @Shadow
    private ItemRenderer itemRenderer;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private float offHandHeight;

    @Inject(at = @At("HEAD"), method = "renderPlayerArm", cancellable = true)
    public void vivecraft$overrideArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm, CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }
        vivecraft$vrPlayerArm(poseStack, multiBufferSource, i, f, g, humanoidArm);
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
    public void vivecraft$overrideArmItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }
        this.vivecraft$vrRenderArmWithItem(abstractClientPlayer, f, g, interactionHand, h, itemStack, i, poseStack, multiBufferSource, j);
        ci.cancel();
    }

    @Unique
    private void vivecraft$vrRenderArmWithItem(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight) {
        boolean mainHand = pHand == InteractionHand.MAIN_HAND;
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        HumanoidArm humanoidarm = mainHand ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite();
        pEquippedProgress = this.vivecraft$getEquipProgress(pHand, pPartialTicks);
        pMatrixStack.pushPose();
        boolean renderArm = dh.currentPass != RenderPass.THIRD || dh.vrSettings.mixedRealityRenderHands;

        if (dh.currentPass == RenderPass.CAMERA) {
            renderArm = false;
        }

        if (BowTracker.isBow(pStack) && dh.bowTracker.isActive((LocalPlayer) pPlayer)) {
            renderArm = false;
        }

        if (TelescopeTracker.isTelescope(pStack) && (pHand == InteractionHand.OFF_HAND && dh.currentPass == RenderPass.SCOPEL || pHand == InteractionHand.MAIN_HAND && dh.currentPass == RenderPass.SCOPER)) {
            renderArm = false;
        }

        if (renderArm && !pPlayer.isInvisible()) {
            this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, humanoidarm);
        }

        if (!pStack.isEmpty()) {
            pMatrixStack.pushPose();

            if (pPlayer.swingingArm == pHand) {
                this.vivecraft$transformFirstPersonVR(pMatrixStack, humanoidarm, pSwingProgress);
            }

            VivecraftItemRendering.VivecraftItemTransformType rendertype = VivecraftItemRendering.getTransformType(pStack, pPlayer, itemRenderer);

            boolean useLeftHandModelinLeftHand = false;

            // swap hand for claws, since it's backwards else wise
            if (dh.climbTracker.isClaws(pStack) && dh.vrSettings.reverseHands) {
                mainHand = !mainHand;
            }

            ItemDisplayContext itemDisplayContext;
            if ((ClientNetworking.isThirdPersonItems() && !(BowTracker.isBow(pStack) && dh.bowTracker.isActive((LocalPlayer) pPlayer))) || dh.climbTracker.isClaws(pStack)) {
                useLeftHandModelinLeftHand = true; //test
                VivecraftItemRendering.applyThirdPersonItemTransforms(pMatrixStack, rendertype, mainHand, pPlayer, pEquippedProgress, pPartialTicks, pStack, pHand);
                itemDisplayContext = mainHand ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : (useLeftHandModelinLeftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            } else {
                VivecraftItemRendering.applyFirstPersonItemTransforms(pMatrixStack, rendertype, mainHand, pPlayer, pEquippedProgress, pPartialTicks, pStack, pHand);
                itemDisplayContext = mainHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : (useLeftHandModelinLeftHand ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
            }

            ClientDataHolderVR.isfphand = true;

            if (rendertype == VivecraftItemRendering.VivecraftItemTransformType.Map) {
                RenderSystem.disableCull();
                this.renderMap(pMatrixStack, pBuffer, pCombinedLight, pStack);
            } else if (rendertype == VivecraftItemRendering.VivecraftItemTransformType.Telescope) {
                if (dh.currentPass != RenderPass.SCOPEL && dh.currentPass != RenderPass.SCOPER) {
                    pMatrixStack.pushPose();
                    pMatrixStack.scale(0.625F, 0.625F, 0.625F);
                    pMatrixStack.translate(mainHand ? -0.03D : 0.03D, 0.0D, -0.1D);
                    this.renderItem(pPlayer, pStack, itemDisplayContext, !mainHand && useLeftHandModelinLeftHand, pMatrixStack, pBuffer, pCombinedLight);
                    pMatrixStack.popPose();
                }

                pMatrixStack.pushPose();
                pMatrixStack.translate(mainHand ? -0.01875D : 0.01875D, 0.215D, -0.0626D);
                pMatrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                pMatrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                pMatrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                    // this messes stuff up when rendering the quads
                    OptifineHelper.endEntities();
                }
                VREffectsHelper.drawScopeFB(pMatrixStack, pHand == InteractionHand.MAIN_HAND ? 0 : 1);
                pMatrixStack.popPose();
                if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                    OptifineHelper.beginEntities();
                }
            } else {
                this.renderItem(pPlayer, pStack, itemDisplayContext, !mainHand && useLeftHandModelinLeftHand, pMatrixStack, pBuffer, pCombinedLight);
            }

            ClientDataHolderVR.isfphand = false;
            pMatrixStack.popPose();
        }

        pMatrixStack.popPose();
    }

    @Shadow
    public abstract void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i);

    @Shadow
    protected abstract void renderMap(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack);

    @Shadow
    protected abstract void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm humanoidarm);

    @Unique
    private float vivecraft$getEquipProgress(InteractionHand hand, float partialTicks) {
        return hand == InteractionHand.MAIN_HAND ? 1.0F - (this.oMainHandHeight + (this.mainHandHeight - this.oMainHandHeight) * partialTicks) : 1.0F - (this.oOffHandHeight + (this.offHandHeight - this.oOffHandHeight) * partialTicks);
    }

    @Unique
    private void vivecraft$vrPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm) {
        boolean flag = humanoidArm != HumanoidArm.LEFT;
        float h = flag ? 1.0F : -1.0F;
        AbstractClientPlayer abstractclientplayer = this.minecraft.player;
        RenderSystem.setShaderTexture(0, abstractclientplayer.getSkin().texture());
        VRArmRenderer vrarmrenderer = ((EntityRenderDispatcherVRExtension) entityRenderDispatcher).vivecraft$getArmSkinMap().get(abstractclientplayer.getSkin().model().id());
        poseStack.pushPose();

        if (abstractclientplayer.swingingArm == InteractionHand.MAIN_HAND && flag) {
            this.vivecraft$transformFirstPersonVR(poseStack, humanoidArm, g);
        }

        if (abstractclientplayer.swingingArm == InteractionHand.OFF_HAND && !flag) {
            this.vivecraft$transformFirstPersonVR(poseStack, humanoidArm, g);
        }

        poseStack.scale(0.4f, 0.4F, 0.4F);
        boolean slim = abstractclientplayer.getSkin().model().id().equals("slim");

            /*
             x offset: (arm x origin + arm x offset + arm x dimension * 0.5) / 16
             z offset: (arm y origin + arm y offset + arm y dimension) / 16
             slim
             x offset: (5 + -1 + 3*0.5) / 16 = 0.34375
             z offset: (-2 + 2.5 + 12) / 16 = 0.78125
             regular
             x offset: (5 - 1 + 4*0.5) / 16 = 0.375
             z offset: (-2 + 2 + 12) / 16 = 0.75
            */

        poseStack.translate((slim ? -0.34375F : -0.375F) * h, 0.0F, slim ? 0.78125F : 0.75F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        if (flag) {
            vrarmrenderer.renderRightHand(poseStack, multiBufferSource, i, abstractclientplayer);
        } else {
            vrarmrenderer.renderLeftHand(poseStack, multiBufferSource, i, abstractclientplayer);
        }
        poseStack.popPose();
    }

    @Override
    @Unique
    public void vivecraft$setSwingType(VRFirstPersonArmSwing interact) {
        this.vivecraft$swingType = interact;
    }

    @Unique
    private void vivecraft$transformFirstPersonVR(PoseStack matrixStackIn, HumanoidArm hand, float swingProgress) {
        if (swingProgress != 0.0F) {
            switch (this.vivecraft$swingType) {
                case Attack:
                    float f2 = Mth.sin((float) ((double) (swingProgress * 3.0F) * Math.PI));
                    if ((double) swingProgress > 0.5D) {
                        f2 = Mth.sin((float) ((double) swingProgress * Math.PI + Math.PI));
                    }

                    matrixStackIn.translate(0.0D, 0.0D, 0.2F);
                    matrixStackIn.mulPose(Axis.XP.rotationDegrees(f2 * 30.0F));
                    matrixStackIn.translate(0.0D, 0.0D, -0.2F);
                    break;

                case Interact:
                    float f1 = Mth.sin((float) ((double) (swingProgress * 3.0F) * Math.PI));

                    if ((double) swingProgress > 0.5D) {
                        f1 = Mth.sin((float) ((double) swingProgress * Math.PI + Math.PI));
                    }

                    matrixStackIn.mulPose(Axis.ZP.rotationDegrees((float) (hand == HumanoidArm.RIGHT ? -1 : 1) * f1 * 45.0F));
                    break;

                case Use:
                    float f = Mth.sin((float) ((double) (swingProgress * 2.0F) * Math.PI));

                    if ((double) swingProgress > 0.25D) {
                        f = Mth.sin((float) ((double) (swingProgress / 2.0F) * Math.PI + Math.PI));
                    }
                    matrixStackIn.translate(0.0D, 0.0D, -(1.0F + f) * 0.1F);
            }
        }
    }
}
