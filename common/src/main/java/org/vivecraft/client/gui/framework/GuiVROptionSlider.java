package org.vivecraft.client.gui.framework;

import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;

public class GuiVROptionSlider extends AbstractSliderButton implements GuiVROption {
    @Nullable
    private final VRSettings.VrOptions enumOptions;
    private int id = -1;

    public GuiVROptionSlider(int id, int x, int y, int width, int height, VRSettings.VrOptions option) {
        super(x, y, width, height,
            Component.literal(ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(option)),
            option.normalizeValue(ClientDataHolderVR.getInstance().vrSettings.getOptionFloatValue(option)));

        this.id = id;
        this.enumOptions = option;
    }

    public GuiVROptionSlider(int id, int x, int y, VRSettings.VrOptions option) {
        this(id, x, y, 150, 20, option);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal(ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(this.enumOptions)));
    }

    @Override
    protected void applyValue() {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        double d0 = this.enumOptions.denormalizeValue((float) this.value);
        dataholder.vrSettings.setOptionFloatValue(this.enumOptions, (float) d0);
        // with that keyboard changes don't work, if there are fewer options than pixels
        InputType inputType = Minecraft.getInstance().getLastInputType();
        if (inputType == InputType.MOUSE) {
            this.value = this.enumOptions.normalizeValue((float) d0);
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    @Nullable
    public VRSettings.VrOptions getOption() {
        return this.enumOptions;
    }
}
