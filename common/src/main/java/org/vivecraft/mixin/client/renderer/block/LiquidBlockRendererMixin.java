package org.vivecraft.mixin.client.renderer.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static org.vivecraft.client_vr.VRState.dh;

@Mixin(net.minecraft.client.renderer.block.LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {

    // needed for menuworlds water rendering
    @ModifyConstant(method = "tesselate", constant = @Constant(intValue = 15))
    public int vivecraft$chunkClipping(int i) {
        // -1 is 0xFFFF FFFF
        // so no change
        return dh.skipStupidGoddamnChunkBoundaryClipping ? -1 : 15;
    }
}
