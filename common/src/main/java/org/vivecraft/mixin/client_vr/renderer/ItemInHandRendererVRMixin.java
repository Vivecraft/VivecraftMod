package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
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
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_vr.render.VivecraftItemRendering;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.data.ViveItems;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Mixin(value = ItemInHandRenderer.class, priority = 999)
public abstract class ItemInHandRendererVRMixin {

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
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private float offHandHeight;

    @Shadow
    @Final
    private MapRenderState mapRenderState;

    @Shadow
    public abstract void renderItem(
        LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext,
        PoseStack poseStack, SubmitNodeCollector collector, int seed);

    @Shadow
    protected abstract void renderMap(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, ItemStack stack);

    @Shadow
    protected abstract void renderPlayerArm(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, float equippedProgress,
        float swingProgress,
        HumanoidArm side);

    @Shadow
    @Final
    private static RenderType MAP_BACKGROUND;

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true)
    private void vivecraft$overrideArm(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, float equippedProgress,
        float swingProgress, HumanoidArm side, CallbackInfo ci)
    {
        if (VRState.VR_RUNNING) {
            vivecraft$vrPlayerArm(poseStack, collector, combinedLight, swingProgress, side);
            ci.cancel();
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void vivecraft$overrideArmItem(
        AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress,
        ItemStack itemStack, float equippedProgress, PoseStack poseStack, SubmitNodeCollector collector,
        int combinedLight, CallbackInfo ci)
    {
        if (VRState.VR_RUNNING) {
            this.vivecraft$vrRenderArmWithItem(player, partialTick, hand, swingProgress, itemStack, poseStack,
                collector,
                combinedLight);
            ci.cancel();
        }
    }

    @Inject(method = "renderMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"), cancellable = true)
    private void vivecraft$overrideMapShaders(
        PoseStack poseStack, SubmitNodeCollector collector, int packedLight, ItemStack stack, CallbackInfo ci)
    {
        // with shaders, at least iris, we can't provide a custom text pipeline so need to use entity
        if (VRState.VR_RUNNING && ShadersHelper.isShaderActive()) {
            MapId mapId = stack.get(DataComponents.MAP_ID);
            MapItemSavedData mapData = MapItem.getSavedData(mapId, this.minecraft.level);
            RenderType renderType =
                mapData == null ? VIVECRAFT$MAP_BACKGROUND_NO_CULL : VIVECRAFT$MAP_BACKGROUND_CHECKERBOARD_NO_CULL;
            Matrix4f matrix = poseStack.last().pose();
            Vector3f normal = matrix.transformDirection(0F, 0F, 1F, new Vector3f());
            collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
                consumer.addVertex(matrix, -7.0F, 135.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(matrix, 135.0F, 135.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(matrix, 135.0F, -7.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(1.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
                consumer.addVertex(matrix, -7.0F, -7.0F, 0.0F)
                    .setColor(255, 255, 255, 255)
                    .setUv(0.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                    .setNormal(normal.x, normal.y, normal.z);
            });
            if (mapData != null) {
                MapRenderer mapRenderer = this.minecraft.getMapRenderer();
                mapRenderer.extractRenderState(mapId, mapData, this.mapRenderState);
                mapRenderer.render(this.mapRenderState, poseStack, collector, false, packedLight);
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
        AbstractClientPlayer player, float partialTick, InteractionHand hand, float swingProgress, ItemStack itemStack,
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight)
    {
        // TODO 26.1 extract arm state
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();

        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm side = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        if (dh.vrSettings.reverseHands) {
            side = side.getOpposite();
        }
        // we need to get this here, because the supplied value is invalid when we call it
        float equippedProgress = this.vivecraft$getEquipProgress(hand, partialTick);

        poseStack.pushPose();

        boolean renderArm = dh.currentPass != RenderPass.THIRD || dh.vrSettings.mixedRealityRenderHands;

        if (RenderPass.isFirstPerson(dh.currentPass)) {
            renderArm &= dh.vrSettings.showPlayerHands;
        }

        if (dh.currentPass == RenderPass.CAMERA) {
            renderArm = false;
        }
        if (BowTracker.isBow(itemStack) && dh.bowTracker.isActive((LocalPlayer) player)) {
            renderArm = false;
        }
        if (TelescopeTracker.isTelescope(itemStack) &&
            (hand == InteractionHand.OFF_HAND && dh.currentPass == RenderPass.SCOPEL ||
                hand == InteractionHand.MAIN_HAND && dh.currentPass == RenderPass.SCOPER
            ))
        {
            renderArm = false;
        }

        if (RenderPass.isFirstPerson(dh.currentPass) &&
            ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf &&
            ClientDataHolderVR.getInstance().vrSettings.modelArmsMode == VRSettings.ModelArmsMode.COMPLETE)
        {
            renderArm = false;
        }

        if (renderArm && !player.isInvisible()) {
            this.renderPlayerArm(poseStack, collector, combinedLight, equippedProgress, swingProgress, side);
        }

        if (!itemStack.isEmpty()) {
            poseStack.pushPose();

            if (player.swingingArm == hand) {
                this.vivecraft$transformFirstPersonVR(poseStack, side, swingProgress);
            }

            VivecraftItemRendering.VivecraftItemTransformType transformType = VivecraftItemRendering.getTransformType(
                itemStack, player);

            boolean useLeftHandModelinLeftHand = false;

            ItemDisplayContext itemDisplayContext;

            // third person transforms for custom model data items/item model overrides, but not spear, shield and crossbow
            boolean hasItemOverride = itemStack.getComponents() instanceof PatchedDataComponentMap patched &&
                patched.hasNonDefault(DataComponents.ITEM_MODEL);
            boolean hasCMD = (hasItemOverride || itemStack.has(DataComponents.CUSTOM_MODEL_DATA)) &&
                transformType != VivecraftItemRendering.VivecraftItemTransformType.CROSSBOW &&
                transformType != VivecraftItemRendering.VivecraftItemTransformType.SPEAR &&
                transformType != VivecraftItemRendering.VivecraftItemTransformType.SHIELD;

            boolean isBow = BowTracker.isBow(itemStack) && dh.bowTracker.isActive((LocalPlayer) player);

            if (ViveItems.isClimbingClaws(itemStack) || (!isBow &&
                (ClientNetworking.isThirdPersonItems() || (hasCMD && ClientNetworking.isThirdPersonItemsCustom()))
            ))
            {
                // swap hand, since it's backwards else wise
                if (dh.vrSettings.reverseHands) {
                    mainHand = !mainHand;
                }
                useLeftHandModelinLeftHand = true; // test
                VivecraftItemRendering.applyThirdPersonItemTransforms(poseStack, transformType, mainHand, player,
                    equippedProgress, partialTick, itemStack, hand);

                itemDisplayContext = mainHand || !useLeftHandModelinLeftHand ?
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            } else {
                VivecraftItemRendering.applyFirstPersonItemTransforms(poseStack, transformType, mainHand, player,
                    equippedProgress, partialTick, itemStack, hand);

                itemDisplayContext = mainHand || !useLeftHandModelinLeftHand ?
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            }

            if (transformType == VivecraftItemRendering.VivecraftItemTransformType.MAP) {
                this.renderMap(poseStack, collector, combinedLight, itemStack);
            } else if (transformType == VivecraftItemRendering.VivecraftItemTransformType.TELESCOPE) {
                if (dh.currentPass != RenderPass.SCOPEL && dh.currentPass != RenderPass.SCOPER) {
                    poseStack.pushPose();

                    renderItem(player, itemStack, itemDisplayContext, poseStack, collector, combinedLight);

                    if (ClientNetworking.isThirdPersonItems()) {
                        // account for the -2/16 offset of the third person spyglass transform
                        poseStack.translate(0.0F, 0.219F, 0.0F);
                    } else {
                        poseStack.translate(0.0F, 0.344F, 0.0F);
                    }

                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
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
                this.renderItem(player, itemStack, itemDisplayContext, poseStack, collector, combinedLight);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Unique
    private float vivecraft$getEquipProgress(InteractionHand hand, float partialTick) {
        return hand == InteractionHand.MAIN_HAND ?
            1.0F - Mth.lerp(partialTick, this.oMainHandHeight, this.mainHandHeight) :
            1.0F - Mth.lerp(partialTick, this.oOffHandHeight, this.offHandHeight);
    }

    @Unique
    private boolean vivecraft$didLogModelError = false;

    @Unique
    private void vivecraft$vrPlayerArm(
        PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, float swingProgress, HumanoidArm side)
    {
        LocalPlayer player = this.minecraft.player;
        boolean rightHand = side == HumanoidArm.RIGHT;
        boolean mainHand =
            side == (ClientDataHolderVR.getInstance().vrSettings.reverseHands ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
        float offsetDirection = rightHand ? 1.0F : -1.0F;

        VRArmRenderer vrArmRenderer = ((EntityRenderDispatcherVRExtension) this.entityRenderDispatcher).vivecraft$getArmSkinMap()
            .get(player.getSkin().model());

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

        if (player.swingingArm == InteractionHand.MAIN_HAND && mainHand) {
            this.vivecraft$transformFirstPersonVR(poseStack, side, swingProgress);
        }

        if (player.swingingArm == InteractionHand.OFF_HAND && !mainHand) {
            this.vivecraft$transformFirstPersonVR(poseStack, side, swingProgress);
        }

        poseStack.scale(0.4f, 0.4F, 0.4F);
        boolean slim = player.getSkin().model() == PlayerModelType.SLIM;

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
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        vrArmRenderer.armAlpha = SwingTracker.getItemFade(player, ItemStack.EMPTY, mainHand);
        Identifier skin = player.getSkin().body().texturePath();

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
                poseStack.mulPose(Axis.XP.rotationDegrees(forwardRotation * 30.0F));
                poseStack.translate(0.0F, 0.0F, -0.2F);
            }
            case INTERACT -> {
                float sideRotation;
                if (swingProgress > 0.5F) {
                    sideRotation = Mth.sin(swingProgress * Mth.PI + Mth.PI);
                } else {
                    sideRotation = Mth.sin((swingProgress * 3.0F) * Mth.PI);
                }

                poseStack.mulPose(
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
