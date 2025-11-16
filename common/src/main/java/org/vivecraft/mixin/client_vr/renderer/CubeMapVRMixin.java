package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.VRShaders;

@Mixin(CubeMap.class)
public class CubeMapVRMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderPipelines;PANORAMA:Lcom/mojang/blaze3d/pipeline/RenderPipeline;"))
    private RenderPipeline vivecraft$solidPipeline(RenderPipeline instance) {
        return VRState.VR_RUNNING ? VRShaders.SOLID_PANORAMA : instance;
    }
}
