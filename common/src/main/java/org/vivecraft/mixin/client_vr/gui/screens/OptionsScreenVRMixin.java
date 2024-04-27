package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.gui.settings.GuiMainVRSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(OptionsScreen.class)
public class OptionsScreenVRMixin extends Screen {
    protected OptionsScreenVRMixin(Component component) {
        super(component);
    }

    // replace FOV slider
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$addVivecraftSettings(CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            Button button = new Button.Builder(Component.translatable("vivecraft.options.screen.main.button"), (p) ->
            {
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().setScreen(new GuiMainVRSettings(this));
            })
                .build();
            if (!ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft) {
                // TODO 1.20.5 not sure if that does the right thing
                rowHelper.addChild(button, rowHelper.newCellSettings().alignHorizontallyRight());
            } else {
                rowHelper.addChild(button, 2, rowHelper.newCellSettings().alignHorizontallyLeft());
            }
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;visitWidgets(Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$noBigButtonsPlease(CallbackInfo ci, LinearLayout linearLayout, LinearLayout linearLayout2, GridLayout gridLayout) {
        gridLayout.visitChildren(child -> {
            if (child.getWidth() > 150 && child instanceof Button button) {
                button.setWidth(150);
            }
        });
    }
}
