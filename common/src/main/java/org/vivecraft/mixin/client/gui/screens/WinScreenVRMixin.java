package org.vivecraft.mixin.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.WinScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WinScreen.class)
public class WinScreenVRMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V"), method = "render")
    private void dontDestroyAlpha(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor){
        RenderSystem.blendFuncSeparate(sourceFactor, destFactor, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", shift = At.Shift.BEFORE), method = "respawn", cancellable = true)
    private void dontClearScreen(CallbackInfo ci){
        ci.cancel();
    }
}
