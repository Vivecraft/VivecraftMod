package org.vivecraft.mixin.client_vr.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import static com.mojang.blaze3d.platform.GlStateManager.BLEND;
import static com.mojang.blaze3d.platform.GlStateManager.glBlendFuncSeparate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(com.mojang.blaze3d.platform.GlStateManager.class)
public class GlStateManagerVRMixin {

    //Change the limit of textures to 32
    @ModifyArg(at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;range(II)Ljava/util/stream/IntStream;"), index = 1, method = "<clinit>")
    private static int size(int i) {
        return 32;
    }

    //Change the limit of textures to 32
    // This doesn't exist anymore
    /*@ModifyConstant(constant = @Constant(intValue = 12),method = "_getTextureId")
    private static int properId(int i) {
        return RenderSystemAccessor.getShaderTextures().length;
    }*/

    /**
     * @author
     * @reason
     */
    // do remap because of forge
    @Overwrite(remap = false)
    public static void _blendFuncSeparate(int i, int j, int k, int l) {
        RenderSystem.assertOnRenderThread();
        if (i == SourceFactor.SRC_ALPHA.value && j == DestFactor.ONE_MINUS_SRC_ALPHA.value && k == SourceFactor.ONE.value && l == DestFactor.ZERO.value) {
            l = DestFactor.ONE_MINUS_SRC_ALPHA.value;
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
