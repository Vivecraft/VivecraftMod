package org.vivecraft.mixin.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.WindowExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.MCVR;

@Mixin(MouseHandler.class)
public class MouseHandlerVRMixin {

    @Shadow
    private boolean mouseGrabbed;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Redirect(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private boolean vivecraft$checkNull(LocalPlayer instance) {
        return instance != null && instance.isSpectator();
    }


    @Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
    public void vivecraft$noTurnStanding(CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            // call the tutorial before canceling
            // head movement
            // this.minecraft.getTutorial().onMouse(1.0 - MCVR.get().hmdHistory.averagePosition(0.2).subtract(MCVR.get().hmdPivotHistory.averagePosition(0.2)).normalize().dot(MCVR.get().hmdHistory.averagePosition(1.0).subtract(MCVR.get().hmdPivotHistory.averagePosition(1.0)).normalize()),0);
            // controller movement
            int mainController = ClientDataHolderVR.getInstance().vrSettings.reverseHands ? 1 : 0;
            this.minecraft.getTutorial().onMouse(1.0 - MCVR.get().controllerForwardHistory[mainController].averagePosition(0.2).normalize().dot(MCVR.get().controllerForwardHistory[mainController].averagePosition(1.0).normalize()), 0);
            ci.cancel();
        }
    }

    // cancel after tutorial call
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onMouse(DD)V", shift = At.Shift.AFTER), method = "turnPlayer", cancellable = true)
    public void vivecraft$noTurnSeated(CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }

        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "grabMouse", cancellable = true)
    public void vivecraft$seated(CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            this.mouseGrabbed = true;
            ci.cancel();
        }
    }

    // we change the screen size different from window size, so need to modify the mouse position on grab/release
    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 2, method = {"grabMouse", "releaseMouse"})
    public double vivecraft$modifyXCenter(double x) {
        return VRState.vrRunning
               ? (double) ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenWidth() / 2
               : x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"), index = 3, method = {"grabMouse", "releaseMouse"})
    public double vivecraft$modifyYCenter(double y) {
        return VRState.vrRunning
               ? (double) ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenHeight() / 2
               : y;
    }

    @Inject(at = @At(value = "HEAD"), method = "releaseMouse", cancellable = true)
    public void vivecraft$grabMouse(CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            this.mouseGrabbed = false;
            ci.cancel();
        }
    }

    // we change the screen size different from window size, so adapt move events to the emulated size
    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 0, method = "onMove", argsOnly = true)
    public double vivecraft$modifyX(double x) {
        if (VRState.vrRunning) {
            x *= GuiHandler.guiWidth / (double) ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenWidth();
        }
        return x;
    }

    @ModifyVariable(at = @At(value = "HEAD"), ordinal = 1, method = "onMove", argsOnly = true)
    public double vivecraft$modifyY(double y) {
        if (VRState.vrRunning) {
            y *= (double) GuiHandler.guiHeight / (double) ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenHeight();
        }
        return y;
    }
}
