package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.Xloader;
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

    @Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getTutorial()Lnet/minecraft/client/tutorial/Tutorial;"))
    private void vivecraft$modifyMouseTravel(
        CallbackInfo ci,
        // this is stupid, but the locals have differnt ordinals on different modloaders
        @Local(ordinal = 1) LocalDoubleRef fabricX, @Local(ordinal = 2) LocalDoubleRef fabricY,
        @Local(ordinal = 4) LocalDoubleRef forgeX, @Local(ordinal = 5) LocalDoubleRef forgeY)
    {
        if (VRState.VR_RUNNING) {
            int mainController = ClientDataHolderVR.getInstance().vrSettings.reverseHands ? 1 : 0;
            Vector3f up = MCVR.get().controllerUpHistory[mainController].averagePosition(0.1).normalize();
            Vector3f cur = MCVR.get().controllerForwardHistory[mainController].averagePosition(0.1).normalize();
            Vector3f prev = MCVR.get().controllerForwardHistory[mainController].averagePosition(0.3).normalize();

            // yaw
            double velX = (Math.atan2(-prev.x, prev.z) - Math.atan2(-cur.x, cur.z)) * Mth.RAD_TO_DEG;

            // pitch
            double velY = (Math.asin(prev.y) - Math.asin(cur.y)) * (up.y < 0 ? -1 : 1) * Mth.RAD_TO_DEG;

            if (Xloader.INSTANCE.getModloader() == Xloader.ModLoader.FABRIC ||
                Xloader.INSTANCE.getModloader() == Xloader.ModLoader.QUILT)
            {
                fabricX.set(velX);
                fabricY.set(velY);
            } else {
                forgeX.set(velX);
                forgeY.set(velY);
            }
        }
    }

    @WrapWithCondition(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private boolean vivecraft$noTurning(LocalPlayer instance, double x, double y) {
        return !VRState.VR_RUNNING;
    }

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noMouseGrab(CallbackInfo ci) {
        if (!VRState.VR_RUNNING) {
            return;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            this.mouseGrabbed = true;
            ci.cancel();
        }
    }

    @Inject(method = "releaseMouse", at = @At(value = "HEAD"), cancellable = true)
    private void vivecraft$noMouseReleaseMovement(CallbackInfo ci) {
        if (!VRState.VR_RUNNING) {
            return;
        }

        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            this.mouseGrabbed = false;
            ci.cancel();
        }
    }

    // we change the screen size different from window size, so need to modify the mouse position on grab/release
    @ModifyArg(method = {"grabMouse", "releaseMouse"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"), index = 2)
    private double vivecraft$modifyXCenter(double x) {
        return VRState.VR_RUNNING
            ? (double) ((WindowExtension) (Object) this.minecraft.getWindow()).vivecraft$getActualScreenWidth() / 2
            : x;
    }

    @ModifyArg(method = {"grabMouse", "releaseMouse"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"), index = 3)
    private double vivecraft$modifyYCenter(double y) {
        return VRState.VR_RUNNING
            ? (double) ((WindowExtension) (Object) this.minecraft.getWindow()).vivecraft$getActualScreenHeight() / 2
            : y;
    }

    // we change the screen size different from window size, so adapt move events to the emulated size
    @ModifyVariable(method = "onMove", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private double vivecraft$modifyX(double x) {
        if (VRState.VR_RUNNING) {
            x *= GuiHandler.GUI_WIDTH /
                (double) ((WindowExtension) (Object) this.minecraft.getWindow()).vivecraft$getActualScreenWidth();
        }
        return x;
    }

    @ModifyVariable(method = "onMove", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
    private double vivecraft$modifyY(double y) {
        if (VRState.VR_RUNNING) {
            y *= (double) GuiHandler.GUI_HEIGHT /
                (double) ((WindowExtension) (Object) this.minecraft.getWindow()).vivecraft$getActualScreenHeight();
        }
        return y;
    }
}
