package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vivecraft.client.extensions.RenderTargetExtension;

import static net.minecraft.client.Minecraft.ON_OSX;

@Mixin(net.minecraft.client.renderer.PostChain.class)
public class PostChainVRMixin {

    @Shadow
    @Final
    private RenderTarget screenTarget;

    @ModifyVariable(method = "addTempTarget", at = @At("STORE"), ordinal = 0)
    private RenderTarget vivecraft$vrTarget(RenderTarget old) {
        if (((RenderTargetExtension) this.screenTarget).vivecraft$getUseStencil()) {
            ((RenderTargetExtension) old).vivecraft$setUseStencil(true);
            old.resize(old.width, old.height, ON_OSX);
        }
        return old;
    }
}
