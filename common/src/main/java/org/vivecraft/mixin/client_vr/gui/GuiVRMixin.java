package org.vivecraft.mixin.client_vr.gui;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(Gui.class)
public class GuiVRMixin {

    @Shadow
    private @Nullable Screen screen;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;toggle()V", shift = At.Shift.AFTER))
    private void vivecraft$saveHideGuiOption(CallbackInfo ci) {
        ClientDataHolderVR.getInstance().vrSettings.saveOptions();
    }


    @Inject(method = "setOverlay", at = @At("TAIL"))
    private void vivecraft$onOverlaySet(CallbackInfo ci) {
        GuiHandler.onScreenChanged(this.screen, this.screen, true);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void vivecraft$onScreenChange(
        Screen guiScreen, CallbackInfo ci, @Share("guiScale") LocalIntRef guiScaleRef)
    {
        if (guiScreen == null) {
            GuiHandler.GUI_APPEAR_OVER_BLOCK_ACTIVE = false;
        }
        // cache gui scale so it can be checked after screen apply
        guiScaleRef.set(this.minecraft.options.guiScale().get());
    }

    @Inject(method = "setScreen", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/Gui;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0))
    private void vivecraft$onScreenSet(Screen guiScreen, CallbackInfo ci) {
        GuiHandler.onScreenChanged(this.screen, guiScreen, true);
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    private void vivecraft$checkGuiScaleChangePost(CallbackInfo ci, @Share("guiScale") LocalIntRef guiScaleRef) {
        if (guiScaleRef.get() != this.minecraft.options.guiScale().get()) {
            // checks if something changed the GuiScale during screen change
            // and tries to adjust the VR GuiScale accordingly
            int maxScale = VRState.VR_RUNNING ? GuiHandler.GUI_SCALE_FACTOR_MAX :
                this.minecraft.getWindow().calculateScale(0, this.minecraft.options.forceUnicodeFont().get());

            // auto uses max scale
            if (guiScaleRef.get() == 0) {
                guiScaleRef.set(maxScale);
            }

            int newScale =
                this.minecraft.options.guiScale().get() == 0 ? maxScale : this.minecraft.options.guiScale().get();

            if (newScale < guiScaleRef.get()) {
                // if someone reduced the gui scale, try to reduce the VR gui scale by the same steps
                int newVRScale = VRState.VR_RUNNING ? newScale :
                    Math.max(1, GuiHandler.GUI_SCALE_FACTOR_MAX - (guiScaleRef.get() - newScale));
                GuiHandler.GUI_SCALE_FACTOR = GuiHandler.calculateScale(newVRScale,
                    this.minecraft.options.forceUnicodeFont().get(),
                    GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT);
            } else {
                // new gui scale is bigger than before, so just reset to the default
                VRSettings vrSettings = ClientDataHolderVR.getInstance().vrSettings;
                GuiHandler.GUI_SCALE_FACTOR = GuiHandler.calculateScale(
                    vrSettings.doubleGUIResolution ? vrSettings.guiScale : (int) Math.ceil(vrSettings.guiScale * 0.5f),
                    this.minecraft.options.forceUnicodeFont().get(), GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT);
            }

            // resize the screen for the new gui scale
            if (VRState.VR_RUNNING && this.screen != null) {
                this.screen.resize(GuiHandler.SCALED_WIDTH, GuiHandler.SCALED_HEIGHT);
            }
        }
    }
}
