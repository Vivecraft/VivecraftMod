package org.vivecraft.mixin.client_vr.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GlStateManager.class, priority = 990)
public class GlStateManagerVRMixin {

    // Change the limit of textures to 32, needed because we add additional samplers to fabulous
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;range(II)Ljava/util/stream/IntStream;"), index = 1)
    private static int vivecraft$moreTextures(int endExclusive) {
        return 32;
    }

    // dstAlpha first, because that is the variable we are changing
    @ModifyVariable(method = "_blendFuncSeparate", at = @At("HEAD"), remap = false, index = 3, argsOnly = true)
    private static int vivecraft$guiAlphaBlending(int dstAlpha, int srcRgb, int dstRgb, int srcAlpha) {
        if (srcRgb == GlStateManager.SourceFactor.SRC_ALPHA.value &&
            dstRgb == GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value &&
            srcAlpha == GlStateManager.SourceFactor.ONE.value &&
            dstAlpha == GlStateManager.DestFactor.ZERO.value)
        {
            return GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value;
        } else {
            return dstAlpha;
        }
    }
}
