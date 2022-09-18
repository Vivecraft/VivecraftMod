package org.vivecraft.modCompatMixin.irisMixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.iris.PipelineManagerExtension;
import org.vivecraft.render.RenderPass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Pseudo
@Mixin(net.coderbot.iris.pipeline.PipelineManager.class)
public class IrisPipelineManagerVRMixin implements PipelineManagerExtension {

    @Shadow(remap = false)
    private void resetTextureState(){}
    @Shadow(remap = false)
    private WorldRenderingPipeline pipeline;
    @Shadow(remap = false)
    @Final
    private Function<DimensionId, WorldRenderingPipeline> pipelineFactory;

    private ShadowRenderTargets shadowRenderTargets;
    public ShadowRenderTargets getShadowRenderTargets() {
        return shadowRenderTargets;
    }

    public void setShadowRenderTargets(ShadowRenderTargets targets) {
        shadowRenderTargets = targets;
    }

    private final Map<RenderPass, WorldRenderingPipeline> vrPipelines = new HashMap<>();

    @Inject(method = "preparePipeline", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private void generateVRPipelines(DimensionId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        RenderTarget current = Minecraft.getInstance().mainRenderTarget;
        // main pipeline also sets this, but we don't want that, since it is unused
        shadowRenderTargets = null;
        for (RenderPass renderPass : RenderPass.values()) {
            Iris.logger.info("Creating VR pipeline for dimension {}, RenderPass {}", newDimension, renderPass);
            switch (renderPass) {
                case LEFT:
                case RIGHT:
                    Minecraft.getInstance().mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.framebufferVrRender;
                    break;

                case CENTER:
                    Minecraft.getInstance().mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.framebufferUndistorted;
                    break;

                case THIRD:
                    Minecraft.getInstance().mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.framebufferMR;
                    break;

                case SCOPEL:
                    Minecraft.getInstance().mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferL;
                    break;

                case SCOPER:
                    Minecraft.getInstance().mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferR;
                    break;

                case CAMERA:
                    Minecraft.getInstance().mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.cameraRenderFramebuffer;
                    break;
                default:
                    Minecraft.getInstance().mainRenderTarget = null;
            }
            if (Minecraft.getInstance().mainRenderTarget == null) {
                Iris.logger.info("skipped VR pipeline for dimension {}, RenderPass {}, not used", newDimension, renderPass);
                continue;
            }
            pipeline = pipelineFactory.apply(newDimension);
            vrPipelines.put(renderPass, pipeline);
        }

        Minecraft.getInstance().mainRenderTarget = current;

        if (ClientDataHolder.getInstance().currentPass != null) {
            pipeline = vrPipelines.get(ClientDataHolder.getInstance().currentPass);
        } else {
            pipeline = vrPipelines.get(RenderPass.LEFT);
        }
    }
    @Inject(method = "preparePipeline", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, cancellable = true)
    private void returnCurrentVRPipeline(DimensionId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        pipeline = vrPipelines.get(ClientDataHolder.getInstance().currentPass);
        cir.setReturnValue(pipeline);
    }

    @Inject(method = "destroyPipeline", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"), remap = false)
    private void destroyVRPipelines(CallbackInfo ci) {
        vrPipelines.forEach((renderPass, pipeline) -> {
            Iris.logger.info("Destroying VR pipeline {}", renderPass);
            resetTextureState();
            pipeline.destroy();
        });
        shadowRenderTargets = null;
        vrPipelines.clear();
    }

    public WorldRenderingPipeline getVRPipeline(RenderPass pass){
        return vrPipelines.get(pass);
    }
}
