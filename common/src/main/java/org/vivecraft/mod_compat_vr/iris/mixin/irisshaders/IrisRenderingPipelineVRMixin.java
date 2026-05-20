package org.vivecraft.mod_compat_vr.iris.mixin.irisshaders;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.util.Set;
import java.util.function.Supplier;

@Pseudo
@Mixin(IrisRenderingPipeline.class)
public class IrisRenderingPipelineVRMixin {

    @Shadow
    private ShadowRenderTargets shadowRenderTargets;

    // make this mutable, or WrapOperation doesn't work
    @Final
    @Mutable
    @Shadow
    private Supplier<ShadowRenderTargets> shadowTargetsSupplier;

    @Final
    @Shadow
    private Set<GlImage> customImages;

    @Shadow
    private ShaderStorageBufferHolder shaderStorageBufferHolder;

    // store shadowTargets of the first pipeline
    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$storeShadowTargets(ProgramSet programSet, CallbackInfo ci) {
        ShadersHelper.SLOW_MODE = !this.customImages.isEmpty() || this.shaderStorageBufferHolder != null;

        if (!ShadersHelper.isSlowMode() &&
            ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() == null)
        {
            ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$setShadowRenderTargets(
                this.shadowRenderTargets);
        }
    }

    @WrapOperation(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;shadowTargetsSupplier:Ljava/util/function/Supplier;", ordinal = 0))
    private void vivecraft$onlyOneShadowRenderTarget(
        IrisRenderingPipeline instance, Supplier<ShadowRenderTargets> value, Operation<Void> original)
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

    @ModifyReturnValue(method = "shouldDisableVanillaEntityShadows()Z", at = @At("RETURN"))
    private boolean vivecraft$shouldDisableEntityShadows(boolean noEntityShadows) {
        return noEntityShadows || (!ShadersHelper.isSlowMode() && !RenderPassType.isVanilla() &&
            ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getShadowRenderTargets() != null
        );
    }
}
