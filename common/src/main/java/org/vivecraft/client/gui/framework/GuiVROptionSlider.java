package org.vivecraft.client.gui.framework;

import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.InputType;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class GuiVROptionSlider extends AbstractSliderButton implements GuiVROption
{
    @Nonnull
    private final VrOptions enumOptions;

    public GuiVROptionSlider(int x, int y, int width, int height, @Nonnull VrOptions option)
    {
        super(
            x,
            y,
            width,
            height,
            Component.literal(dh.vrSettings.getButtonDisplayString(option)),
            option.normalizeValue(dh.vrSettings.getOptionFloatValue(option))
        );

        this.enumOptions = option;
    }

    public GuiVROptionSlider(int x, int y, @Nullable VrOptions option)
    {
        this(x, y, 150, 20, option == null ? VrOptions.DUMMY : option);
    }

    @Override
    protected void updateMessage()
    {
        this.setMessage(Component.literal(dh.vrSettings.getButtonDisplayString(this.enumOptions)));
    }

    @Override
    protected void applyValue()
    {
        double d0 = this.enumOptions.denormalizeValue((float) this.value);
        dh.vrSettings.setOptionFloatValue(this.enumOptions, (float) d0);
        // with that keyboard changes don't work, if there are fewer options than pixels
        InputType inputType = mc.getLastInputType();
        if (inputType == InputType.MOUSE)
        {
            this.value = this.enumOptions.normalizeValue((float) d0);
        }
    }

    public @Override @Nonnull VrOptions getOption()
    {
        return this.enumOptions;
    }
}
