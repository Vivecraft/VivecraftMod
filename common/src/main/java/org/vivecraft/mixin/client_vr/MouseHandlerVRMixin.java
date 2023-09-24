package org.vivecraft.mixin.client_vr;

import org.joml.Vector3d;

import net.minecraft.client.player.LocalPlayer;

import static org.vivecraft.client_vr.VRState.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.MouseHandler.class)
public class MouseHandlerVRMixin {

    @Shadow private boolean mouseGrabbed;

    // TODO, this seems unnecessary, and wrong
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDD)Z", shift = Shift.BEFORE), method = "onScroll", cancellable = true)
    public void cancelScroll(long g, double h, double f, CallbackInfo ci) {
        if (mc.screen.mouseScrolled(g, h, f)) {
            ci.cancel();
        }
    }

    @Redirect(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private boolean checkNull(LocalPlayer instance) {
        return instance != null && instance.isSpectator();
    }


    @Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
    public void noTurnStanding(CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }

        if (!dh.vrSettings.seated) {
            // call the tutorial before canceling
            // head movement
            // mc.getTutorial().onMouse(1.0 - dh.vr.hmdHistory.averagePosition(0.2).subtract(dh.vr.hmdPivotHistory.averagePosition(0.2)).normalize().dot(dh.vr.hmdHistory.averagePosition(1.0).subtract(dh.vr.hmdPivotHistory.averagePosition(1.0)).normalize()),0);
            // controller movement
            int mainController = dh.vrSettings.reverseHands ? 1 : 0;
            mc.getTutorial().onMouse(1.0 - dh.vr.controllerForwardHistory[mainController].averagePosition(0.2, new Vector3d()).normalize().dot(dh.vr.controllerForwardHistory[mainController].averagePosition(1.0, new Vector3d()).normalize()),0);
            ci.cancel();
        }
    }

    // cancel after tutorial call
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onMouse(DD)V", shift = Shift.AFTER), method = "turnPlayer", cancellable = true)
    public void noTurnSeated(CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }

        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "grabMouse", cancellable = true)
    public void seated(CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }

        if (!dh.vrSettings.seated) {
            this.mouseGrabbed = true;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "releaseMouse", cancellable = true)
    public void grabMouse(CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }

        if (!dh.vrSettings.seated) {
            this.mouseGrabbed = false;
            ci.cancel();
        }
    }
}
