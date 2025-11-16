package org.vivecraft.mixin.client_vr.blaze3d.opengl;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.VRShaders;

@Mixin(GlCommandEncoder.class)
public class GLCommandEncoderVRMixin {
    @ModifyExpressionValue(method = "applyPipelineState", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline;getDepthTestFunction()Lcom/mojang/blaze3d/platform/DepthTestFunction;", ordinal = 0, remap = false), remap = true)
    private DepthTestFunction vivecraft$depthAlways(
        DepthTestFunction depthTest, @Local(argsOnly = true) RenderPipeline renderPipeline)
    {
        // This is just here because there is no specific ALWAYS_DEPTH_TEST
        return (VRState.VR_RUNNING && VRShaders.DEPTH_ALWAYS_PIPELINES.contains(renderPipeline)) ? null : depthTest;
    }
}
