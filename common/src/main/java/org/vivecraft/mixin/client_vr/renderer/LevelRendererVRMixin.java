package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MultiPassTextureTarget;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.LevelTargetBundleExtension;
import org.vivecraft.client_vr.gameplay.trackers.InteractTracker;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.util.Set;

// priority 999 to inject before iris, for the vrFast rendering
@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Unique
    private static final ResourceLocation vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID = ResourceLocation.fromNamespaceAndPath(
        "vivecraft", "vrtransparency");

    @Unique
    private Entity vivecraft$renderedEntity;

    @Unique
    private boolean vivecraft$guiRendered = false;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private ClientLevel level;

    @Final
    @Shadow
    private RenderBuffers renderBuffers;

    @Shadow
    protected abstract void renderHitOutline(
        PoseStack poseStack, VertexConsumer consumer, Entity entity, double camX, double camY, double camZ,
        BlockPos pos, BlockState state, int color);

    @Shadow
    @Final
    private LevelTargetBundle targets;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void vivecraft$reinitVR(ResourceManager resourceManager, CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("Resource Reload");
        }
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;pollLightUpdates()V"))
    private void vivecraft$onePollLightUpdates(ClientLevel instance, Operation<Void> original) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().isFirstPass) {
            original.call(instance);
        }
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I"))
    private int vivecraft$oneLightingUpdates(LevelLightEngine instance, Operation<Integer> original) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().isFirstPass) {
            return original.call(instance);
        } else {
            return 0;
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;createInternal(Ljava/lang/String;Lcom/mojang/blaze3d/resource/ResourceDescriptor;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 0))
    private void vivecraft$addVRTargets(
        CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder,
        @Local RenderTargetDescriptor renderTargetDescriptor)
    {
        if (VRState.VR_RUNNING) {
            this.targets.replace(LevelTargetBundleExtension.OCCLUDED_TARGET_ID,
                frameGraphBuilder.createInternal("vroccluded", renderTargetDescriptor));
            this.targets.replace(LevelTargetBundleExtension.UNOCCLUDED_TARGET_ID,
                frameGraphBuilder.createInternal("vrunoccluded", renderTargetDescriptor));
            this.targets.replace(LevelTargetBundleExtension.HANDS_TARGET_ID,
                frameGraphBuilder.createInternal("vrhands", renderTargetDescriptor));
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V", shift = Shift.AFTER))
    private void vivecraft$addStencilPass(CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
        if (!RenderPassType.isVanilla()) {
            if (ClientDataHolderVR.getInstance().vrSettings.vrUseStencil) {
                FramePass framePass = frameGraphBuilder.addPass("vr_stencil");
                this.targets.main = framePass.readsAndWrites(this.targets.main);
                framePass.executes(() -> {
                    Profiler.get().popPush("stencil");
                    VREffectsHelper.drawEyeStencil();
                });
            }
        }
    }

    @ModifyExpressionValue(method = "collectVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
    private boolean vivecraft$dontCullPlayer(boolean doRender, @Local Entity entity) {
        return doRender ||
            (ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf && entity == Minecraft.getInstance().player);
    }

    @ModifyExpressionValue(method = "collectVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"))
    private boolean vivecraft$noPlayerWhenSleeping(boolean isSleeping) {
        // no self render, we don't want an out-of-body experience
        return isSleeping && !RenderPassType.isVanilla();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void vivecraft$storeEntityAndRestorePos(
        CallbackInfo ci, @Local(argsOnly = true) Entity entity,
        @Share("capturedEntity") LocalRef<Entity> capturedEntity)
    {
        if (!RenderPassType.isVanilla() && entity == this.minecraft.getCameraEntity()) {
            capturedEntity.set(entity);
            ((GameRendererExtension) this.minecraft.gameRenderer).vivecraft$restoreRVEPos(capturedEntity.get());
        }
        this.vivecraft$renderedEntity = entity;
    }

    @Inject(method = "renderEntity", at = @At("TAIL"))
    private void vivecraft$clearEntityAndSetupPos(
        CallbackInfo ci, @Local(argsOnly = true) Entity entity,
        @Share("capturedEntity") LocalRef<Entity> capturedEntity)
    {
        if (capturedEntity.get() != null) {
            ((GameRendererExtension) this.minecraft.gameRenderer).vivecraft$cacheRVEPos(capturedEntity.get());
            ((GameRendererExtension) this.minecraft.gameRenderer).vivecraft$setupRVE();
        }
        this.vivecraft$renderedEntity = null;
    }

    // no remap needed to make the * work
    @Inject(method = {"method_62202*", "addMainPass*"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;readsAndWrites(Lcom/mojang/blaze3d/resource/ResourceHandle;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 0, remap = true), remap = false)
    public void vivecraft$markVRTargetsForWrite(CallbackInfo ci, @Local FramePass framePass) {
        if (VRState.VR_RUNNING && this.targets instanceof LevelTargetBundleExtension ext) {
            if (ext.vivecraft$getOccluded() != null) {
                this.targets.replace(LevelTargetBundleExtension.OCCLUDED_TARGET_ID,
                    framePass.readsAndWrites(ext.vivecraft$getOccluded()));
            }
            if (ext.vivecraft$getUnoccluded() != null) {
                this.targets.replace(LevelTargetBundleExtension.UNOCCLUDED_TARGET_ID,
                    framePass.readsAndWrites(ext.vivecraft$getUnoccluded()));
            }
            if (ext.vivecraft$getHands() != null) {
                this.targets.replace(LevelTargetBundleExtension.HANDS_TARGET_ID,
                    framePass.readsAndWrites(ext.vivecraft$getHands()));
            }
            // fix vanilla bug https://bugs.mojang.com/browse/MC-278096, is fixed in 1.21.5
            if (this.targets.clouds != null && this.minecraft.options.getCloudsType() == CloudStatus.OFF) {
                this.targets.clouds = framePass.readsAndWrites(this.targets.clouds);
            }
        }
    }

    // no remap needed to make the * work
    @Inject(method = {
        "method_62214*", // fabric
        "lambda$addMainPass$1*", // forge
        "lambda$addMainPass$2*" // neoforge
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = Shift.AFTER, remap = true), remap = false)
    private void vivecraft$interactOutlineSolid(
        CallbackInfo ci, @Local(argsOnly = true) Camera camera, @Local PoseStack poseStack)
    {
        vivecraft$interactOutline(camera, poseStack, false);
    }

    // no remap needed to make the * work
    @Inject(method = {
        "method_62214*", // fabric
        "lambda$addMainPass$1*", // forge
        "lambda$addMainPass$2*" // neoforge
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 2, remap = true), remap = false)
    private void vivecraft$interactOutlineTranslucent(
        CallbackInfo ci, @Local(argsOnly = true) Camera camera, @Local PoseStack poseStack)
    {
        vivecraft$interactOutline(camera, poseStack, true);
    }

    @Unique
    private void vivecraft$interactOutline(Camera camera, PoseStack poseStack, boolean sort) {
        if (RenderPassType.isVanilla()) return;

        Profiler.get().popPush("interact outline");
        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginOutlineShader();
        }

        InteractTracker interactTracker = ClientDataHolderVR.getInstance().interactTracker;

        for (int c = 0; c < 2; c++) {
            if (interactTracker.isInteractActive(c) &&
                (interactTracker.inBlockHit[c] != null || interactTracker.bukkit[c]))
            {
                BlockPos blockpos = interactTracker.inBlockHit[c] != null ?
                    interactTracker.inBlockHit[c].getBlockPos() : BlockPos.containing(
                    ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getController(c).getPosition());
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (sort == ItemBlockRenderTypes.getChunkRenderType(blockstate).sortOnUpload()) {
                    this.renderHitOutline(poseStack,
                        this.renderBuffers.bufferSource().getBuffer(RenderType.lines()),
                        camera.getEntity(),
                        camera.getPosition().x,
                        camera.getPosition().y,
                        camera.getPosition().z,
                        blockpos, blockstate,
                        0x66FFFFFF);
                }
            }
        }
        this.renderBuffers.bufferSource().endBatch(RenderType.lines());
        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endOutlineShader();
        }
    }

    // no remap needed to make the * work
    @Inject(method = {
        "method_62214*", // fabric
        "lambda$addMainPass$1*", // forge
        "lambda$addMainPass$2*" // neoforge
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 0, shift = Shift.AFTER, remap = true), remap = false)
    private void vivecraft$renderVrStuffPart1(CallbackInfo ci, @Local(ordinal = 0) float partialTick) {
        if (RenderPassType.isVanilla()) return;

        if (this.targets.translucent != null) {
            // fix vanilla bug https://bugs.mojang.com/browse/MC-278096, is fixed in 1.21.5
            if (this.targets.clouds != null && this.minecraft.options.getCloudsType() == CloudStatus.OFF) {
                this.targets.clouds.get().clear();
            }
            VREffectsHelper.renderVRFabulous(partialTick, this.targets);
        } else {
            VREffectsHelper.renderVrFast(partialTick, false);
            if (ShadersHelper.isShaderActive() && ClientDataHolderVR.getInstance().vrSettings.shaderGUIRender ==
                VRSettings.ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID)
            {
                // shaders active, and render gui before translucents
                VREffectsHelper.renderVrFast(partialTick, true);
                this.vivecraft$guiRendered = true;
            }
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCloudsType()Lnet/minecraft/client/CloudStatus;"))
    private void vivecraft$renderVrStuffPart2(
        CallbackInfo ci, @Local(ordinal = 0) float partialTick, @Local FrameGraphBuilder frameGraphBuilder)
    {
        if (RenderPassType.isVanilla()) return;

        if (this.targets.translucent == null && (!ShadersHelper.isShaderActive() ||
            ClientDataHolderVR.getInstance().vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.AFTER_TRANSLUCENT
        ))
        {
            // no shaders, or shaders, and gui after translucents
            FramePass framePass = frameGraphBuilder.addPass("vr stuff part2");
            this.targets.main = framePass.readsAndWrites(this.targets.main);
            framePass.executes(() -> VREffectsHelper.renderVrFast(partialTick, true));
            this.vivecraft$guiRendered = true;
        }
    }

    // if the gui didn't render yet, render it now.
    // or if shaders are on, and option AFTER_SHADER is selected
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void vivecraft$renderVrStuffFinal(
        CallbackInfo ci, @Local(ordinal = 0) float partialTick)
    {
        if (RenderPassType.isVanilla()) return;

        if (!this.vivecraft$guiRendered && !Minecraft.useShaderTransparency()) {
            // re set up modelView, since this is after everything got cleared
            RenderSystem.getModelViewStack().pushMatrix().identity();
            RenderHelper.applyVRModelView(ClientDataHolderVR.getInstance().currentPass,
                RenderSystem.getModelViewStack());

            VREffectsHelper.renderVrFast(partialTick, true);

            RenderSystem.getModelViewStack().popMatrix();
        }
        // reset for next frame
        this.vivecraft$guiRendered = false;
    }

    @WrapOperation(method = "initOutline", at = @At(value = "NEW", target = "com/mojang/blaze3d/pipeline/TextureTarget"))
    private TextureTarget vivecraft$multiPassOutlineTarget(
        int width, int height, boolean useDepth, Operation<TextureTarget> original)
    {
        if (VRState.VR_INITIALIZED) {
            return new MultiPassTextureTarget(width, height, useDepth);
        } else {
            return original.call(width, height, useDepth);
        }
    }

    @WrapOperation(method = "getTransparencyChain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/ResourceLocation;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain vivecraft$vrTransparency(
        ShaderManager instance, ResourceLocation id, Set<ResourceLocation> externalTargets,
        Operation<PostChain> original)
    {
        if (VRState.VR_RUNNING) {
            return original.call(instance, vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID,
                LevelTargetBundleExtension.VR_TARGETS);
        } else {
            return original.call(instance, id, externalTargets);
        }
    }

    @Override
    @Unique
    public Entity vivecraft$getRenderedEntity() {
        return this.vivecraft$renderedEntity;
    }
}
