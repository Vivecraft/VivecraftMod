package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(Screen.class)
public abstract class ScreenVRMixin extends AbstractContainerEventHandler implements Renderable {

    @Inject(at = @At("HEAD"), method = "renderTransparentBackground", cancellable = true)
    public void vivecraft$vrBackground(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().vrSettings != null && !ClientDataHolderVR.getInstance().vrSettings.menuBackground) {
            ci.cancel();
        }
    }
}
