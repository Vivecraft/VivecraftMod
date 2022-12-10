package org.vivecraft.mixin.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.extensions.RenderTargetExtension;

@Mixin(PostChain.class)
public class PostChainVRMixin {

    @Shadow
    @Final
    private RenderTarget screenTarget;

    @ModifyVariable(method = "addTempTarget", at = @At(value = "STORE"), ordinal = 0)
    private RenderTarget vrTarget(RenderTarget old) {
        if (((RenderTargetExtension)screenTarget).getUseStencil()) {
            ((RenderTargetExtension) old).setUseStencil(true);
            old.resize(old.width, old.height, Minecraft.ON_OSX);
        }
        return old;
    }
}
