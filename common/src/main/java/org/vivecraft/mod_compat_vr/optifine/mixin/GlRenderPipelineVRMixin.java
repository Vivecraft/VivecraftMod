package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;
import org.vivecraft.mod_compat_vr.optifine.extensions.GlRenderPipelineExtension;

@ClassDependentMixin("net.optifine.Config")
@Mixin(targets = "com.mojang.renderpearl.backend.opengl.GlRenderPipeline")
public class GlRenderPipelineVRMixin implements GlRenderPipelineExtension {

    @Unique
    private String vivecraft$pipelineName;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$rememberName(
        CallbackInfo ci, @Local(argsOnly = true) BackendRenderPipeline.CreateInfo createInfo)
    {
        this.vivecraft$pipelineName = createInfo.name();
    }

    @Override
    public String vivecraft$getName() {
        return this.vivecraft$pipelineName;
    }
}
