package org.vivecraft.modCompat.immersivePortals.mixin;

import com.mojang.blaze3d.pipeline.TextureTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.render.RenderPass;
import qouteall.imm_ptl.core.render.SecondaryFrameBuffer;

import java.util.HashMap;
import java.util.Map;


@Mixin(SecondaryFrameBuffer.class)
public class SecondaryFrameBufferMixin {

    @Unique
    private final Map<RenderPass, TextureTarget> framebuffers = new HashMap<>();

    @Shadow
    public TextureTarget fb;

    @Inject(method = "prepare(II)V", at = @At("HEAD"), remap = false)
    private void getRenderPassFrameBuffer(int width, int height, CallbackInfo ci){
        fb = framebuffers.get(ClientDataHolder.getInstance().currentPass);
    }
    @Inject(method = "prepare(II)V", at = @At("TAIL"), remap = false)
    private void setRenderPassFrameBuffer(int width, int height, CallbackInfo ci){
        framebuffers.put(ClientDataHolder.getInstance().currentPass, fb);
    }
}
