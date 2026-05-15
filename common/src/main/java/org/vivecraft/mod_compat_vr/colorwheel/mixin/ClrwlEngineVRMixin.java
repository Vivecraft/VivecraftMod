package org.vivecraft.mod_compat_vr.colorwheel.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.djefrey.colorwheel.compile.ClrwlPrograms;
import dev.djefrey.colorwheel.engine.BeginTranslucentRenderFunction;
import dev.djefrey.colorwheel.engine.ClrwlEngine;
import dev.djefrey.colorwheel.instancing.ClrwlInstancedDrawManager;
import dev.engine_room.flywheel.backend.compile.FlwPrograms;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.colorwheel.extensions.ClrwlInstancedDrawManagerExtension;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

@Mixin(targets = "dev.djefrey.colorwheel.engine.ClrwlEngine")
public class ClrwlEngineVRMixin {

    @Shadow
    public static Map<IrisRenderingPipeline, ClrwlEngine> ENGINES;

    @Shadow
    @Final
    private ClrwlInstancedDrawManager drawManager;

    @Shadow
    @Final
    private ShaderPack pack;

    @Shadow
    @Final
    private NamespacedId dimension;

    @Unique
    private final Map<RenderPass, IrisRenderingPipeline> vivecraft$pipelines = new EnumMap<>(RenderPass.class);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$init(CallbackInfo ci, @Local(ordinal = 0) boolean isFallback) {
        if (VRState.VR_INITIALIZED) {
            Object pipelineManager = IrisHelper.getPipelineManager();
            if (pipelineManager != null) {
                Map<RenderPass, ClrwlPrograms> programs = new EnumMap<>(RenderPass.class);
                for (RenderPass renderPass : RenderPass.values()) {
                    Object pipeline = ((PipelineManagerExtension) pipelineManager).vivecraft$getVRPipeline(renderPass);
                    if (pipeline != null) {
                        try {
                            Method callback = pipeline.getClass().getMethod("colorwheel$setBeginTranslucentsCallback",
                                BeginTranslucentRenderFunction.class);
                            callback.invoke(pipeline,
                                (BeginTranslucentRenderFunction) this.drawManager::renderTranslucent);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            VRSettings.LOGGER.error("failed to set colorwheel translucent callback");
                        }
                        this.vivecraft$pipelines.put(renderPass, (IrisRenderingPipeline) pipeline);

                        ENGINES.put((IrisRenderingPipeline) pipeline, (ClrwlEngine) (Object) this);
                        // make on eprogram cache per pass
                        programs.put(renderPass,
                            ClrwlPrograms.build(FlwPrograms.SOURCES, this.pack, this.dimension, isFallback));
                    }
                }
                ((ClrwlInstancedDrawManagerExtension) this.drawManager).vivecraft$setPrograms(programs);
            }
        }
    }

    @Inject(method = "delete", at = @At("TAIL"))
    private void vivecraft$delete(CallbackInfo ci) {
        for (RenderPass renderPass : this.vivecraft$pipelines.keySet()) {
            IrisRenderingPipeline pipeline = this.vivecraft$pipelines.get(renderPass);
            try {
                Method callback = pipeline.getClass().getMethod("colorwheel$setBeginTranslucentsCallback",
                    BeginTranslucentRenderFunction.class);
                callback.invoke(pipeline, (BeginTranslucentRenderFunction) null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("failed to reset colorwheel translucent callback");
            }

            this.vivecraft$pipelines.remove(renderPass);
            ENGINES.remove(pipeline);
        }
    }
}
