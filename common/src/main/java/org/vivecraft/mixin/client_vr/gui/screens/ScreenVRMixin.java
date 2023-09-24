package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.gui.GuiGraphics;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.client.gui.screens.Screen.class)
public abstract class ScreenVRMixin extends net.minecraft.client.gui.components.events.AbstractContainerEventHandler implements
    net.minecraft.client.gui.components.Renderable
{

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V"), method = "renderBackground")
    public void vrBackground(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n) {
        if (vrRunning && dh.vrSettings != null && !dh.vrSettings.menuBackground) {
            guiGraphics.fillGradient(i, j, k, l, 0, 0);
        } else {
            guiGraphics.fillGradient(i, j, k, l, m, n);
        }
    }

}
