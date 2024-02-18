package org.vivecraft.mixin.client_vr.blaze3d.systems;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.MCVR;

import static com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate;

@Mixin(RenderSystem.class)
public class RenderSystemVRMixin {

    @Inject(at = @At("HEAD"), method = "defaultBlendFunc", cancellable = true, remap = false)
    private static void vivecraft$defaultBlendFunc(CallbackInfo ci) {
        blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "limitDisplayFPS", cancellable = true, remap = false)
    private static void vivecraft$noFPSlimit(CallbackInfo ci) {
        if (VRState.vrRunning && !MCVR.get().capFPS()) {
            ci.cancel();
        }
    }
}
