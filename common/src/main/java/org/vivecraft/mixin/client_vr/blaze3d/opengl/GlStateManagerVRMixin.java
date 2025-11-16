package org.vivecraft.mixin.client_vr.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GlStateManager.class, priority = 990)
public class GlStateManagerVRMixin {

    // Change the limit of textures to 32, needed because we add additional samplers to fabulous
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;range(II)Ljava/util/stream/IntStream;"), index = 1)
    private static int vivecraft$moreTextures(int endExclusive) {
        return 32;
    }
}
