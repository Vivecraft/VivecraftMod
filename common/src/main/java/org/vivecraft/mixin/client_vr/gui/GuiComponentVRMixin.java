package org.vivecraft.mixin.client_vr.gui;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import static org.vivecraft.client_vr.VRState.vrRunning;

import static com.mojang.blaze3d.platform.GlStateManager.BLEND;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.GuiGraphics.class)
public class GuiComponentVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V", shift = Shift.AFTER), method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V")
    private void addBlend(CallbackInfo ci) {
        if (vrRunning) {
            RenderSystem.enableBlend();
            // only change the alpha blending
            RenderSystem.blendFuncSeparate(BLEND.srcRgb, BLEND.dstRgb, SourceFactor.ONE.value, DestFactor.ONE_MINUS_SRC_ALPHA.value);
        }
    }

    @Inject(at = @At("TAIL"), method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V")
    private void stopBlend(CallbackInfo ci) {
        if (vrRunning) {
            RenderSystem.disableBlend();
        }
    }
}
