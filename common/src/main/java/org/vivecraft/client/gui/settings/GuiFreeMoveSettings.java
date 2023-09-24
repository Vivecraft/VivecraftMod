package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client_vr.VRState.dh;

public class GuiFreeMoveSettings extends GuiVROptionsBase
{
    public static String vrTitle = "vivecraft.options.screen.freemove";

    public GuiFreeMoveSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    @Override
    public void init()
    {
        super.clearWidgets();

        if (dh.vrSettings.seated)
        {
            super.init(
                VrOptions.SEATED_HMD,
                VrOptions.FOV_REDUCTION,
                VrOptions.INERTIA_FACTOR
            );
        }
        else
        {
            super.init(
                VrOptions.FREEMOVE_MODE,
                VrOptions.FREEMOVE_FLY_MODE,
                VrOptions.FOV_REDUCTION,
                VrOptions.INERTIA_FACTOR,
                VrOptions.MOVEMENT_MULTIPLIER,
                VrOptions.AUTO_SPRINT,
                VrOptions.AUTO_SPRINT_THRESHOLD,
                VrOptions.ANALOG_MOVEMENT
            );
        }

        if (dh.vrSettings.useFOVReduction)
        {
            super.init(
                VrOptions.FOV_REDUCTION_MIN,
                VrOptions.FOV_REDUCTION_OFFSET
            );
        }

        super.addDefaultButtons();
    }

    @Override
    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if (guivroption.getOption() == VrOptions.FOV_REDUCTION)
            {
                this.reinit = true;
            }
        }
    }
}
