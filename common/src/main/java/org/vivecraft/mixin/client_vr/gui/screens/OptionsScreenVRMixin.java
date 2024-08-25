package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.settings.GuiMainVRSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(OptionsScreen.class)
public class OptionsScreenVRMixin {

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private int vivecraft$makeSpacer1wide(int occupiedColumns) {
        // if we add the VR settings button, use one of the spacer slots
        return ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled ? 1 : occupiedColumns;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private void vivecraft$addVivecraftSettingsLeft(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled &&
            ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft)
        {
            vivecraft$addVivecraftButton(rowHelper);
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;", shift = At.Shift.AFTER))
    private void vivecraft$addVivecraftSettingsRight(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled &&
            !ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft)
        {
            vivecraft$addVivecraftButton(rowHelper);
        }
    }

    @Unique
    private void vivecraft$addVivecraftButton(GridLayout.RowHelper rowHelper) {
        rowHelper.addChild(new Button.Builder(Component.translatable("vivecraft.options.screen.main.button"),
            (p) -> {
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().setScreen(new GuiMainVRSettings((Screen) (Object) this));
            }).build());
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"))
    private void vivecraft$noBigButtonsPlease(CallbackInfo ci, @Local GridLayout gridLayout) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            gridLayout.visitChildren(child -> {
                if (child.getWidth() > 150 && child instanceof Button button) {
                    button.setWidth(150);
                }
            });
        }
    }
}
