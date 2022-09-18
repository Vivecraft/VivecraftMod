package org.vivecraft.modCompatMixin.irisMixin;

import net.coderbot.iris.pipeline.DeferredWorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(DeferredWorldRenderingPipeline.class)
public class IrisDeferredWorldRenderingPipelineVRMixin {

//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"), method = "<init>")
//    public RenderTarget rendertarget(Minecraft instance) {
//        return DataHolder.getInstance().vrRenderer.framebufferVrRender;
//    }
}
