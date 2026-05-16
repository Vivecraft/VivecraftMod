package org.vivecraft.mod_compat_vr.colorwheel.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.djefrey.colorwheel.compile.ClrwlPrograms;
import dev.djefrey.colorwheel.engine.ClrwlProgramFramebuffers;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.mod_compat_vr.colorwheel.extensions.ClrwlInstancedDrawManagerExtension;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;

import java.util.EnumMap;
import java.util.Map;

@Mixin(targets = "dev.djefrey.colorwheel.instancing.ClrwlInstancedDrawManager")
public class ClrwlInstancedDrawManagerVRMixin implements ClrwlInstancedDrawManagerExtension {

    @Unique
    private final Map<RenderPass, IrisRenderingPipeline> vivecraft$pipelines = new EnumMap<>(RenderPass.class);

    @Unique
    private final Map<RenderPass, ClrwlProgramFramebuffers> vivecraft$framebuffers = new EnumMap<>(RenderPass.class);

    @Unique
    private Map<RenderPass, ClrwlPrograms> vivecraft$programs;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$init(CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            Object pipelineManager = IrisHelper.getPipelineManager();
            if (pipelineManager != null) {
                for (RenderPass renderPass : RenderPass.values()) {
                    Object pipeline = ((PipelineManagerExtension) pipelineManager).vivecraft$getVRPipeline(renderPass);
                    if (pipeline != null) {
                        this.vivecraft$pipelines.put(renderPass, (IrisRenderingPipeline) pipeline);
                        this.vivecraft$framebuffers.put(renderPass, new ClrwlProgramFramebuffers());
                    }
                }
            }
        }
    }

    @ModifyExpressionValue(method = {"renderTranslucent", "submitDraws", "renderCrumbling"}, at = @At(value = "FIELD", target = "Ldev/djefrey/colorwheel/instancing/ClrwlInstancedDrawManager;irisPipeline:Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;"))
    private IrisRenderingPipeline vivecraft$vrPipeline(IrisRenderingPipeline original) {
        return VRState.VR_RUNNING &&
            this.vivecraft$pipelines.containsKey(ClientDataHolderVR.getInstance().currentPass) ?
            this.vivecraft$pipelines.get(ClientDataHolderVR.getInstance().currentPass) : original;
    }

    @ModifyExpressionValue(method = {"renderTranslucent", "submitDraws", "submitOitDraws", "renderCrumbling"}, at = @At(value = "FIELD", target = "Ldev/djefrey/colorwheel/instancing/ClrwlInstancedDrawManager;framebuffers:Ldev/djefrey/colorwheel/engine/ClrwlProgramFramebuffers;"))
    private ClrwlProgramFramebuffers vivecraft$vrFramebuffer(ClrwlProgramFramebuffers original) {
        return VRState.VR_RUNNING &&
            this.vivecraft$framebuffers.containsKey(ClientDataHolderVR.getInstance().currentPass) ?
            this.vivecraft$framebuffers.get(ClientDataHolderVR.getInstance().currentPass) : original;
    }

    @ModifyExpressionValue(method = {"renderTranslucent", "submitDraws", "submitOitDraws", "renderCrumbling"}, at = @At(value = "FIELD", target = "Ldev/djefrey/colorwheel/instancing/ClrwlInstancedDrawManager;programs:Ldev/djefrey/colorwheel/compile/ClrwlPrograms;"))
    private ClrwlPrograms vivecraft$vrPrograms(ClrwlPrograms original) {
        return VRState.VR_RUNNING &&
            this.vivecraft$programs.containsKey(ClientDataHolderVR.getInstance().currentPass) ?
            this.vivecraft$programs.get(ClientDataHolderVR.getInstance().currentPass) : original;
    }

    @Inject(method = "delete", at = @At("HEAD"))
    private void vivecraft$deleteFramebuffers(CallbackInfo ci) {
        for (RenderPass pass : this.vivecraft$pipelines.keySet()) {
            this.vivecraft$framebuffers.get(pass).delete(this.vivecraft$pipelines.get(pass));
            this.vivecraft$framebuffers.remove(pass);
            this.vivecraft$pipelines.remove(pass);
            this.vivecraft$programs.get(pass).delete();
            this.vivecraft$programs.remove(pass);
        }
    }

    @Override
    @Unique
    public void vivecraft$setPrograms(Map<RenderPass, ClrwlPrograms> programs) {
        this.vivecraft$programs = programs;
    }
}
