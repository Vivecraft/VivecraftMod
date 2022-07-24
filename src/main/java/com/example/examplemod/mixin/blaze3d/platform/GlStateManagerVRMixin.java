package com.example.examplemod.mixin.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.blaze3d.platform.GlStateManager.BLEND;
import static com.mojang.blaze3d.platform.GlStateManager.glBlendFuncSeparate;

@Mixin(GlStateManager.class)
public class GlStateManagerVRMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static void _blendFuncSeparate(int i, int j, int k, int l) {
        RenderSystem.assertOnRenderThread();
        if (j == GlStateManager.SourceFactor.SRC_ALPHA.value && j == GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value && k == GlStateManager.SourceFactor.ONE.value && l == GlStateManager.DestFactor.ZERO.value) {
            l = GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value;
        }
        if (i != BLEND.srcRgb || j != BLEND.dstRgb || k != BLEND.srcAlpha || l != BLEND.dstAlpha) {
            BLEND.srcRgb = i;
            BLEND.dstRgb = j;
            BLEND.srcAlpha = k;
            BLEND.dstAlpha = l;
            glBlendFuncSeparate(i, j, k, l);
        }
    }
}
