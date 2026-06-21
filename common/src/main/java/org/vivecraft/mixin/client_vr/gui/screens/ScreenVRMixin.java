package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;

@Mixin(Screen.class)
public abstract class ScreenVRMixin {

    @Inject(method = {"extractBackground", "extractPanorama", "extractTransparentBackground"}, at = @At("HEAD"), cancellable = true)
    private void vivecraft$vrNoBackground(CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            if (!ClientDataHolderVR.getInstance().vrSettings.menuBackground &&
                (ClientDataHolderVR.getInstance().menuWorldRenderer.isReady() ||
                    ClientDataHolderVR.getInstance().vrSettings.menuWorldFallbackPanorama ||
                    !MethodHolder.willBeInMenuRoom((Screen) (Object) this)
                ))
            {
                ci.cancel();
            }
        }
    }

    @Inject(method = "extractBlurredBackground", at = @At("HEAD"), cancellable = true)
    public void vivecraft$noGuiBlur(CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            ci.cancel();
        }
    }

    @Inject(method = "extractPanorama", at = @At("HEAD"), cancellable = true)
    private void vivecraft$maybeNoPanorama(CallbackInfo ci) {
        if (VRState.VR_RUNNING && (ClientDataHolderVR.getInstance().menuWorldRenderer.isReady() ||
            ClientDataHolderVR.getInstance().vrSettings.menuWorldFallbackPanorama
        ))
        {
            ci.cancel();
        }
    }
}
