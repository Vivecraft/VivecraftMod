package org.vivecraft.mixin.client_vr;

import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.settings.VRHotkeys;

import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.function.Consumer;

import static org.vivecraft.client_vr.VRState.*;

import static org.lwjgl.glfw.GLFW.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.KeyboardHandler.class)
public class KeyboardHandlerVRMixin {

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0), method = "keyPress", cancellable = true)
    public void screenHandler(long l, int i, int j, int k, int m, CallbackInfo ci) {
        if (i == GLFW_KEY_ESCAPE && k == GLFW_PRESS)
        {
            if (KeyboardHandler.isShowing())
            {
                KeyboardHandler.setOverlayShowing(false);
                if(mc.screen instanceof ChatScreen)
                {
                    mc.screen.onClose();
                }
                ci.cancel();
            }

            if (RadialHandler.isShowing())
            {
                RadialHandler.setOverlayShowing(false, null);
                ci.cancel();
            }
        }

        if (VRHotkeys.handleKeyboardInputs(i, j, k, m)) {
            ci.cancel();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"), method = "keyPress")
    public void noScreenshot(File file, RenderTarget renderTarget, Consumer<Component> consumer) {
        if (!vrRunning) {
            Screenshot.grab(file, renderTarget, consumer);
        } else {
            dh.grabScreenShot = true;
        }
    }

    //TODO really bad
    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 2), method = "keyPress")
    public Screen screenKey(net.minecraft.client.Minecraft instance) {
        return !VRHotkeys.isKeyDown(GLFW_KEY_RIGHT_CONTROL) ? instance.screen : null;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", ordinal = 1, shift = Shift.AFTER), method = "keyPress")
    public void saveHideGuiOptions(long l, int i, int j, int k, int m, CallbackInfo ci) {
        dh.vrSettings.saveOptions();
    }
}
