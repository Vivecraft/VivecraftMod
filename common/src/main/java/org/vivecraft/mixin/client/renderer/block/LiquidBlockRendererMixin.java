package org.vivecraft.mixin.client.renderer.block;

import static org.vivecraft.client_vr.VRState.dh;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(net.minecraft.client.renderer.block.LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
	
	//TODO a bit hacky, when does it happen?
	@ModifyConstant(method = "tesselate", constant = @Constant(intValue = 15))
	public int chunckClipping(int i) {
		// -1 is 0xFFFF FFFF
		// so no change
		return dh.skipStupidGoddamnChunkBoundaryClipping ? -1 : 15;
	}

//	//TODO not found?
//	@Redirect(at = @At(value = "INVOKE", target = ""))
//	public boolean skip() {
//		return true;
//	}
}
