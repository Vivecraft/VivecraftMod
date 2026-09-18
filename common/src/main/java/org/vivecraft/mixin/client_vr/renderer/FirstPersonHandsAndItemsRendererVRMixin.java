package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.Xloader;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.extensions.FirstPersonHandsAndItemsStateExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_vr.render.VivecraftItemRendering;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.renderstates.FirstPersonHandsAdditions;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.alexcaves.AlexCavesHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Mixin(value = FirstPersonHandsAndItemsRenderer.class, priority = 999)
public abstract class FirstPersonHandsAndItemsRendererVRMixin {

    @Unique
    private static final RenderType VIVECRAFT$MAP_BACKGROUND_NO_CULL = RenderTypes.entityCutout(
        Identifier.withDefaultNamespace("textures/map/map_background.png"), false);
    @Unique
    private static final RenderType VIVECRAFT$MAP_BACKGROUND_CHECKERBOARD_NO_CULL = RenderTypes.entityCutout(
        Identifier.withDefaultNamespace("textures/map/map_background_checkerboard.png"), false);

    @Unique
    private static final RenderType VIVECRAFT$MAP_BACKGROUND_NO_CULL_TEXT = VRRenderTypes.textNoCull(
        Identifier.withDefaultNamespace("textures/map/map_background.png"));
    @Unique
    private static final RenderType VIVECRAFT$MAP_BACKGROUND_CHECKERBOARD_NO_CULL_TEXT = VRRenderTypes.textNoCull(
        Identifier.withDefaultNamespace("textures/map/map_background_checkerboard.png"));

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    @Final
    private static RenderType MAP_BACKGROUND;

    @Shadow
    protected abstract void renderPlayerArm(
        PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
        int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, PlayerRenderState playerState);

    @Shadow
    protected abstract void renderMap(
        PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
        ItemStack itemStack, boolean mainHand, FirstPersonHandsAndItemsRenderState state);

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true)
    private void vivecraft$overrideArm(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, float equippedProgress,
        float swingProgress, HumanoidArm side, PlayerRenderState playerState, CallbackInfo ci)
    {
        if (VRState.VR_RUNNING) {
            vivecraft$vrPlayerArm(poseStack, collector, combinedLight, swingProgress, side, playerState);
            ci.cancel();
        }
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void vivecraft$overrideArmItem(
        PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTick, float pitch,
        InteractionHand hand, float swingProgress, ItemStack itemStack, float equippedProgress, PoseStack poseStack,
        SubmitNodeCollector collector, int combinedLight, CallbackInfo ci)
    {
        if (VRState.VR_RUNNING) {
            this.vivecraft$vrRenderArmWithItem(playerState, state, partialTick, hand, swingProgress, itemStack,
                equippedProgress, poseStack, collector, combinedLight);
            ci.cancel();
        }
    }

    @Inject(method = "renderMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V"), cancellable = true)
    private void vivecraft$overrideMapShaders(
        PoseStack poseStack, SubmitNodeCollector collector, int packedLight, ItemStack stack,
        boolean mainHand, FirstPersonHandsAndItemsRenderState state, CallbackInfo ci)
    {
        // with shaders, at least iris, we can't provide a custom text pipeline so need to use entity
        if (VRState.VR_RUNNING && ShadersHelper.isShaderActive()) {
            boolean hasMapData = mainHand ? state.hasMainHandMapData : state.hasOffHandMapData;
            MapRenderState mapRenderState = mainHand ? state.mainHandMapRenderState : state.offHandMapRenderState;
            RenderType renderType =
                hasMapData ? VIVECRAFT$MAP_BACKGROUND_NO_CULL : VIVECRAFT$MAP_BACKGROUND_CHECKERBOARD_NO_CULL;
            Vector3f normal = poseStack.last().pose().transformDirection(0F, 0F, 1F, new Vector3f());
            collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
                consumer.addVertex(pose, -7.0F, 135.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(pose, 135.0F, 135.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(pose, 135.0F, -7.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(pose, -7.0F, -7.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
            });

            if (hasMapData) {
                this.minecraft.getMapRenderer().render(mapRenderState, poseStack, collector, false, packedLight);
            }
            ci.cancel();
        }
    }

    @ModifyArg(method = "renderMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V"))
    private RenderType vivecraft$overrideMapVanilla(RenderType renderType) {
        if (VRState.VR_RUNNING) {
            return renderType == MAP_BACKGROUND ? VIVECRAFT$MAP_BACKGROUND_NO_CULL_TEXT :
                VIVECRAFT$MAP_BACKGROUND_CHECKERBOARD_NO_CULL_TEXT;
        } else {
            return renderType;
        }
    }

    @Unique
    private void vivecraft$vrRenderArmWithItem(
        PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTick,
        InteractionHand hand, float swingProgress, ItemStack itemStack, float equippedProgress,
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight)
    {
        if (playerState.avatarRenderState == null) return;
        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) playerState.avatarRenderState).vivecraft$getRotInfo();
        if (rotInfo == null) return;

        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm side =
            mainHand ? playerState.avatarRenderState.mainArm : playerState.avatarRenderState.mainArm.getOpposite();
        if (rotInfo.leftHanded) {
            side = side.getOpposite();
        }

        poseStack.pushPose();

        FirstPersonHandsAdditions additions = ((FirstPersonHandsAndItemsStateExtension) state).vivecraft$getAdditions();

        boolean renderArm = additions.currentPass != RenderPass.THIRD || additions.handsVisibleInThirdPerson;

        if (RenderPass.isFirstPerson(additions.currentPass)) {
            renderArm &= additions.handsVisibleInFirstPerson;
        }

        if (additions.currentPass == RenderPass.CAMERA) {
            renderArm = false;
        }
        if (BowTracker.isBow(itemStack) && additions.bowActive) {
            renderArm = false;
        }
        if (TelescopeTracker.isTelescope(itemStack) &&
            (hand == InteractionHand.OFF_HAND && additions.currentPass == RenderPass.SCOPEL ||
                hand == InteractionHand.MAIN_HAND && additions.currentPass == RenderPass.SCOPER
            ))
        {
            renderArm = false;
        }

        if (renderArm && !playerState.avatarRenderState.isInvisible) {
            this.renderPlayerArm(poseStack, collector, combinedLight, equippedProgress, swingProgress, side,
                playerState);
        }

        if (!itemStack.isEmpty()) {
            poseStack.pushPose();

            if (playerState.avatarRenderState.currentSwing != null &&
                playerState.avatarRenderState.currentSwing.hand() == hand)
            {
                this.vivecraft$transformFirstPersonVR(poseStack, side, swingProgress);
            }

            VivecraftItemRendering.VivecraftItemTransformType transformType =
                mainHand ? additions.mainHandItemTransformType : additions.offHandItemTransformType;

            ItemDisplayContext itemDisplayContext =
                mainHand ? additions.mainHandItemDisplayContext : additions.offHandItemDisplayContext;

            if (!itemDisplayContext.firstPerson()) {
                VivecraftItemRendering.applyThirdPersonItemTransforms(poseStack, transformType, mainHand, playerState,
                    equippedProgress, partialTick, itemStack, hand);
            } else {
                VivecraftItemRendering.applyFirstPersonItemTransforms(poseStack, transformType, mainHand, playerState,
                    equippedProgress, partialTick, itemStack, hand);
            }

            if (transformType == VivecraftItemRendering.VivecraftItemTransformType.MAP) {
                if (AlexCavesHelper.isLoaded() && AlexCavesHelper.isCaveMap(itemStack)) {
                    AlexCavesHelper.renderCaveMap(poseStack, collector, combinedLight, itemStack);
                } else {
                    this.renderMap(poseStack, collector, combinedLight, itemStack, mainHand, state);
                }
            } else if (transformType == VivecraftItemRendering.VivecraftItemTransformType.TELESCOPE) {
                if (additions.currentPass != RenderPass.SCOPEL && additions.currentPass != RenderPass.SCOPER) {
                    poseStack.pushPose();

                    (mainHand ? state.mainHandRenderState : state.offHandRenderState)
                        .submit(poseStack, collector, combinedLight, OverlayTexture.NO_OVERLAY, 0);
                    (mainHand ? state.mainHandRenderState : state.offHandRenderState)
                        .submit(poseStack, collector, combinedLight, OverlayTexture.NO_OVERLAY, 0);

                    if (ClientNetworking.isThirdPersonItems()) {
                        // account for the -2/16 offset of the third person spyglass transform
                        poseStack.translate(0.0F, 0.219F, 0.0F);
                    } else {
                        poseStack.translate(0.0F, 0.344F, 0.0F);
                    }

                    poseStack.rotate(Axis.XP.rotationDegrees(-90.0F));
                    if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                        // this messes stuff up when rendering the quads
                        OptifineHelper.endEntities();
                    }
                    // render scope view
                    VREffectsHelper.drawScopeFB(collector, poseStack, hand == InteractionHand.MAIN_HAND ? 0 : 1);

                    if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
                        OptifineHelper.beginEntities();
                    }

                    poseStack.popPose();
                }
            } else {
                (mainHand ? state.mainHandRenderState : state.offHandRenderState)
                    .submit(poseStack, collector, combinedLight, OverlayTexture.NO_OVERLAY, 0);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Unique
    private boolean vivecraft$didLogModelError = false;

    @Unique
    private void vivecraft$vrPlayerArm(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, float swingProgress, HumanoidArm side,
        PlayerRenderState playerState)
    {
        if (playerState.avatarRenderState == null) return;
        boolean rightHand = side == HumanoidArm.RIGHT;
        boolean mainHand =
            side == (ClientDataHolderVR.getInstance().vrSettings.reverseHands ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
        float offsetDirection = rightHand ? 1.0F : -1.0F;

        VRArmRenderer vrArmRenderer = ((EntityRenderDispatcherVRExtension) this.minecraft.getEntityRenderDispatcher()).vivecraft$getArmSkinMap()
            .get(playerState.avatarRenderState.skin.model());

        if (vrArmRenderer == null) {
            if (!this.vivecraft$didLogModelError) {
                VRSettings.LOGGER.error(
                    "Vivecraft: Some mod broke player model reloading. Possible culprit 'Stfu' loaded: {}",
                    Xloader.INSTANCE.isModLoaded("stfu"));
                this.vivecraft$didLogModelError = true;
            }
            return;
        }

        poseStack.pushPose();

        if (playerState.avatarRenderState.currentSwing != null) {
            if (playerState.avatarRenderState.currentSwing.hand() == InteractionHand.MAIN_HAND && mainHand) {
                this.vivecraft$transformFirstPersonVR(poseStack, side, swingProgress);
            }

            if (playerState.avatarRenderState.currentSwing.hand() == InteractionHand.OFF_HAND && !mainHand) {
                this.vivecraft$transformFirstPersonVR(poseStack, side, swingProgress);
            }
        }

        poseStack.scale(0.4f, 0.4F, 0.4F);
        boolean slim = playerState.avatarRenderState.skin.model() == PlayerModelType.SLIM;

            /*
             x offset: (arm x origin + arm x offset + arm x dimension * 0.5) / 16
             z offset: (arm y origin + arm y offset + arm y dimension) / 16
             slim
             x offset: (5 + -1 + 3*0.5) / 16 = 0.34375
             regular
             x offset: (5 - 1 + 4*0.5) / 16 = 0.375
             z offset: (-2 + 2 + 12) / 16 = 0.75
            */

        poseStack.translate((slim ? -0.34375F : -0.375F) * offsetDirection, 0.0F, 0.75F);
        poseStack.rotate(Axis.XP.rotationDegrees(-90));
        poseStack.rotate(Axis.YP.rotationDegrees(180));

        vrArmRenderer.armAlpha = ((FirstPersonHandsAndItemsStateExtension) playerState.firstPersonHandsAndItems).vivecraft$getAdditions()
            .getHandFade(mainHand);
        Identifier skin = playerState.avatarRenderState.skin.body().texturePath();

        if (rightHand) {
            vrArmRenderer.renderRightHand(poseStack, collector, combinedLight, skin, true);
        } else {
            vrArmRenderer.renderLeftHand(poseStack, collector, combinedLight, skin, true);
        }
        poseStack.popPose();
    }

    @Unique
    private void vivecraft$transformFirstPersonVR(PoseStack poseStack, HumanoidArm side, float swingProgress) {
        if (swingProgress == 0.0F) return;

        switch (ClientDataHolderVR.getInstance().swingType) {
            case ATTACK -> {
                float forwardRotation;
                if (swingProgress > 0.5F) {
                    forwardRotation = Mth.sin(swingProgress * Mth.PI + Mth.PI);
                } else {
                    forwardRotation = Mth.sin((swingProgress * 3.0F) * Mth.PI);
                }

                poseStack.translate(0.0F, 0.0F, 0.2F);
                poseStack.rotate(Axis.XP.rotationDegrees(forwardRotation * 30.0F));
                poseStack.translate(0.0F, 0.0F, -0.2F);
            }
            case INTERACT -> {
                float sideRotation;
                if (swingProgress > 0.5F) {
                    sideRotation = Mth.sin(swingProgress * Mth.PI + Mth.PI);
                } else {
                    sideRotation = Mth.sin((swingProgress * 3.0F) * Mth.PI);
                }

                poseStack.rotate(
                    Axis.ZP.rotationDegrees((side == HumanoidArm.RIGHT ? -1F : 1F) * sideRotation * 45.0F));
            }
            case USE -> {
                float forwardMovement;
                if (swingProgress > 0.25F) {
                    forwardMovement = Mth.sin((swingProgress / 2.0F) * Mth.PI + Mth.PI);
                } else {
                    forwardMovement = Mth.sin((swingProgress * 2.0F) * Mth.PI);
                }

                poseStack.translate(0.0F, 0.0F, -(1.0F + forwardMovement) * 0.1F);
            }
        }
    }
}
