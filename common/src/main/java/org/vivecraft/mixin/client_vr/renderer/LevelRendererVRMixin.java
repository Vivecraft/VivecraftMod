package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.LevelRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MultiPassTextureTarget;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import java.util.Optional;
import java.util.OptionalDouble;

// priority 1010 to inject after iris, for the VrStuffFinal rendering
@Mixin(value = LevelRenderer.class, priority = 1010)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Shadow
    protected abstract void submitHitOutline(
        PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType,
        BlockOutlineRenderState state, int color, float width, boolean afterTerrain);

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
    private LevelRenderState levelRenderState;

    @Shadow
    @Final
    private GameRenderer gameRenderer;

    @Inject(method = "resize", at = @At("TAIL"))
    private void vivecraft$reinitVR(CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffersMaybe("Resource Reload");
        }
    }

    @Inject(method = "render", at = @At(value = "NEW", target = "org/joml/Matrix4f"))
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

    @Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void vivecraft$interactOutlineSolid(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState,
        @Local(argsOnly = true) SubmitNodeCollector output, @Local PoseStack poseStack)
    {
        if (RenderPassType.isVanilla()) return;

        Profiler.get().popPush("interact outline");

        BlockOutlineRenderState[] outlines = ((LevelRenderStateExtension) levelRenderState).vivecraft$getInteractOutlineStates();

        for (int c = 0; c < 2; c++) {
            if (outlines[c] != null) {
                this.submitHitOutline(poseStack,
                    output,
                    RenderTypes.lines(),
                    outlines[c],
                    0x66FFFFFF,
                    this.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth,
                    outlines[c].isTranslucent());
            }
        }
    }

    @Inject(method = "submitFeatures*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
    private void vivecraft$renderVrStuffPart1(
        CallbackInfo ci, @Local(argsOnly = true) LevelRenderState levelRenderState, @Local PoseStack poseStack)
    {
        if (RenderPassType.isVanilla()) return;

        if (this.gameRenderer.useImprovedTransparency()) {
            VREffectsHelper.renderVROIT(this.submitNodeStorage, levelRenderState, poseStack);
        } else {
            VREffectsHelper.renderVrFast(this.submitNodeStorage, levelRenderState, poseStack, false);
        }
    }

    // if the gui didn't render yet, render it now.
    // or if shaders are on, and option AFTER_SHADER is selected
    @Inject(method = "render", at = @At("RETURN"))
    private void vivecraft$renderVrStuffFinal(CallbackInfo ci) {
        if (RenderPassType.isVanilla()) return;
        VRRenderState vrState = ((LevelRenderStateExtension) this.levelRenderState).vivecraft$getVRRenderState();

        if (vrState.uiAfterWorld) {
            // re set up modelView, since this is after everything got cleared
            RenderSystem.getModelViewStack().pushMatrix().identity();
            RenderHelper.applyVRModelView(vrState.currentPass, RenderSystem.getModelViewStack());

            VREffectsHelper.renderVrFast(this.submitNodeStorage, this.levelRenderState, new PoseStack(), true);
            // actuallyrender the stuff
            try (FeatureRenderDispatcher.PreparedFrame featureFrame = this.featureRenderDispatcher.prepareFrame(
                this.submitNodeStorage))
            {
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                    .createRenderPass(() -> "Vivecraft vr stuff final",
                        this.gameRenderer.mainRenderTarget.getColorTextureView(), Optional.empty(),
                        this.gameRenderer.mainRenderTarget.getDepthTextureView(), OptionalDouble.empty()))
                {
                    FeatureRenderDispatcher.renderAllFeatures(renderPass, featureFrame);
                }
            }

            RenderSystem.getModelViewStack().popMatrix();
        }
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "com/mojang/blaze3d/pipeline/TextureTarget"))
    private TextureTarget vivecraft$multiPassOutlineTarget(
        String label, int width, int height, GpuFormat colorFormat, GpuFormat depthFormat,
        Operation<TextureTarget> original)
    {
        return new MultiPassTextureTarget(label, width, height, colorFormat, depthFormat);
    }

    @Unique
    @Override
    public void vivecraft$renderGizmos(
        CameraRenderState cameraState, SubmitNodeStorage output, FeatureRenderDispatcher dispatcher)
    {

        this.finalizeGizmoCollection();
        // submit
        this.finalizedGizmos.standardPrimitives().submit(output, cameraState, false);
        this.finalizedGizmos.alwaysOnTopPrimitives().submit(output, cameraState, true);

        // render
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Gizmos",
            this.gameRenderer.mainRenderTarget.getColorTextureView(), Optional.empty(),
            this.gameRenderer.mainRenderTarget.getDepthTextureView(), OptionalDouble.of(0.0)))
        {
            RenderSystem.bindDefaultUniforms(renderPass);
            try (FeatureRenderDispatcher.PreparedFrame featureFrame = dispatcher.prepareFrame(output)) {
                featureFrame.executeSolid(renderPass);
                featureFrame.executeTranslucent(renderPass);
                featureFrame.executeTranslucentAfterTerrain(renderPass);
                featureFrame.executeSeeThrough(renderPass);

                // always on top gizmos
                if (!this.finalizedGizmos.alwaysOnTopPrimitives().isEmpty()) {
                    RenderSystem.getDevice().createCommandEncoder()
                        .clearDepthTexture(this.gameRenderer.mainRenderTarget.getDepthTexture(), 0.0);
                    featureFrame.executeAlwaysOnTop(renderPass);
                }
            }
        }
    }
}
