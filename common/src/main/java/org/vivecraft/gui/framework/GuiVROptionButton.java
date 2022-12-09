package org.vivecraft.gui.framework;

import javax.annotation.Nullable;

import org.vivecraft.ClientDataHolder;
import org.vivecraft.settings.VRSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiVROptionButton extends Button
{
    @Nullable
    protected final VRSettings.VrOptions enumOptions;
    public int id = -1;

    public GuiVROptionButton(int id, int x, int y, String text, OnPress action)
    {
        this(id, x, y, (VRSettings.VrOptions)null, text, action);
    }

    public GuiVROptionButton(int id, int x, int y, @Nullable VRSettings.VrOptions option, String text, OnPress action)
    {
        this(id, x, y, 150, 20, option, text, action);
    }

    public GuiVROptionButton(int id, int x, int y, int width, int height, @Nullable VRSettings.VrOptions option, String text, OnPress action)
    {
        super(x, y, width, height, Component.translatable(text), action, Button.DEFAULT_NARRATION);
        this.id = id;
        this.enumOptions = option;
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolder dataholder = ClientDataHolder.getInstance();

        if (option != null && dataholder.vrSettings.overrides.hasSetting(option) && dataholder.vrSettings.overrides.getSetting(option).isValueOverridden())
        {
            this.active = false;
        }
    }

    @Nullable
    public VRSettings.VrOptions getOption()
    {
        return this.enumOptions;
    }
}
