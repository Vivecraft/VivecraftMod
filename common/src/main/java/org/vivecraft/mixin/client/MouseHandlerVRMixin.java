package org.vivecraft.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.ClientDataHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerVRMixin {

    @Shadow
    private boolean isMiddlePressed;
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Shadow private boolean mouseGrabbed;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDD)Z", shift = At.Shift.BEFORE), method = "onScroll", cancellable = true)
    public void cancelScroll(long g, double h, double f, CallbackInfo ci) {
        if (this.minecraft.screen.mouseScrolled(g, h, f)) {
            ci.cancel();
        }
    }

    @Redirect(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private boolean checkNull(LocalPlayer instance) {
        return instance != null && instance.isSpectator();
    }

    @Inject(at = @At("HEAD"), method = "turnPlayer", cancellable = true)
    public void noTurn(CallbackInfo ci) {
        ci.cancel();
    }

    public boolean isMiddleDown() {
        return this.isMiddlePressed;
    }

    public double getXVelocity() {
        return this.accumulatedDX;
    }

    public double getYVelocity() {
        return this.accumulatedDY;
    }

    @Inject(at = @At("HEAD"), method = "grabMouse", cancellable = true)
    public void seated(CallbackInfo ci) {
        if (!ClientDataHolder.getInstance().vrSettings.seated) {
            this.mouseGrabbed = true;
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "releaseMouse", cancellable = true)
    public void grabMouse(CallbackInfo ci) {
        if (!ClientDataHolder.getInstance().vrSettings.seated) {
            this.mouseGrabbed = false;
            ci.cancel();
        }
    }
}
