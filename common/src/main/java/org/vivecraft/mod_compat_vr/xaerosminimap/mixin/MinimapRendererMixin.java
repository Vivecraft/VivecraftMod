package org.vivecraft.mod_compat_vr.xaerosminimap.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "xaero.common.minimap.render.MinimapRenderer")
public class MinimapRendererMixin {
    @WrapOperation(method = "renderMinimap", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V", remap = true), remap = false)
    private void vivecraft$fixConstantReference(
        GlStateManager.SourceFactor srcRgb, GlStateManager.DestFactor dstRgb, GlStateManager.SourceFactor srcAlpha,
        GlStateManager.DestFactor dstAlpha, Operation<Void> original)
    {
        // fix blend function, check for the right one to fix
        // check for ONE and SRC_ALPHA, in case xaero sets this to the right one but forgets to fix the alpha
        if ((srcRgb == GlStateManager.SourceFactor.ONE || srcRgb == GlStateManager.SourceFactor.SRC_ALPHA) &&
            dstRgb == GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA &&
            // someone swapped the ZERO and ONE
            (srcAlpha == GlStateManager.SourceFactor.ZERO || srcAlpha == GlStateManager.SourceFactor.ONE) &&
            (dstAlpha == GlStateManager.DestFactor.ONE || dstAlpha == GlStateManager.DestFactor.ZERO))
        {
            original.call(srcRgb,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        } else {
            original.call(srcRgb, dstRgb, srcAlpha, dstAlpha);
        }
    }
}
