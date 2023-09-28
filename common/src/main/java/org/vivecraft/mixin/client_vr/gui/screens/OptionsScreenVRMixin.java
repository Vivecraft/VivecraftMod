package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.gui.settings.GuiMainVRSettings;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(OptionsScreen.class)
public class OptionsScreenVRMixin extends Screen {
    protected OptionsScreenVRMixin(Component component) {
        super(component);
    }

    // replace FOV slider
    /*
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;"))
    private AbstractWidget vivecraft$addVivecraftSettings(OptionInstance option, Options options, int i, int j, int k) {
        if (option == options.fov()) {
            return new Button.Builder( Component.translatable("vivecraft.options.screen.main.button"),  (p) ->
                {
                    Minecraft.getInstance().options.save();
                    Minecraft.getInstance().setScreen(new GuiMainVRSettings(this));
                })
                .size( k,  20)
                .pos(i,  j)
                .build();
        } else {
            return option.createButton(options, i, j, k);
        }
    }
    */

    // place below FOV slider
    @Inject(method = "init", at = @At(value = "HEAD"))
    private void vivecraft$addVivecraftSettings(CallbackInfo ci) {
        if (ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled) {
            int xOffset = ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft
                          ? -155
                          : 5;

            this.addRenderableWidget(new Button(this.width / 2 + xOffset, this.height / 6 - 12 + 24, 150, 20, Component.translatable("vivecraft.options.screen.main.button"), (p) ->
            {
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().setScreen(new GuiMainVRSettings(this));
            }));
        }
    }
}
