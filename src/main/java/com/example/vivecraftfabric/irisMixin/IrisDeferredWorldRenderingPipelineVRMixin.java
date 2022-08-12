package com.example.vivecraftfabric.irisMixin;

import com.example.vivecraftfabric.DataHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.pipeline.DeferredWorldRenderingPipeline;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(DeferredWorldRenderingPipeline.class)
public class IrisDeferredWorldRenderingPipelineVRMixin {

//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"), method = "<init>")
//    public RenderTarget rendertarget(Minecraft instance) {
//        return DataHolder.getInstance().vrRenderer.framebufferVrRender;
//    }
}
