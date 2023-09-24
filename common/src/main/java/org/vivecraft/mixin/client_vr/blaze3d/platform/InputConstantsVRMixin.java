package org.vivecraft.mixin.client_vr.blaze3d.platform;

import org.vivecraft.client_vr.provider.InputSimulator;

import com.mojang.blaze3d.platform.InputConstants;

import static org.vivecraft.client_vr.VRState.vrRunning;

import static org.lwjgl.glfw.GLFW.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
public class InputConstantsVRMixin {

    @Inject(at = @At("HEAD"), method = "isKeyDown", cancellable = true)
    private static void keyDown(long window, int key, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(glfwGetKey(window, key) == GLFW_PRESS || (vrRunning && InputSimulator.isKeyDown(key)));
    }
}
