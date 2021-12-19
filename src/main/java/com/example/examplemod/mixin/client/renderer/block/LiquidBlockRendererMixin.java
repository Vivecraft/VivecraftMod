package com.example.examplemod.mixin.client.renderer.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
	
	@Unique
	private static boolean skipStupidGoddamnChunkBoundaryClipping;
	
	//TODO a bit hacky, when does it happen?
	@ModifyConstant(method = "tesselate", constant = @Constant(intValue = 15))
	public int chunckClipping(int i) {
		return skipStupidGoddamnChunkBoundaryClipping? Integer.MAX_VALUE : 15;
	}
	
//	//TODO not found?
//	@Redirect(at = @At(value = "INVOKE", target = ""))
//	public boolean skip() {
//		return true;
//	}
}
