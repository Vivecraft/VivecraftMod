package org.vivecraft.client.gui.framework.widgets;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;

public class GuiVROptionButton extends Button.Plain implements GuiVROption {
    @Nullable
    private final VRSettings.VrOptions enumOptions;
    private int id = -1;

    public GuiVROptionButton(int id, int x, int y, String text, OnPress action) {
        this(id, x, y, null, text, action);
    }

    public GuiVROptionButton(int id, int x, int y, @Nullable VRSettings.VrOptions option, String text, OnPress action) {
        this(id, x, y, 150, 20, option, text, action);
    }

    public GuiVROptionButton(
        int id, int x, int y, int width, int height, @Nullable VRSettings.VrOptions option, String text,
        OnPress onPress)
    {
        super(x, y, width, height, Component.translatable(text), onPress, Button.DEFAULT_NARRATION);
        this.id = id;
        this.enumOptions = option;

        VRSettings.ServerOverrides overrides = ClientDataHolderVR.getInstance().vrSettings.overrides;
        if (option != null) {
            this.active = option.isChangeable() &&
                !(overrides.hasSetting(option) && overrides.getSetting(option).isValueOverridden());
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    @Nullable
    public VRSettings.VrOptions getOption() {
        return this.enumOptions;
    }
}
