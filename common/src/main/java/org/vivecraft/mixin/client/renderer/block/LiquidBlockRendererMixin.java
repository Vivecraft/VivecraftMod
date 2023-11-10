package org.vivecraft.mixin.client.renderer.block;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
    // needed for menuworlds water rendering
    @ModifyConstant(method = "tesselate", constant = @Constant(intValue = 15))
    public int vivecraft$chunkClipping(int i) {
        // -1 is 0xFFFF FFFF
        // so no change
        return ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread() ? -1 : 15;
    }
}
