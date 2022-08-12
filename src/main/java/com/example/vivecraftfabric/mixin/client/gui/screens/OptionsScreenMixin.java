package com.example.vivecraftfabric.mixin.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.gui.settings.GuiMainVRSettings;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component component) {
        super(component);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Option;createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;", ordinal = 0))
    private AbstractWidget addVivecraftSettings(Option option, Options options, int i, int j, int k) {
        if (option == Option.FOV)
        {
            return new Button(i, j, k, 20, new TranslatableComponent("vivecraft.options.screen.main.button"), (p) ->
            {
                Minecraft.getInstance().options.save();
                Minecraft.getInstance().setScreen(new GuiMainVRSettings(this));
            });
        }
        else
        {
            return option.createButton(options, i, j, k);
        }
    }
}
