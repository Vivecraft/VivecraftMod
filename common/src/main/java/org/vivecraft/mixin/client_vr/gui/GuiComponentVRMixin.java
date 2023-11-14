package org.vivecraft.mixin.client_vr.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;

@Mixin(GuiGraphics.class)
public class GuiComponentVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V", remap = false, shift = At.Shift.AFTER), method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V")
    private void vivecraft$addBlend(CallbackInfo ci) {
        if (VRState.vrRunning) {
            RenderSystem.enableBlend();
            // only change the alpha blending
            RenderSystem.blendFuncSeparate(GlStateManager.BLEND.srcRgb, GlStateManager.BLEND.dstRgb, GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value);
        }
    }

    @Inject(at = @At("TAIL"), method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V")
    private void vivecraft$stopBlend(CallbackInfo ci) {
        if (VRState.vrRunning) {
            RenderSystem.disableBlend();
        }
    }
}
