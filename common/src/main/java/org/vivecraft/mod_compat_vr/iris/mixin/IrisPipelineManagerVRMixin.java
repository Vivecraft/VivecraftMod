package org.vivecraft.mod_compat_vr.iris.mixin;

import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.PipelineManager;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.shadows.ShadowRenderTargets;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrInitialized;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(net.coderbot.iris.pipeline.PipelineManager.class)
public class IrisPipelineManagerVRMixin implements org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension {

    @Shadow(remap = false)
    private void resetTextureState(){}
    @Shadow(remap = false)
    private WorldRenderingPipeline pipeline;
    @Shadow(remap = false)
    @Final
    private Function<Object, WorldRenderingPipeline> pipelineFactory;

    private ShadowRenderTargets shadowRenderTargets;
    public ShadowRenderTargets getShadowRenderTargets() {
        return shadowRenderTargets;
    }

    public void setShadowRenderTargets(ShadowRenderTargets targets) {
        shadowRenderTargets = targets;
    }

    private final Map<Object, Map<RenderPass, WorldRenderingPipeline>> vrPipelinesPerDimension = new HashMap<>();
    private WorldRenderingPipeline vanillaPipeline;
    private  Map<RenderPass, WorldRenderingPipeline> vrPipelinesCurrentDimension;

    private WorldRenderPass currentWorldRenderPass;
    @Inject(method = "preparePipeline", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", shift = Shift.BEFORE), remap = false)
    private void generateVanillaPipeline(CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        // this also runs on game startup, when the renderpassManager isn't initialized yet
        if (vrInitialized && RenderPassManager.INSTANCE != null) {
            currentWorldRenderPass = RenderPassManager.wrp;
            RenderPass currentRenderPass = dh.currentPass;
            RenderPassManager.setVanillaRenderPass();
            dh.currentPass = currentRenderPass;
        }
    }

    @Group(name = "generateVRPipelines", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = DimensionId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = Shift.AFTER), remap = false, expect = 0)
    private void generateVRPipelines164(DimensionId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        generateVRPipelines(newDimension);
    }

    @Group(name = "generateVRPipelines", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = NamespacedId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = Shift.AFTER), remap = false, expect = 0)
    private void generateVRPipelines165(NamespacedId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        generateVRPipelines(newDimension);
    }

    @Unique
    private void generateVRPipelines(Object newDimension) {
        if (vrInitialized) {
            vanillaPipeline = pipeline;
            if (!this.vrPipelinesPerDimension.containsKey(newDimension)) {
                vrPipelinesPerDimension.put(newDimension, new HashMap<>());
                vrPipelinesCurrentDimension = vrPipelinesPerDimension.get(newDimension);
                // main pipeline also sets this, but we don't want that, since it is unused
                shadowRenderTargets = null;

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
                    vrPipelinesPerDimension.get(newDimension).put(renderPass, pipe);
                }
                // set to currently needed renderpass again
                if (currentWorldRenderPass != null) {
                    RenderPassManager.setWorldRenderPass(currentWorldRenderPass);
                } else if (dh.currentPass == RenderPass.GUI) {
                    RenderPassManager.setGUIRenderPass();
                } else {
                    RenderPassManager.setVanillaRenderPass();
                }
            }
            vrPipelinesCurrentDimension = vrPipelinesPerDimension.get(newDimension);

            if (!RenderPassType.isVanilla()) {
                if (dh.currentPass != RenderPass.VANILLA) {
                    pipeline = vrPipelinesCurrentDimension.get(dh.currentPass);
                } else {
                    pipeline = vrPipelinesCurrentDimension.get(RenderPass.LEFT);
                }
            }
        }
    }

    @Group(name = "returnCurrentVRPipeline", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = DimensionId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, cancellable = true, expect = 0)
    private void returnCurrentVRPipeline164(DimensionId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (!RenderPassType.isVanilla()) {
            pipeline = getCurrentVRPipeline(newDimension);
            cir.setReturnValue(pipeline);
        }
    }

    @Group(name = "returnCurrentVRPipeline", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = NamespacedId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, cancellable = true, expect = 0)
    private void returnCurrentVRPipeline165(NamespacedId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (!RenderPassType.isVanilla()) {
            pipeline = getCurrentVRPipeline(newDimension);
            cir.setReturnValue(pipeline);
        }
    }

    @Unique
    private WorldRenderingPipeline getCurrentVRPipeline(Object key) {
        return vrPipelinesPerDimension.get(key).get(dh.currentPass);
    }

    @Inject(method = "destroyPipeline", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"), remap = false)
    private void destroyVRPipelines(CallbackInfo ci) {
        if (vrInitialized) {
            vrPipelinesPerDimension.forEach((dimID, map) -> {
                map.forEach((renderPass, pipeline) -> {
                    Iris.logger.info("Destroying VR pipeline {}", renderPass);
                    resetTextureState();
                    pipeline.destroy();
                });
                map.clear();
            });
            shadowRenderTargets = null;
            vrPipelinesPerDimension.clear();
            vanillaPipeline = null;
        }
    }

    public WorldRenderingPipeline getVRPipeline(RenderPass pass){
        return vrPipelinesCurrentDimension.get(pass);
    }

    public WorldRenderingPipeline getVanillaPipeline(){
        return vanillaPipeline;
    }
}
