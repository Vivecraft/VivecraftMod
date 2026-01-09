package org.vivecraft.mod_compat_vr.iris.mixin.coderbot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Pseudo
@Mixin(NewWorldRenderingPipeline.class)
public class IrisNewWorldRenderingPipelineVRMixin {
    @Shadow(remap = false)
    private ShadowRenderTargets shadowRenderTargets;

    // make this mutable, or WrapOperation doesn't work
    @Final
    @Mutable
    @Shadow(remap = false)
    private Supplier<ShadowRenderTargets> shadowTargetsSupplier;

    // store shadowTargets of the first pipeline
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void vivecraft$storeShadowTargets(ProgramSet programSet, CallbackInfo ci) {
        // because iris pre 1.6 doesn't have those fields use reflection here
        try {
            Class<?> renderingPipeline = Class.forName(
                "net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline");
            Field customImages = renderingPipeline.getDeclaredField("customImages");
            Field shaderStorageBufferHolde = renderingPipeline.getDeclaredField("shaderStorageBufferHolder");
            ShadersHelper.SLOW_MODE =
                !((Set<?>) customImages.get(this)).isEmpty() || shaderStorageBufferHolde.get(this) != null;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {}

        if (!ShadersHelper.isSlowMode() &&
            ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() == null)
        {
            ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$setShadowRenderTargets(
                this.shadowRenderTargets);
        }
    }

    // return main shadowRenderTargets, instead of own
    @WrapOperation(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/coderbot/iris/pipeline/newshader/NewWorldRenderingPipeline;shadowTargetsSupplier:Ljava/util/function/Supplier;", ordinal = 0), remap = false)
    private void vivecraft$onlyOneShadowRenderTarget(
        NewWorldRenderingPipeline instance, Supplier<ShadowRenderTargets> value, Operation<Void> original)
    {
        Supplier<ShadowRenderTargets> wrappedSupplier = () -> {
            if (!ShadersHelper.isSlowMode() && !RenderPassType.isVanilla() && this.shadowRenderTargets == null &&
                ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() != null)
            {
                return (ShadowRenderTargets) ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets();
            } else {
                return value.get();
            }
        };
        original.call(instance, wrappedSupplier);
    }

    @ModifyReturnValue(method = "shouldDisableVanillaEntityShadows()Z", at = @At("RETURN"), remap = false)
    private boolean vivecraft$shouldDisableEntityShadows(boolean noEntityShadows) {
        return noEntityShadows || (!ShadersHelper.isSlowMode() && !RenderPassType.isVanilla() &&
            ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() != null
        );
    }

    /**
     * needed because shadowRenderTargets never gets set for sub pipelines
     * this should give the own shadow targets, for the main pipeline and the first RenderPass,
     * and for all other pipelines the one from the first RenderPass
     */
    @Group(name = "reroute shadowRenderTargets", min = 6)
    @ModifyArg(method = {
        "addGbufferOrShadowSamplers*",
        "lambda$new$2*",// iris 1.4
        "lambda$new$3*",// iris 1.3.1, 1.4
        "lambda$new$4*",// iris 1.3.1
        "lambda$new$5*",// iris 1.4, 1.5
        "lambda$new$6*",// iris 1.3.1, 1.5, 1.6
        "lambda$new$7*",// iris 1.4, 1.6
        "lambda$new$8*",// iris 1.3.1, 1.5
        "lambda$new$9*",// iris 1.6
        "lambda$new$10*",// iris 1.5
        "lambda$new$11*"// iris 1.6
    }, at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false, expect = 0)
    private Object vivecraft$rerouteShadowTarget(Object obj) {
        // make sure we only change ShadowRenderTargets, since this might also inject into other lambdas
        if (!ShadersHelper.isSlowMode() && !RenderPassType.isVanilla() && obj instanceof ShadowRenderTargets ||
            obj == null)
        {
            return Objects.requireNonNullElse(
                ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets(), obj);
        } else {
            return obj;
        }
    }
}
