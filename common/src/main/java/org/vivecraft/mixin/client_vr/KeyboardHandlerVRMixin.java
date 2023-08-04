package org.vivecraft.mixin.client_vr;

import net.minecraft.client.Screenshot;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.VRState;

import java.io.File;
import java.util.function.Consumer;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerVRMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0), method = "keyPress", cancellable = true)
    public void screenHandler(long l, int i, int j, int k, int m, CallbackInfo ci) {
        if (i == 256 && k == 1) {
            if (org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler.Showing)
            {
                org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler.setOverlayShowing(false);
                if(this.minecraft.screen instanceof ChatScreen) {
                    minecraft.screen.onClose();
                }
                ci.cancel();
            }

            if (RadialHandler.isShowing()) {
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
        if (!VRState.vrRunning) {
            Screenshot.grab(file, renderTarget, consumer);
        } else {
            ClientDataHolderVR.getInstance().grabScreenShot = true;
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/Screen;passEvents:Z"), method = "keyPress")
    public boolean passEvents(Screen instance) {
        return instance.passEvents && !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    //TODO really bad
    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 2), method = "keyPress")
    public Screen screenKey(Minecraft instance) {
        return !MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL) ? instance.screen : null;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", ordinal = 1, shift = At.Shift.AFTER), method = "keyPress")
    public void saveHideGuiOptions(long l, int i, int j, int k, int m, CallbackInfo ci) {
        ClientDataHolderVR.getInstance().vrSettings.saveOptions();
    }
}
