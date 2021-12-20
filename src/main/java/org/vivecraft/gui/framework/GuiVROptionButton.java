package org.vivecraft.gui.framework;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import org.vivecraft.settings.VRSettings;

public class GuiVROptionButton extends Button
{
    @Nullable
    protected final VRSettings.VrOptions enumOptions;
    public int id = -1;

    public GuiVROptionButton(int id, int x, int y, String text, Button.OnPress action)
    {
        this(id, x, y, (VRSettings.VrOptions)null, text, action);
    }

    public GuiVROptionButton(int id, int x, int y, @Nullable VRSettings.VrOptions option, String text, Button.OnPress action)
    {
        this(id, x, y, 150, 20, option, text, action);
    }

    public GuiVROptionButton(int id, int x, int y, int width, int height, @Nullable VRSettings.VrOptions option, String text, Button.OnPress action)
    {
        super(x, y, width, height, text, action);
        this.id = id;
        this.enumOptions = option;
        Minecraft minecraft = Minecraft.getInstance();

        if (option != null && minecraft.vrSettings.overrides.hasSetting(option) && minecraft.vrSettings.overrides.getSetting(option).isValueOverridden())
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
