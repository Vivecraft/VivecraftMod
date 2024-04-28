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
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;

@Mixin(Screen.class)
public abstract class ScreenVRMixin extends AbstractContainerEventHandler implements Renderable {

    @Inject(at = @At("HEAD"), method = "renderBlurredBackground", cancellable = true)
    public void vivecraft$noGuiBlur(CallbackInfo ci) {
        // TODO make blur work in VR
        if (VRState.vrRunning) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = {"renderBackground", "renderPanorama", "renderTransparentBackground"}, cancellable = true)
    public void vivecraft$noBackground(CallbackInfo ci) {
        if (VRState.vrRunning && ((MethodHolder.willBeInMenuRoom((Screen) (Object) this) && (ClientDataHolderVR.getInstance().menuWorldRenderer.isReady() || ClientDataHolderVR.getInstance().vrSettings.menuWorldFallbackPanorama)) || (!MethodHolder.willBeInMenuRoom((Screen) (Object) this) && !ClientDataHolderVR.getInstance().vrSettings.menuBackground))) {
            ci.cancel();
        }
    }
}
