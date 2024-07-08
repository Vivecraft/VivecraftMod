package org.vivecraft.mixin.client_vr.gui.screens;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.options.OptionsScreen;
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;Ljava/util/function/Consumer;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 0), method = "init")
    private void vivecraft$addVivecraftSettingsSpacer(CallbackInfo ci, @Local(ordinal = 0) LinearLayout header) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            header.addChild(new SpacerElement(-150, 4), header.newCellSettings());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 2, shift = At.Shift.AFTER), method = "init")
    private void vivecraft$addVivecraftSettings(CallbackInfo ci, @Local(ordinal = 0) LinearLayout header) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            Button button = new Button.Builder(Component.translatable("vivecraft.options.screen.main.button"), (p) ->
            {
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().setScreen(new GuiMainVRSettings(this));
            })
                .build();

            if (!ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft) {
                header.addChild(button, header.newCellSettings().alignHorizontallyRight().paddingTop(-4));
            } else {
                header.addChild(button, header.newCellSettings().paddingTop(-4));
            }
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;visitWidgets(Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$noBigButtonsPlease(CallbackInfo ci, @Local GridLayout gridLayout) {
        gridLayout.visitChildren(child -> {
            if (child.getWidth() > 150 && child instanceof Button button) {
                button.setWidth(150);
            }
        });
    }
}
