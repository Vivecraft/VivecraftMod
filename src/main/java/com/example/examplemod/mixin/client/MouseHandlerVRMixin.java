package com.example.examplemod.mixin.client;

import com.example.examplemod.DataHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.objectweb.asm.Opcodes;
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

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z", ordinal = 0), method = "grabMouse")
    public boolean seated(Minecraft instance) {
        return !DataHolder.getInstance().vrSettings.seated;
    }

    @Inject(at = @At(value = "HEAD"), method = "releaseMouse", cancellable = true)
    public void grabMouse(CallbackInfo ci) {
        if (!DataHolder.getInstance().vrSettings.seated) {
            this.mouseGrabbed = false;
            ci.cancel();
        }
    }
}
