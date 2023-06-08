package org.vivecraft.mixin.client_vr.blaze3d.systems;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;

import static com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate;

@Mixin(RenderSystem.class)
public class RenderSystemVRMixin {

    // do remap because of forge
    @Inject(at = @At("HEAD"), method = "defaultBlendFunc", cancellable = true, remap = VivecraftVRMod.compiledWithForge)
    private static void defaultBlendFunc(CallbackInfo ci) {
        blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        ci.cancel();
    }
}
