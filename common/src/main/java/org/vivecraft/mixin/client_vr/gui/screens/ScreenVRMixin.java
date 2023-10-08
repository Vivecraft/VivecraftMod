package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

@Mixin(net.minecraft.client.gui.screens.Screen.class)
public abstract class ScreenVRMixin extends net.minecraft.client.gui.components.events.AbstractContainerEventHandler implements
    net.minecraft.client.gui.components.Renderable {

    @Inject(at = @At("HEAD"), method = "renderTransparentBackground", cancellable = true)
    public void vivecraft$vrBackground(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning && dh.vrSettings != null && !dh.vrSettings.menuBackground) {
            ci.cancel();
        }
    }
}
