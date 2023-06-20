package org.vivecraft.titleworlds.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    Minecraft minecraft;

    @Unique
    private boolean hideGui;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    void preRenderGui(float f, long l, boolean bl, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            this.hideGui = this.minecraft.options.hideGui;
            this.minecraft.options.hideGui = true;
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/Gui;render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    void postRenderGui(float f, long l, boolean bl, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            this.minecraft.options.hideGui = this.hideGui;
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    void preRenderLevel(float f, long l, boolean bl, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            this.hideGui = this.minecraft.options.hideGui;
            this.minecraft.options.hideGui = true;
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    void postRenderLevel(float f, long l, boolean bl, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            this.minecraft.options.hideGui = this.hideGui;
        }
    }
}
