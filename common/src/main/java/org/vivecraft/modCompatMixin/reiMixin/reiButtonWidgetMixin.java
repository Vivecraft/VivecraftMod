package org.vivecraft.modCompatMixin.reiMixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {"me.shedaniel.rei.impl.client.gui.widget.basewidgets.ButtonWidget"})
public class reiButtonWidgetMixin {

    @Inject(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIIZLme/shedaniel/math/Color;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(II)V", shift = At.Shift.AFTER))
    private void changeBlendFunction(CallbackInfo ci) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA, GlStateManager.DestFactor.ONE);
    }
}
