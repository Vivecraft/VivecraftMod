package org.vivecraft.mod_compat_vr.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.PipelineManager;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrInitialized;

@Pseudo
@Mixin(net.coderbot.iris.pipeline.PipelineManager.class)
public class IrisPipelineManagerVRMixin implements org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension {

    @Shadow(remap = false)
    private void resetTextureState() {
    }

    @Shadow(remap = false)
    private WorldRenderingPipeline pipeline;
    @Shadow(remap = false)
    @Final
    private Function<Object, WorldRenderingPipeline> pipelineFactory;

    @Unique
    private ShadowRenderTargets vivecraft$shadowRenderTargets;

    @Override
    @Unique
    public ShadowRenderTargets vivecraft$getShadowRenderTargets() {
        return vivecraft$shadowRenderTargets;
    }

    @Override
    @Unique
    public void vivecraft$setShadowRenderTargets(ShadowRenderTargets targets) {
        vivecraft$shadowRenderTargets = targets;
    }

    @Unique
    private final Map<Object, Map<RenderPass, WorldRenderingPipeline>> vivecraft$vrPipelinesPerDimension = new HashMap<>();
    @Unique
    private WorldRenderingPipeline vivecraft$vanillaPipeline;
    @Unique
    private Map<RenderPass, WorldRenderingPipeline> vivecraft$vrPipelinesCurrentDimension;

    @Unique
    private WorldRenderPass vivecraft$currentWorldRenderPass;

    @Inject(method = "preparePipeline", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", shift = Shift.BEFORE), remap = false)
    private void vivecraft$generateVanillaPipeline(CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        // this also runs on game startup, when the renderpassManager isn't initialized yet
        if (vrInitialized && RenderPassManager.INSTANCE != null) {
            vivecraft$currentWorldRenderPass = RenderPassManager.wrp;
            RenderPass currentRenderPass = dh.currentPass;
            RenderPassManager.setVanillaRenderPass();
            dh.currentPass = currentRenderPass;
        }
    }

    @Group(name = "generateVRPipelines", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = DimensionId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = Shift.AFTER), remap = false, expect = 0)
    private void vivecraft$generateVRPipelines164(DimensionId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        vivecraft$generateVRPipelines(newDimension);
    }

    @Group(name = "generateVRPipelines", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = NamespacedId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = Shift.AFTER), remap = false, expect = 0)
    private void vivecraft$generateVRPipelines165(NamespacedId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        vivecraft$generateVRPipelines(newDimension);
    }

    @Unique
    private void vivecraft$generateVRPipelines(Object newDimension) {
        if (vrInitialized) {
            vivecraft$vanillaPipeline = pipeline;
            if (!this.vivecraft$vrPipelinesPerDimension.containsKey(newDimension)) {
                vivecraft$vrPipelinesPerDimension.put(newDimension, new HashMap<>());
                vivecraft$vrPipelinesCurrentDimension = vivecraft$vrPipelinesPerDimension.get(newDimension);
                // main pipeline also sets this, but we don't want that, since it is unused
                vivecraft$shadowRenderTargets = null;

                for (RenderPass renderPass : RenderPass.values()) {
                    Iris.logger.info("Creating VR pipeline for dimension {}, RenderPass {}", newDimension, renderPass);
                    WorldRenderPass worldRenderPass = null;
                    switch (renderPass) {
                        case LEFT, RIGHT -> worldRenderPass = WorldRenderPass.stereoXR;
                        case CENTER -> worldRenderPass = WorldRenderPass.center;
                        case THIRD -> worldRenderPass = WorldRenderPass.mixedReality;
                        case SCOPEL -> worldRenderPass = WorldRenderPass.leftTelescope;
                        case SCOPER -> worldRenderPass = WorldRenderPass.rightTelescope;
                        case CAMERA -> worldRenderPass = WorldRenderPass.camera;
                        default -> {
                            Iris.logger.info("skipped VR pipeline for dimension {}, RenderPass {}, not used", newDimension, renderPass);
                            continue;
                        }
                    }

                    if (worldRenderPass != null) {
                        RenderPassManager.setWorldRenderPass(worldRenderPass);
                    } else {
                        continue;
                    }

                    WorldRenderingPipeline pipe = pipelineFactory.apply(newDimension);
                    vivecraft$vrPipelinesPerDimension.get(newDimension).put(renderPass, pipe);
                }
                // set to currently needed renderpass again
                if (vivecraft$currentWorldRenderPass != null) {
                    RenderPassManager.setWorldRenderPass(vivecraft$currentWorldRenderPass);
                } else if (dh.currentPass == RenderPass.GUI) {
                    RenderPassManager.setGUIRenderPass();
                } else {
                    RenderPassManager.setVanillaRenderPass();
                }
            }
            vivecraft$vrPipelinesCurrentDimension = vivecraft$vrPipelinesPerDimension.get(newDimension);

            if (!RenderPassType.isVanilla()) {
                if (dh.currentPass != null) {
                    pipeline = vivecraft$vrPipelinesCurrentDimension.get(dh.currentPass);
                } else {
                    pipeline = vivecraft$vrPipelinesCurrentDimension.get(RenderPass.LEFT);
                }
            }
        }
    }

    @Group(name = "returnCurrentVRPipeline", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = DimensionId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, cancellable = true, expect = 0)
    private void vivecraft$returnCurrentVRPipeline164(DimensionId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (!RenderPassType.isVanilla()) {
            pipeline = vivecraft$getCurrentVRPipeline(newDimension);
            cir.setReturnValue(pipeline);
        }
    }

    @Group(name = "returnCurrentVRPipeline", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = NamespacedId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, cancellable = true, expect = 0)
    private void vivecraft$returnCurrentVRPipeline165(NamespacedId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (!RenderPassType.isVanilla()) {
            pipeline = vivecraft$getCurrentVRPipeline(newDimension);
            cir.setReturnValue(pipeline);
        }
    }

    @Unique
    private WorldRenderingPipeline vivecraft$getCurrentVRPipeline(Object key) {
        return vivecraft$vrPipelinesPerDimension.get(key).get(dh.currentPass);
    }

    @Inject(method = "destroyPipeline", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"), remap = false)
    private void vivecraft$destroyVRPipelines(CallbackInfo ci) {
        if (vrInitialized) {
            vivecraft$vrPipelinesPerDimension.forEach((dimID, map) -> {
                map.forEach((renderPass, pipeline) -> {
                    Iris.logger.info("Destroying VR pipeline {}", renderPass);
                    resetTextureState();
                    pipeline.destroy();
                });
                map.clear();
            });
            vivecraft$shadowRenderTargets = null;
            vivecraft$vrPipelinesPerDimension.clear();
            vivecraft$vanillaPipeline = null;
        }
    }

    @Override
    @Unique
    public WorldRenderingPipeline vivecraft$getVRPipeline(RenderPass pass) {
        return vivecraft$vrPipelinesCurrentDimension.get(pass);
    }

    @Override
    @Unique
    public WorldRenderingPipeline vivecraft$getVanillaPipeline() {
        return vivecraft$vanillaPipeline;
    }
}
