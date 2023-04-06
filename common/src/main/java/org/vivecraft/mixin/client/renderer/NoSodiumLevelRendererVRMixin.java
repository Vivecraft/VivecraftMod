package org.vivecraft.mixin.client.renderer;

import org.vivecraft.mixin.client.blaze3d.systems.RenderSystemAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class NoSodiumLevelRendererVRMixin {

    @Shadow
    private boolean needsFullRenderChunkUpdate;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;needsFullRenderChunkUpdate:Z", ordinal = 1, shift = At.Shift.AFTER), method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V")
    public void alwaysUpdateCull(Camera camera, Frustum frustum, boolean bl, boolean bl2, CallbackInfo info) {
        this.needsFullRenderChunkUpdate = true;
    }

    @ModifyConstant(method = "renderChunkLayer", constant = @Constant(intValue = 12))
    public int moreTextures(int constant) {
        return RenderSystemAccessor.getShaderTextures().length;
    }
}
