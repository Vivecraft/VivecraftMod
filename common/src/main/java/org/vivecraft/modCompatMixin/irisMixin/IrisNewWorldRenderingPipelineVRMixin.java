package org.vivecraft.modCompatMixin.irisMixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.shaderpack.PackShadowDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.extensions.iris.PipelineManagerExtension;

import java.util.Objects;

@Pseudo
@Mixin(NewWorldRenderingPipeline.class)
public class IrisNewWorldRenderingPipelineVRMixin {
    @Shadow(remap = false)
    private ShadowRenderTargets shadowRenderTargets;

    @Final
    @Shadow(remap = false)
    private ShadowRenderer shadowRenderer;

    // store shadowTargets of the first pipeline
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void storeShadowTargets(ProgramSet par1, CallbackInfo ci) {
        if (((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets() == null) {
            ((PipelineManagerExtension) Iris.getPipelineManager()).setShadowRenderTargets(shadowRenderTargets);
        }
    }

    // return main shadowRenderTargets, instead of own
    @Group(name = "one shadowRenderTargets", min = 1, max = 1)
    @Inject(target = @Desc(value = "lambda$new$1", owner = NewWorldRenderingPipeline.class, ret = ShadowRenderTargets.class, args = PackShadowDirectives.class), at = @At("HEAD"), cancellable = true, remap = false, expect = 0)
    private void onlyOneShadowTargetSupplier131(CallbackInfoReturnable<ShadowRenderTargets> cir) {
        if (((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets() != null) {
            cir.setReturnValue(((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets());
        }
    }
    @Group(name = "one shadowRenderTargets", min = 1, max = 1)
    @Inject(target = @Desc(value = "lambda$new$0", owner = NewWorldRenderingPipeline.class, ret = ShadowRenderTargets.class, args = PackShadowDirectives.class), at = @At("HEAD"), cancellable = true, remap = false, expect = 0)
    private void onlyOneShadowTargetSupplier140(CallbackInfoReturnable<ShadowRenderTargets> cir) {
        if (((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets() != null) {
            cir.setReturnValue(((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets());
        }
    }
    // why do you need to be special?
    @Group(name = "one shadowRenderTargets", min = 1, max = 1)
    @Inject(target = @Desc(value = "lambda$new$1", owner = NewWorldRenderingPipeline.class, ret = ShadowRenderTargets.class, args = {}), at = @At("HEAD"), cancellable = true, remap = false, expect = 0)
    private void onlyOneShadowTargetSupplierOculus(CallbackInfoReturnable<ShadowRenderTargets> cir) {
        if (((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets() != null) {
            cir.setReturnValue(((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets());
        }
    }

    @Inject(method = "shouldDisableVanillaEntityShadows()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void shouldDisableEntityShadows(CallbackInfoReturnable<Boolean> cir) {
        if (shadowRenderer != null || ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets() != null) {
            cir.setReturnValue(true);
        }
    }

    // needed because shadowRenderTargets never gets set for sub pipelines
    // this should give the own shadow targets, for the main pipeline and renderpass.LEFT,
    // and for all other piplines the one from renderpass.LEFT

    @Group(name = "reroute shadowRenderTargets", min = 6, max = 6)
    @Redirect(method = "addGbufferOrShadowSamplers", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, expect = 0, slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/coderbot/iris/samplers/IrisSamplers;hasShadowSamplers(Lnet/coderbot/iris/gl/sampler/SamplerHolder;)Z")))
    private Object rerouteShadowTarget(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return Objects.requireNonNull(targets != null ? targets : obj);
    }

    // iris 1.3.1 and before
    @Group(name = "reroute shadowRenderTargets",  min = 6, max = 6)
    @Redirect(target = {
            @Desc(value = "lambda$new$3", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramSamplers.class, args = {java.util.function.Supplier.class, int.class}),
            @Desc(value = "lambda$new$4", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramImages.class, args = {java.util.function.Supplier.class, int.class}),
            @Desc(value = "lambda$new$6", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramSamplers.class, args = int.class),
            @Desc(value = "lambda$new$8", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramImages.class, args = int.class)
    }, at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, expect = 0)
    private Object rerouteShadowTarget131(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return Objects.requireNonNull(targets != null ? targets : obj);
    }

    // iris 1.4.0+
    @Group(name = "reroute shadowRenderTargets",  min = 6, max = 6)
    @Redirect(target = {
            @Desc(value = "lambda$new$2", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramSamplers.class, args = {java.util.function.Supplier.class, int.class}),
            @Desc(value = "lambda$new$3", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramImages.class, args = {java.util.function.Supplier.class, int.class}),
            @Desc(value = "lambda$new$5", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramSamplers.class, args = int.class),
            @Desc(value = "lambda$new$7", owner = NewWorldRenderingPipeline.class, ret = net.coderbot.iris.gl.program.ProgramImages.class, args = int.class)
    }, at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, expect = 0)
    private Object rerouteShadowTarget140(Object obj) {
        ShadowRenderTargets targets = ((PipelineManagerExtension) Iris.getPipelineManager()).getShadowRenderTargets();
        return Objects.requireNonNull(targets != null ? targets : obj);
    }

}
