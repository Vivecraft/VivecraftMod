package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.extensions.FeatureRenderDispatcherExtension;
import org.vivecraft.client.extensions.LevelRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MultiPassTextureTarget;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.LevelTargetBundleExtension;
import org.vivecraft.client_vr.gameplay.interact_modules.BlockInteractionModule;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.util.Set;

// priority 990 to inject before iris, for the vrFast rendering
@Mixin(value = LevelRenderer.class, priority = 990)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Unique
    private static final Identifier vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID = Identifier.fromNamespaceAndPath(
        "vivecraft", "vrtransparency");

    @Unique
    private boolean vivecraft$guiRendered = false;

    @Shadow
    private ClientLevel level;

    @Final
    @Shadow
    private RenderBuffers renderBuffers;

    @Shadow
    protected abstract void renderHitOutline(
        PoseStack poseStack, VertexConsumer builder, double camX, double camY, double camZ,
        BlockOutlineRenderState state, int color, float width);

    @Shadow
    @Final
    private LevelTargetBundle targets;

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Shadow
    @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Shadow
    protected abstract void finalizeGizmoCollection();

    @Shadow
    private LevelRenderer.FinalizedGizmos finalizedGizmos;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void vivecraft$reinitVR(ResourceManager resourceManager, CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffersMaybe("Resource Reload");
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

    @ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
    private boolean vivecraft$dontCullPlayer(boolean doRender, @Local Entity entity) {
        return doRender ||
            (ClientDataHolderVR.getInstance().vrSettings.shouldRenderSelf && entity == Minecraft.getInstance().player);
    }

    @ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"))
    private boolean vivecraft$noPlayerWhenSleeping(boolean isSleeping) {
        // no self render, we don't want an out-of-body experience
        return isSleeping && RenderPassType.isVanilla();
    }

    @Inject(method = "addMainPass*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;readsAndWrites(Lcom/mojang/blaze3d/resource/ResourceHandle;)Lcom/mojang/blaze3d/resource/ResourceHandle;", ordinal = 0))
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
        }
    }

    @Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", shift = Shift.AFTER))
    private void vivecraft$interactOutlineSolid(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState, @Local PoseStack poseStack)
    {
        vivecraft$interactOutline(levelRenderState, poseStack, false);
    }

    @Inject(method = "lambda$addMainPass$0*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", ordinal = 2))
    private void vivecraft$interactOutlineTranslucent(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState, @Local PoseStack poseStack)
    {
        vivecraft$interactOutline(levelRenderState, poseStack, true);
    }

    @Unique
    private void vivecraft$interactOutline(LevelRenderState levelRenderState, PoseStack poseStack, boolean sort) {
        if (RenderPassType.isVanilla()) return;

        Profiler.get().popPush("interact outline");
        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginOutlineShader();
        }

        BlockOutlineRenderState[] outlines = ((LevelRenderStateExtension) levelRenderState).vivecraft$getInteractOutlineStates();

        for (int c = 0; c < 2; c++) {
            if (outlines[c] != null) {
                if (sort == outlines[c].isTranslucent()) {
                    this.renderHitOutline(poseStack,
                        this.renderBuffers.bufferSource().getBuffer(RenderTypes.lines()),
                        levelRenderState.cameraRenderState.pos.x,
                        levelRenderState.cameraRenderState.pos.y,
                        levelRenderState.cameraRenderState.pos.z,
                        outlines[c],
                        0x66FFFFFF,
                        Minecraft.getInstance().getWindow().getAppropriateLineWidth());
                }
            }
        }
        this.renderBuffers.bufferSource().endBatch(RenderTypes.lines());
        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endOutlineShader();
        }
    }

    @Inject(method = "extractBlockOutline", at = @At("HEAD"))
    private void vivecraft$extractInteractOutline(
        CallbackInfo ci, @Local(argsOnly = true) Camera camera,
        @Local(argsOnly = true) LevelRenderState levelRenderState)
    {
        if (RenderPassType.isVanilla()) return;

        BlockInteractionModule blockModule = ClientDataHolderVR.getInstance().blockModule;
        for (int c = 0; c < 2; c++) {
            if (blockModule.isActive(c)) {
                BlockPos blockPos = blockModule.inBlockHit[c] != null ? blockModule.inBlockHit[c].getBlockPos() :
                    BlockPos.containing(
                        ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getController(c).getPosition());
                BlockState blockState = this.level.getBlockState(blockPos);
                BlockStateModel blockStateModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet()
                    .get(blockState);
                ((LevelRenderStateExtension) levelRenderState).vivecraft$setInteractOutlineState(c,
                    new BlockOutlineRenderState(blockPos,
                        blockStateModel.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT), false,
                        blockState.getShape(this.level, blockPos, CollisionContext.of(camera.entity()))));
            } else {
                ((LevelRenderStateExtension) levelRenderState).vivecraft$setInteractOutlineState(c, null);
            }
        }
    }

    @Inject(method = "lambda$addMainPass$0*", at = @At("TAIL"))
    private void vivecraft$renderVrFabulous(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState, @Local PoseStack poseStack)
    {
        if (RenderPassType.isVanilla() || this.targets.translucent == null) return;

        VREffectsHelper.renderVRFabulous(this.featureRenderDispatcher, this.submitNodeStorage, levelRenderState,
            poseStack, this.targets);
    }

    @Inject(method = "lambda$addMainPass$0*", at = @At(value = "CONSTANT", args = "stringValue=renderSolidFeatures"))
    private void vivecraft$renderVrStuffPart1(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState, @Local PoseStack poseStack)
    {
        if (RenderPassType.isVanilla() || this.targets.translucent != null) return;

        VREffectsHelper.renderVrFast(this.submitNodeStorage, levelRenderState, poseStack, false);
    }

    @Inject(method = "lambda$addMainPass$0*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderTranslucentParticles()V"))
    private void vivecraft$renderLateCustomGeometry(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ((FeatureRenderDispatcherExtension) this.featureRenderDispatcher).vivecraft$renderLate();
            this.renderBuffers.bufferSource().endBatch();
        }
    }

    // if the gui didn't render yet, render it now.
    // or if shaders are on, and option AFTER_SHADER is selected
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void vivecraft$renderVrStuffFinal(CallbackInfo ci) {
        if (RenderPassType.isVanilla()) return;
        VRRenderState vrState = ((LevelRenderStateExtension) this.levelRenderState).vivecraft$getVRRenderState();

        if (vrState.uiAfterWorld) {
            // re set up modelView, since this is after everything got cleared
            RenderSystem.getModelViewStack().pushMatrix().identity();
            RenderHelper.applyVRModelView(vrState.currentPass, RenderSystem.getModelViewStack());

            VREffectsHelper.renderVrFast(this.submitNodeStorage, this.levelRenderState, new PoseStack(), true);
            // actuallyrender the stuff
            this.featureRenderDispatcher.renderAllFeatures();
            this.renderBuffers.bufferSource().endBatch();

            RenderSystem.getModelViewStack().popMatrix();
        }
        // reset for next frame
        this.vivecraft$guiRendered = false;
    }

    @WrapOperation(method = "initOutline", at = @At(value = "NEW", target = "com/mojang/blaze3d/pipeline/TextureTarget"))
    private TextureTarget vivecraft$multiPassOutlineTarget(
        String name, int width, int height, boolean useDepth, Operation<TextureTarget> original)
    {
        if (VRState.VR_INITIALIZED) {
            return new MultiPassTextureTarget(name, width, height, useDepth);
        } else {
            return original.call(name, width, height, useDepth);
        }
    }

    @WrapOperation(method = "getTransparencyChain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain vivecraft$vrTransparency(
        ShaderManager instance, Identifier id, Set<Identifier> externalTargets,
        Operation<PostChain> original)
    {
        if (VRState.VR_RUNNING) {
            return original.call(instance, vivecraft$VR_TRANSPARENCY_POST_CHAIN_ID,
                LevelTargetBundleExtension.VR_TARGETS);
        } else {
            return original.call(instance, id, externalTargets);
        }
    }

    @Inject(method = "getCloudsTarget", at = @At("HEAD"), cancellable = true)
    private void vivecraft$getCloudsTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null &&
            ClientDataHolderVR.getInstance().menuWorldRenderer.isRendering())
        {
            cir.setReturnValue(null);
        }
    }

    @Unique
    @Override
    public void vivecraft$renderGizmos(
        PoseStack poseStack, CameraRenderState cameraState, Matrix4fc modelViewMatrix)
    {
        RenderSystem.outputColorTextureOverride = this.minecraft.mainRenderTarget.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = this.minecraft.mainRenderTarget.getDepthTextureView();

        this.finalizeGizmoCollection();
        // regular gizmos
        this.finalizedGizmos.standardPrimitives()
            .render(poseStack, this.renderBuffers.bufferSource(), cameraState, modelViewMatrix);
        this.renderBuffers.bufferSource().endBatch();

        // always on top gizmos
        if (!this.finalizedGizmos.alwaysOnTopPrimitives().isEmpty()) {
            RenderSystem.getDevice().createCommandEncoder()
                .clearDepthTexture(this.minecraft.mainRenderTarget.getDepthTexture(), 1.0);
            this.finalizedGizmos.alwaysOnTopPrimitives()
                .render(poseStack, this.renderBuffers.bufferSource(), cameraState, modelViewMatrix);
            this.renderBuffers.bufferSource().endBatch();
        }
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
    }
}
