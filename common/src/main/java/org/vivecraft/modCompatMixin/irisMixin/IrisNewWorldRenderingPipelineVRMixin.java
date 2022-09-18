package org.vivecraft.modCompatMixin.irisMixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.extensions.iris.PipelineManagerExtension;

@Pseudo
@Mixin(NewWorldRenderingPipeline.class)
public class IrisNewWorldRenderingPipelineVRMixin {
    @Shadow(remap = false)
    private ShadowRenderTargets shadowRenderTargets;

    // store shadowTargets of the first pipeline
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void storeShadowTargets(ProgramSet par1, CallbackInfo ci) {
        if (((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets() == null) {
            ((PipelineManagerExtension) Iris.getPipelineManager()).setShadowRenderTargets(shadowRenderTargets);
        }
    }

    // return main shadowRenderTargets, instead of own
    @Inject(method = "lambda$new$1", at = @At("HEAD"), cancellable = true, remap = false)
    private void onlyOneShadowTargetSupplier(CallbackInfoReturnable<ShadowRenderTargets> cir) {
        if (((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets() != null) {
            cir.setReturnValue(((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets());
        }
    }

    // needed because shadowRenderTargets never gets set for sub pipelines
    // this should give the own shadow targets, for the main pipeline and renderpass.LEFT,
    // and for all other piplines the one from renderpass.LEFT
    @Redirect(method = "addGbufferOrShadowSamplers", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object rerouteShadowTarget(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return targets != null ? targets : obj;
    }
    @Redirect(method = "lambda$new$3", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object rerouteShadowTargetTerrainSamplers(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return targets != null ? targets : obj;
    }
    @Redirect(method = "lambda$new$4", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object rerouteShadowTargetTerrainImages(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return targets != null ? targets : obj;
    }
    @Redirect(method = "lambda$new$6", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object rerouteShadowTargetShadowTerrainSamplers(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return targets != null ? targets : obj;
    }
    @Redirect(method = "lambda$new$8", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object rerouteShadowTargetShadowTerrainImages(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return targets != null ? targets : obj;
    }

}
