package com.example.examplemod.mixin.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.GuiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiComponent.class)
public class GuiComponentVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V"), method = "innerBlit(Lcom/mojang/math/Matrix4f;IIIIIFFFF)V")
    private static void addBlend(Matrix4f matrix4f, int i, int j, int k, int l, int m, float f, float g, float h, float n, CallbackInfo ci) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferUploader;end(Lcom/mojang/blaze3d/vertex/BufferBuilder;)V", shift = At.Shift.AFTER), method = "innerBlit(Lcom/mojang/math/Matrix4f;IIIIIFFFF)V")
    private static void stopBlend(Matrix4f matrix4f, int i, int j, int k, int l, int m, float f, float g, float h, float n, CallbackInfo ci) {
        RenderSystem.disableBlend();
    }
}
