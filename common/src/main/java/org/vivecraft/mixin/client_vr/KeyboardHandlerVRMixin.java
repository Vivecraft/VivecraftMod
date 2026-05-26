package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.CloseKeyboardContext;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.settings.VRHotkeys;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerVRMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0), cancellable = true)
    private void vivecraft$handleVivecraftKeys(
        long windowPointer, int action, KeyEvent keyEvent, CallbackInfo ci)
    {
        if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
            if (org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler.SHOWING) {
                org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler.hideOverlay(CloseKeyboardContext.FORCE);

                // close chat with the keyboard
                if (this.minecraft.gui.screen() instanceof ChatScreen) {
                    this.minecraft.gui.screen().onClose();
                }
                ci.cancel();
            }

            if (RadialHandler.isShowing()) {
                RadialHandler.setOverlayShowing(false, null);
                ci.cancel();
            }
        }

        if (VRHotkeys.handleKeyboardInputs(keyEvent.key(), keyEvent.scancode(), action, keyEvent.modifiers())) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Lnet/minecraft/client/Minecraft;Z)V"))
    private void vivecraft$markScreenshot(
        Minecraft minecraft, boolean debugPanoramaRequested, Operation<Void> original)
    {
        if (!VRState.VR_RUNNING) {
            original.call(minecraft, debugPanoramaRequested);
        } else {
            ClientDataHolderVR.getInstance().grabScreenShot = true;
        }
    }
}
