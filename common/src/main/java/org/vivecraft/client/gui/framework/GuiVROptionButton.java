package org.vivecraft.client.gui.framework;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiVROptionButton extends Button implements GuiVROption {
    @Nonnull
    private final VrOptions enumOptions;

    public GuiVROptionButton(int x, int y, String text, OnPress action) {
        this(x, y, null, text, action);
    }

    public GuiVROptionButton(int x, int y, @Nullable VrOptions option, String text, OnPress action) {
        this(x, y, 150, 20, option, text, action);
    }

    public GuiVROptionButton(int x, int y, int width, int height, @Nullable VrOptions option, String text, OnPress action) {
        super(
            x,
            y,
            width,
            height,
            I18n.exists(text + ".button") ?
            Component.translatable(text + ".button") :
            Component.translatable(text),
            action,
            Button.DEFAULT_NARRATION
        );
        this.enumOptions = option == null ? VrOptions.DUMMY : option;

        if (option != null && ClientDataHolderVR.getInstance().vrSettings.overrides.hasSetting(option) &&
            ClientDataHolderVR.getInstance().vrSettings.overrides.getSetting(option).isValueOverridden()
        ) {
            this.active = false;
        }
    }

    public @Override
    @Nonnull VrOptions getOption() {
        return this.enumOptions;
    }
}
