package org.vivecraft.mod_compat_vr.iris.mixin.irisshaders;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.pipeline.ShadowRenderer;
import net.irisshaders.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.PackShadowDirectives;
import net.irisshaders.iris.shaderpack.ProgramSet;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;

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
    private void vivecraft$storeShadowTargets(ProgramSet par1, CallbackInfo ci) {
        if (((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() == null) {
            ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$setShadowRenderTargets(shadowRenderTargets);
        }
    }

    // return main shadowRenderTargets, instead of own
    @Group(name = "one shadowRenderTargets", min = 1, max = 1)
    @Inject(target = @Desc(value = "lambda$new$4", owner = NewWorldRenderingPipeline.class, ret = ShadowRenderTargets.class, args = PackShadowDirectives.class), at = @At("HEAD"), cancellable = true, remap = false, expect = 0)
    private void vivecraft$onlyOneShadowTargetSupplier160(CallbackInfoReturnable<ShadowRenderTargets> cir) {
        if (!RenderPassType.isVanilla() && ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() != null) {
            cir.setReturnValue((ShadowRenderTargets) ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets());
        }
    }

    @Inject(method = "shouldDisableVanillaEntityShadows()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void vivecraft$shouldDisableEntityShadows(CallbackInfoReturnable<Boolean> cir) {
        if (!RenderPassType.isVanilla() && (shadowRenderer != null || ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() != null)) {
            cir.setReturnValue(true);
        }
    }

    // needed because shadowRenderTargets never gets set for sub pipelines
    // this should give the own shadow targets, for the main pipeline and renderpass.LEFT,
    // and for all other piplines the one from renderpass.LEFT

    @Group(name = "reroute shadowRenderTargets", min = 6, max = 6)
    @Redirect(method = "addGbufferOrShadowSamplers", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, expect = 0, slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/samplers/IrisSamplers;hasShadowSamplers(Lnet/irisshaders/iris/gl/sampler/SamplerHolder;)Z")))
    private Object vivecraft$rerouteShadowTarget(Object obj) {
        if (!RenderPassType.isVanilla()) {
            return Objects.requireNonNull(Objects.requireNonNullElse(((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets(), obj));
        } else {
            return Objects.requireNonNull(obj);
        }
    }

    // iris 1.6.0+
    @Group(name = "reroute shadowRenderTargets", min = 6, max = 6)
    @Redirect(target = {
        @Desc(value = "lambda$new$6", owner = NewWorldRenderingPipeline.class, ret = ProgramSamplers.class, args = {java.util.function.Supplier.class, int.class}),
        @Desc(value = "lambda$new$7", owner = NewWorldRenderingPipeline.class, ret = ProgramImages.class, args = {java.util.function.Supplier.class, int.class}),
        @Desc(value = "lambda$new$9", owner = NewWorldRenderingPipeline.class, ret = ProgramSamplers.class, args = int.class),
        @Desc(value = "lambda$new$11", owner = NewWorldRenderingPipeline.class, ret = ProgramImages.class, args = int.class)
    }, at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, expect = 0)
    private Object vivecraft$rerouteShadowTarget160(Object obj) {
        if (!RenderPassType.isVanilla()) {
            return Objects.requireNonNull(Objects.requireNonNullElse(((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets(), obj));
        } else {
            return Objects.requireNonNull(obj);
        }
    }
}
