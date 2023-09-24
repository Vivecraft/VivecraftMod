package org.vivecraft.mixin.client_vr.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screens.WinScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WinScreen.class)
public class WinScreenVRMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFunc(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V"), method = "render")
    private void dontDestroyAlpha(SourceFactor sourceFactor, DestFactor destFactor){
        RenderSystem.blendFuncSeparate(sourceFactor, destFactor, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    }
}
