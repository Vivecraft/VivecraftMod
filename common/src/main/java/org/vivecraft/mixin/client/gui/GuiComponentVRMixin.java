package org.vivecraft.mixin.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiComponent.class)
public class GuiComponentVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V", shift = At.Shift.AFTER), method = "innerBlit(Lorg/joml/Matrix4f;IIIIIFFFF)V")
    private static void addBlend(CallbackInfo ci) {
        RenderSystem.enableBlend();
        // only change the alpha blending
        RenderSystem.blendFuncSeparate(GlStateManager.BLEND.srcRgb, GlStateManager.BLEND.dstRgb, GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value);
    }

    @Inject(at = @At("TAIL"), method = "innerBlit(Lorg/joml/Matrix4f;IIIIIFFFF)V")
    private static void stopBlend(CallbackInfo ci) {
        RenderSystem.disableBlend();
    }
}
