package org.vivecraft.mod_compat_vr.iris.mixin.coderbot;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.PipelineManager;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Pseudo
@Mixin(net.coderbot.iris.pipeline.PipelineManager.class)
public class IrisPipelineManagerVRMixin implements PipelineManagerExtension {

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
    public void vivecraft$setShadowRenderTargets(Object targets) {
        vivecraft$shadowRenderTargets = (ShadowRenderTargets) targets;
    }

    @Unique
    private final Map<Object, Map<RenderPass, WorldRenderingPipeline>> vivecraft$vrPipelinesPerDimension = new HashMap<>();
    @Unique
    private WorldRenderingPipeline vivecraft$vanillaPipeline;
    @Unique
    private Map<RenderPass, WorldRenderingPipeline> vivecraft$vrPipelinesCurrentDimension;

    @Unique
    private WorldRenderPass vivecraft$currentWorldRenderPass = null;

    @Inject(method = "preparePipeline", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE), remap = false)
    private void vivecraft$generateVanillaPipeline(CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        // this also runs on game startup, when the renderpassManager isn't initialized yet
        if (VRState.vrInitialized && RenderPassManager.INSTANCE != null) {
            vivecraft$currentWorldRenderPass = RenderPassManager.wrp;
            RenderPass currentRenderPass = ClientDataHolderVR.getInstance().currentPass;
            RenderPassManager.setVanillaRenderPass();
            ClientDataHolderVR.getInstance().currentPass = currentRenderPass;
        }
    }

    @Group(name = "generateVRPipelines", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = DimensionId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER), remap = false, expect = 0)
    private void vivecraft$generateVRPipelines164(DimensionId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        vivecraft$generateVRPipelines(newDimension);
    }

    @Group(name = "generateVRPipelines", min = 1, max = 1)
    @Inject(target = @Desc(value = "preparePipeline", owner = PipelineManager.class, ret = WorldRenderingPipeline.class, args = NamespacedId.class), at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER), remap = false, expect = 0)
    private void vivecraft$generateVRPipelines165(NamespacedId newDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        vivecraft$generateVRPipelines(newDimension);
    }

    @Unique
    private void vivecraft$generateVRPipelines(Object newDimension) {
        if (VRState.vrInitialized) {
            vivecraft$vanillaPipeline = pipeline;
            if (!this.vivecraft$vrPipelinesPerDimension.containsKey(newDimension)) {
                vivecraft$vrPipelinesPerDimension.put(newDimension, new HashMap<>());
                vivecraft$vrPipelinesCurrentDimension = vivecraft$vrPipelinesPerDimension.get(newDimension);
                // main pipeline also sets this, but we don't want that, since it is unused
                vivecraft$shadowRenderTargets = null;

                for (RenderPass renderPass : RenderPass.values()) {
                    VRSettings.logger.info("Creating VR pipeline for dimension {}, RenderPass {}", newDimension, renderPass);
                    WorldRenderPass worldRenderPass = null;
                    switch (renderPass) {
                        case LEFT, RIGHT -> worldRenderPass = WorldRenderPass.stereoXR;
                        case CENTER -> worldRenderPass = WorldRenderPass.center;
                        case THIRD -> worldRenderPass = WorldRenderPass.mixedReality;
                        case SCOPEL -> worldRenderPass = WorldRenderPass.leftTelescope;
                        case SCOPER -> worldRenderPass = WorldRenderPass.rightTelescope;
                        case CAMERA -> worldRenderPass = WorldRenderPass.camera;
                        default -> {
                            VRSettings.logger.info("skipped VR pipeline for dimension {}, RenderPass {}, not used", newDimension, renderPass);
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
                } else if (ClientDataHolderVR.getInstance().currentPass == RenderPass.GUI) {
                    RenderPassManager.setGUIRenderPass();
                } else {
                    RenderPassManager.setVanillaRenderPass();
                }
            }
            vivecraft$vrPipelinesCurrentDimension = vivecraft$vrPipelinesPerDimension.get(newDimension);

            if (!RenderPassType.isVanilla()) {
                if (ClientDataHolderVR.getInstance().currentPass != null) {
                    pipeline = vivecraft$vrPipelinesCurrentDimension.get(ClientDataHolderVR.getInstance().currentPass);
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
        return vivecraft$vrPipelinesPerDimension.get(key).get(ClientDataHolderVR.getInstance().currentPass);
    }

    @Inject(method = "destroyPipeline", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"), remap = false)
    private void vivecraft$destroyVRPipelines(CallbackInfo ci) {
        vivecraft$vrPipelinesPerDimension.forEach((dimID, map) -> {
            map.forEach((renderPass, pipeline) -> {
                VRSettings.logger.info("Destroying VR pipeline {}", renderPass);
                resetTextureState();
                pipeline.destroy();
            });
            map.clear();
        });
        vivecraft$shadowRenderTargets = null;
        vivecraft$vrPipelinesPerDimension.clear();
        vivecraft$vanillaPipeline = null;
    }

    @Override
    @Unique
    public Object vivecraft$getVRPipeline(RenderPass pass) {
        return vivecraft$vrPipelinesCurrentDimension.get(pass);
    }

    @Override
    @Unique
    public Object vivecraft$getVanillaPipeline() {
        return vivecraft$vanillaPipeline;
    }
}
