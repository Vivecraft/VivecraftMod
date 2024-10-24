package org.vivecraft.mixin.client.renderer.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
    // needed for menuworlds water rendering, to get world space positions, and not per chunk
    @ModifyExpressionValue(method = "tesselate", at = @At(value = "CONSTANT", args = "intValue=15"))
    private int vivecraft$noChunkWrappingInMenuWorld(int i) {
        // -1 is 0xFFFF FFFF
        // so no change
        return ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread() ? -1 : i;
    }
}
