package com.example.vivecraftfabric.irisMixin;

import com.example.vivecraftfabric.DataHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.RenderPass;

@Pseudo
@Mixin(SystemTimeUniforms.FrameCounter.class)
public class IrisBeginFrameHack {

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelShadows(CallbackInfo ci) {
        if (DataHolder.getInstance().currentPass != RenderPass.LEFT) {
            ci.cancel();
        }
    }
}
