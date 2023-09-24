package org.vivecraft.mixin.client_vr.blaze3d.systems;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import static com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(com.mojang.blaze3d.systems.RenderSystem.class)
public class RenderSystemVRMixin {

    // do remap because of forge
    @Inject(at = @At("HEAD"), method = "defaultBlendFunc", cancellable = true, remap = false)
    private static void defaultBlendFunc(CallbackInfo ci) {
        blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
        ci.cancel();
    }
}
